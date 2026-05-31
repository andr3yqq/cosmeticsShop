package com.andr3yqq.cosmeticsshop.address;

import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AddressDTO addressDTO;
    private Address address;
    private User user;

    @BeforeEach
    void setUp() {
        addressDTO = new AddressDTO(1L, "123 Street", "Apt 1", "City", "State", "12345", "Country", LocalDateTime.now(), LocalDateTime.now());
        address = new Address(1L, "123 Street", "Apt 1", "City", "State", "12345", "Country", LocalDateTime.now(), LocalDateTime.now());
        user = new User("test@example.com", "password", "John", "Doe");
        user.setId(2L);
    }

    @Test
    void createAddress_Success() {
        when(addressMapper.toAddress(addressDTO)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);

        Address result = addressService.createAddress(addressDTO);

        assertNotNull(result);
        assertEquals(address.getStreetLine1(), result.getStreetLine1());
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void updateAddress_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address result = addressService.updateAddress(addressDTO);

        assertNotNull(result);
        assertEquals("123 Street", result.getStreetLine1());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_NullId_ThrowsException() {
        addressDTO.setId(null);

        assertThrows(IllegalArgumentException.class, () -> addressService.updateAddress(addressDTO));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void updateAddress_NotFound_ThrowsException() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> addressService.updateAddress(addressDTO));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void getAddressById_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Address result = addressService.getAddressById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAddressById_NotFound_ReturnsNull() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        Address result = addressService.getAddressById(1L);

        assertNull(result);
    }

    @Test
    void createAddressForUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(addressMapper.toAddress(addressDTO)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);
        when(userRepository.save(user)).thenReturn(user);

        Address result = addressService.createAddressForUser(2L, addressDTO);

        assertNotNull(result);
        assertEquals(address, user.getAddress());
        verify(userRepository, times(1)).save(user);
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void createAddressForUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> addressService.createAddressForUser(2L, addressDTO));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void updateAddressForUser_ExistingAddress_Success() {
        user.setAddress(address);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(addressRepository.save(address)).thenReturn(address);

        Address result = addressService.updateAddressForUser(2L, addressDTO);

        assertNotNull(result);
        assertEquals("123 Street", result.getStreetLine1());
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void updateAddressForUser_NoExistingAddress_CallsCreate() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(addressMapper.toAddress(addressDTO)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);
        when(userRepository.save(user)).thenReturn(user);

        Address result = addressService.updateAddressForUser(2L, addressDTO);

        assertNotNull(result);
        assertEquals(address, user.getAddress());
        verify(addressRepository, times(1)).save(address);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteAddressForUser_Success() {
        user.setAddress(address);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(addressRepository).delete(address);

        addressService.deleteAddressForUser(2L);

        assertNull(user.getAddress());
        verify(userRepository, times(1)).save(user);
        verify(addressRepository, times(1)).delete(address);
    }
}
