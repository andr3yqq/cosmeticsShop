package com.andr3yqq.cosmeticsshop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.getUserByEmail(userDTO.getEmail()).isEmpty()) {
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
        User user = userRepository.findById(userDTO.getId()).orElse(null);
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
        return userRepository.findById(userId).orElse(null);
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
