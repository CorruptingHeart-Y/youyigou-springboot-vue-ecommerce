package com.ecommerce.order.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Order;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "客户端-物流查询")
@RestController
@RequestMapping("/api/client/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final OrderMapper orderMapper;

    @Operation(summary = "查询物流信息")
    @GetMapping("/{orderNo}")
    public Result<?> queryLogistics(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getUserId().equals(user.getUserId())) {
            return Result.error("订单不存在");
        }

        List<Map<String, Object>> tracks = new ArrayList<>();

        String[] companies = {"顺丰速运", "圆通快递", "中通快递", "韵达快递", "EMS", "京东物流"};

        if (order.getLogisticsNo() != null && !order.getLogisticsNo().isEmpty()) {
            Random r = new Random(order.getLogisticsNo().hashCode());
            String company = order.getLogisticsCompany() != null ? order.getLogisticsCompany()
                    : companies[Math.abs(r.nextInt()) % companies.length];
            long baseTime = order.getCreateTime() != null
                    ? order.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis() - 86400000L * 3;

            String[][] mockNodes = {
                    {"已揽收", "包裹已被快递员取走"},
                    {"运输中", "正在发往目的地"},
                    {"到达中转站", "已到达中转分拣中心"},
                    {"派送中", "快递员正在派送您的包裹"},
                    {"已签收", "包裹已由本人签收"}
            };

            int nodesToShow = "DELIVERED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()) ? 5 : 3;
            for (int i = 0; i < nodesToShow; i++) {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date(baseTime + i * 86400000L / 2)));
                node.put("status", mockNodes[i][0]);
                node.put("detail", mockNodes[i][1]);
                tracks.add(node);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("logisticsNo", order.getLogisticsNo() != null ? order.getLogisticsNo() : "暂无物流单号");
        result.put("logisticsCompany", order.getLogisticsCompany() != null ? order.getLogisticsCompany() : "暂无");
        result.put("orderStatus", order.getStatus());
        result.put("tracks", tracks);

        return Result.success(result);
    }
}