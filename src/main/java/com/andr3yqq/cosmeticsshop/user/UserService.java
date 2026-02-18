package com.andr3yqq.cosmeticsshop.user;

import java.util.List;

public interface UserService {
    User createUser(UserDTO userDTO);
    User loginUser(UserLoginDTO userLoginDTO);
    User updateUser(UserDTO userDTO);
    User getUserById(Long id);
    List<User> getAllUsers();
    void resetPassword(UserLoginDTO userLoginDTO);
    Role getUserRole(Long userId);
}
