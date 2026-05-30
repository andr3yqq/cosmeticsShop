package com.andr3yqq.cosmeticsshop.user;

import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {
    User getOrCreateUserFromJwt(Jwt jwt);
    User updateUser(UserDTO userDTO);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    Role getUserRole(Long userId);
    void deleteUser(Long id);
}
