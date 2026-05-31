package com.andr3yqq.cosmeticsshop.address;

import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public Address createAddress(AddressDTO addressDTO) {
        Address address = addressMapper.toAddress(addressDTO);
        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(AddressDTO addressDTO) {
        if (addressDTO.getId() == null) {
            throw new IllegalArgumentException("Address ID cannot be null for updates");
        }
        Address existingAddress = addressRepository.findById(addressDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + addressDTO.getId()));

        existingAddress.setStreetLine1(addressDTO.getStreetLine1());
        existingAddress.setStreetLine2(addressDTO.getStreetLine2());
        existingAddress.setCity(addressDTO.getCity());
        existingAddress.setState(addressDTO.getState());
        existingAddress.setZipcode(addressDTO.getZipcode());
        existingAddress.setCountry(addressDTO.getCountry());

        return addressRepository.save(existingAddress);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public Address createAddressForUser(Long userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Address address = addressMapper.toAddress(addressDTO);
        Address savedAddress = addressRepository.save(address);
        user.setAddress(savedAddress);
        userRepository.save(user);
        return savedAddress;
    }

    @Override
    public Address updateAddressForUser(Long userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Address address = user.getAddress();
        if (address == null) {
            return createAddressForUser(userId, addressDTO);
        }

        address.setStreetLine1(addressDTO.getStreetLine1());
        address.setStreetLine2(addressDTO.getStreetLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipcode(addressDTO.getZipcode());
        address.setCountry(addressDTO.getCountry());

        return addressRepository.save(address);
    }

    @Override
    public void deleteAddressForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Address address = user.getAddress();
        if (address != null) {
            user.setAddress(null);
            userRepository.save(user);
            addressRepository.delete(address);
        }
    }
}
