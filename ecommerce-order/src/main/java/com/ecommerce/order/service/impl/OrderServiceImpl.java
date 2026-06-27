package com.ecommerce.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.OrderItemVO;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.*;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final PromotionMapper promotionMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, Long addressId, String payMethod, String remark) {
        return createOrder(userId, addressId, payMethod, remark, null, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, Long addressId, String payMethod, String remark, Long couponId,
            BigDecimal couponDiscount) {
        List<CartItem> checkedItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, true));

        if (checkedItems.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : checkedItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品【" + item.getProductName() + "】已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("商品【" + item.getProductName() + "】库存不足");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        String orderNo = IdUtil.getSnowflakeNextIdStr();

        BigDecimal discount = BigDecimal.ZERO;
        UserCoupon userCoupon = null;
        if (couponId != null && couponDiscount != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            Promotion promo = promotionMapper.selectById(couponId);
            if (promo == null || promo.getStatus() != 1 || !"COUPON".equals(promo.getType())) {
                throw new BusinessException("优惠券不可用");
            }
            userCoupon = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getPromotionId, couponId)
                    .eq(UserCoupon::getStatus, 0));
            if (userCoupon == null) {
                throw new BusinessException("您未领取该优惠券或已使用");
            }
            if (promo.getMinAmount() != null && totalAmount.compareTo(promo.getMinAmount()) < 0) {
                throw new BusinessException("未达到最低消费金额 ¥" + promo.getMinAmount());
            }
            discount = couponDiscount.compareTo(totalAmount) > 0 ? totalAmount : couponDiscount;
        }

        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .addressId(addressId)
                .totalAmount(totalAmount)
                .discountAmount(discount)
                .payAmount(totalAmount.subtract(discount))
                .payMethod(payMethod)
                .status("PENDING_PAY")
                .remark(remark)
                .build();
        this.save(order);

        for (CartItem item : checkedItems) {
            Product product = productMapper.selectById(item.getProductId());
            OrderItem orderItem = OrderItem.builder()
                    .orderNo(orderNo)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getCoverImage())
                    .price(product.getPrice())
                    .spec(item.getSpec())
                    .quantity(item.getQuantity())
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            product.setSales(product.getSales() + item.getQuantity());
            productMapper.updateById(product);
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, true));

        if (userCoupon != null) {
            userCoupon.setStatus(1);
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderNo(orderNo);
            userCouponMapper.updateById(userCoupon);
        }

        return toVO(order);
    }

    @Override
    @Transactional
    public void payOrder(String orderNo, String payMethod) {
        Order order = getOrder(orderNo);
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setStatus("PENDING_DELIVER");
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        order.setPayNo(IdUtil.getSnowflakeNextIdStr());
        this.updateById(order);
    }

    @Override
    public OrderVO getOrderDetail(String orderNo) {
        return toVO(getOrder(orderNo));
    }

    @Override
    public Page<OrderVO> listUserOrders(Long userId, int page, int size, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> pages = this.page(new Page<>(page, size), wrapper);
        return (Page<OrderVO>) pages.convert(this::toVO);
    }

    @Override
    public Page<OrderVO> listAllOrders(int page, int size, String orderNo, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> pages = this.page(new Page<>(page, size), wrapper);
        return (Page<OrderVO>) pages.convert(this::toVO);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, String orderNo) {
        Order order = getOrder(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许取消");
        }
        order.setStatus("CANCELLED");
        this.updateById(order);
        restoreStock(orderNo);
    }

    @Override
    @Transactional
    public void confirmReceive(Long userId, String orderNo) {
        Order order = getOrder(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许确认收货");
        }
        order.setStatus("COMPLETED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void refundOrder(Long userId, String orderNo) {
        Order order = getOrder(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"COMPLETED".equals(order.getStatus()) && !"PENDING_DELIVER".equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许申请退款");
        }
        order.setStatus("REFUNDING");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void adminCancelOrder(String orderNo) {
        Order order = getOrder(orderNo);
        if ("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许取消");
        }
        boolean needRestoreStock = "PENDING_DELIVER".equals(order.getStatus());
        order.setStatus("CANCELLED");
        this.updateById(order);
        if (needRestoreStock) {
            restoreStock(orderNo);
        }
    }

    @Override
    @Transactional
    public void adminDeliverOrder(String orderNo, String logisticsNo, String logisticsCompany) {
        Order order = getOrder(orderNo);
        if (!"PENDING_DELIVER".equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许发货");
        }
        order.setStatus("DELIVERED");
        order.setLogisticsNo(logisticsNo);
        order.setLogisticsCompany(logisticsCompany);
        this.updateById(order);
    }

    @Override
    @Transactional
    public void adminProcessRefund(String orderNo, boolean approve) {
        Order order = getOrder(orderNo);
        if (!"REFUNDING".equals(order.getStatus())) {
            throw new BusinessException("该订单不在退款中状态");
        }
        if (approve) {
            order.setStatus("REFUNDED");
            restoreStock(orderNo);
        } else {
            order.setStatus("COMPLETED");
        }
        this.updateById(order);
    }

    @Override
    public void exportOrdersToExcel(String filePath) {
        List<Order> list = this.list(new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单列表");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("订单号");
            header.createCell(1).setCellValue("用户ID");
            header.createCell(2).setCellValue("金额");
            header.createCell(3).setCellValue("支付方式");
            header.createCell(4).setCellValue("状态");
            header.createCell(5).setCellValue("时间");

            for (int i = 0; i < list.size(); i++) {
                Order o = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(o.getOrderNo());
                row.createCell(1).setCellValue(o.getUserId());
                row.createCell(2).setCellValue(o.getPayAmount().doubleValue());
                row.createCell(3).setCellValue(o.getPayMethod());
                row.createCell(4).setCellValue(o.getStatus());
                row.createCell(5)
                        .setCellValue(o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    private void restoreStock(String orderNo) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNo, orderNo));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.updateById(product);
            }
        }
    }

    private Order getOrder(String orderNo) {
        Order order = this.getById(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);

        Address address = addressMapper.selectById(order.getAddressId());
        if (address != null) {
            vo.setAddressDetail(
                    address.getProvince() + address.getCity() + address.getDistrict() + " " + address.getDetail());
            vo.setReceiverName(address.getReceiverName());
            vo.setReceiverPhone(address.getReceiverPhone());
        }

        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNo, order.getOrderNo()));
        vo.setItems(items.stream().map(item -> OrderItemVO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .spec(item.getSpec())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build()).collect(Collectors.toList()));

        return vo;
    }
}