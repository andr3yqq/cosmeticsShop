package com.andr3yqq.cosmeticsshop.user;

public interface UserService {
    User createUser(UserDTO userDTO);
    User loginUser(UserDTO userDTO);
    User updateUser(UserDTO userDTO);
    void resetPassword(UserDTO userDTO);
}
