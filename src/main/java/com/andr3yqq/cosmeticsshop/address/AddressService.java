package com.andr3yqq.cosmeticsshop.address;

public interface AddressService {
    Address createAddress(AddressDTO addressDTO);
    Address updateAddress(AddressDTO addressDTO);
    Address getAddressById(Long id);

    Address createAddressForUser(Long userId, AddressDTO addressDTO);
    Address updateAddressForUser(Long userId, AddressDTO addressDTO);
    void deleteAddressForUser(Long userId);
}
