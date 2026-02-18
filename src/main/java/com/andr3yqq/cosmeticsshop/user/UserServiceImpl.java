package com.andr3yqq.cosmeticsshop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.getUserByEmail(userDTO.getEmail()) != null) {
            User user = new User(userDTO.getEmail(), userDTO.getPassword(), userDTO.getFirstName(), userDTO.getLastName());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User loginUser(UserLoginDTO userLoginDTO) {
        return null;
    }

    @Override
    public User updateUser(UserDTO userDTO) {
        User user = userRepository.getUserByid(userDTO.getId());
        if (user != null) {
            user.setEmail(userDTO.getEmail());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.getUserByid(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void resetPassword(UserLoginDTO userLoginDTO) {

    }

    @Override
    public Role getUserRole(Long userId) {
        User user = getUserById(userId);
        if (user != null)
            return user.getRole();
        return null;
    }
}
