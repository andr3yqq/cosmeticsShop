package com.andr3yqq.cosmeticsshop.address;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDTO toAddressDTO(Address address);

    Address toAddress(AddressDTO addressDTO);

    List<AddressDTO> toAddressDTOList(List<Address> addresses);

    List<Address> toAddressList(List<AddressDTO> addressDTOs);
}
