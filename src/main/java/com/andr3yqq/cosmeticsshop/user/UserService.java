package com.andr3yqq.cosmeticsshop.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {
    User getOrCreateUserFromJwt(Jwt jwt);
    User updateUser(UserDTO userDTO);
    User getUserById(Long id);
    User getUserByEmail(String email);
    Page<User> getAllUsers(Pageable pageable);
    Role getUserRole(Long userId);
    void deleteUser(Long id);
}
