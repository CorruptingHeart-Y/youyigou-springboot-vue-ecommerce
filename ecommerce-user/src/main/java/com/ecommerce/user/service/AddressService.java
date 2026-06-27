package com.ecommerce.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {

    List<Address> listAddresses(Long userId);

    void addAddress(Long userId, AddressRequest request);

    void updateAddress(Long userId, Long addressId, AddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    void setDefault(Long userId, Long addressId);
}