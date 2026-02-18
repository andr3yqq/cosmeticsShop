package com.andr3yqq.cosmeticsshop.address;

public interface AddressService {
    Address createAddress(AddressDTO addressDTO);
    Address updateAddress(AddressDTO addressDTO);
    Address getAddressById(Long id);

}
