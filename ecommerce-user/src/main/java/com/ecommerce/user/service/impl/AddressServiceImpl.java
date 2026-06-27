package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.entity.Address;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listAddresses(Long userId) {
        return this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime));
    }

    @Override
    @Transactional
    public void addAddress(Long userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
            address.setIsDefault(true);
        } else {
            long count = this.count(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
            address.setIsDefault(count == 0);
        }
        this.save(address);
    }

    @Override
    @Transactional
    public void updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
            address.setIsDefault(true);
        }
        this.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        this.removeById(addressId);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(true);
        this.updateById(address);
    }

    private void clearDefault(Long userId) {
        Address update = new Address();
        update.setIsDefault(false);
        this.update(update, new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
    }
}