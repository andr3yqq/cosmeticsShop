package com.andr3yqq.cosmeticsshop.user;

import com.andr3yqq.cosmeticsshop.address.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.getUserByEmail(userDTO.getEmail()).isEmpty()) {
            User user = new User(userDTO.getEmail(), passwordEncoder.encode(userDTO.getPassword()), userDTO.getFirstName(), userDTO.getLastName());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setActive(true);

            // Set Role: Always force ROLE_USER for public registrations to prevent privilege escalation
            String roleName = "ROLE_USER";
            Role role = roleRepository.getRoleByName(roleName);
            if (role == null) {
                role = new Role();
                role.setName(roleName);
                role = roleRepository.save(role);
            }
            user.setRole(role);

            // Set Address
            if (userDTO.getAddressId() != null) {
                addressRepository.findById(userDTO.getAddressId()).ifPresent(user::setAddress);
            }

            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User loginUser(UserLoginDTO userLoginDTO) {
        User user = userRepository.getUserByEmail(userLoginDTO.getEmail()).orElse(null);
        if (user != null && passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            return user;
        }
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
            user.setActive(userDTO.isActive());

            // Only allow role updates if the active requester has ROLE_ADMIN authority
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (userDTO.getRole() != null && isAdmin) {
                Role role = roleRepository.getRoleByName(userDTO.getRole());
                if (role == null) {
                    role = new Role();
                    role.setName(userDTO.getRole());
                    role = roleRepository.save(role);
                }
                user.setRole(role);
            }

            if (userDTO.getAddressId() != null) {
                addressRepository.findById(userDTO.getAddressId()).ifPresent(user::setAddress);
            } else {
                user.setAddress(null);
            }

            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void resetPassword(UserLoginDTO userLoginDTO) {
        User user = userRepository.getUserByEmail(userLoginDTO.getEmail()).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(userLoginDTO.getPassword()));
            userRepository.save(user);
        }
    }

    @Override
    public Role getUserRole(Long userId) {
        User user = getUserById(userId);
        if (user != null)
            return user.getRole();
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
