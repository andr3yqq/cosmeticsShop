package com.andr3yqq.cosmeticsshop.user;

import com.andr3yqq.cosmeticsshop.address.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;

    @Override
    public User getOrCreateUserFromJwt(Jwt jwt) {
        String email = getClaimBySuffix(jwt, "email");
        String firstName = getClaimBySuffix(jwt, "given_name");
        String lastName = getClaimBySuffix(jwt, "family_name");

        if (email == null) {
            // Attempt to retrieve profile from Auth0 /userinfo endpoint
            try {
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
                if (issuer != null) {
                    if (!issuer.endsWith("/")) {
                        issuer += "/";
                    }
                    String userinfoUrl = issuer + "userinfo";
                    
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(jwt.getTokenValue());
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    
                    var response = restTemplate.exchange(
                        userinfoUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        java.util.Map.class
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        Map<String, Object> body = response.getBody();
                        if (body.get("email") != null) {
                            email = body.get("email").toString();
                        }
                        if (body.get("given_name") != null) {
                            firstName = body.get("given_name").toString();
                        } else if (body.get("nickname") != null) {
                            firstName = body.get("nickname").toString();
                        } else if (body.get("name") != null) {
                            firstName = body.get("name").toString();
                        }
                        
                        if (body.get("family_name") != null) {
                            lastName = body.get("family_name").toString();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch profile from Auth0 userinfo: " + e.getMessage());
            }
        }

        if (email == null) {
            email = jwt.getSubject();
        }

        if (email == null) {
            throw new IllegalArgumentException("JWT does not contain email or subject claims.");
        }

        List<String> jwtRoles = getRolesFromJwt(jwt);
        System.out.println("[JIT Provisioning] JWT Claims: " + jwt.getClaims());
        System.out.println("[JIT Provisioning] Extracted Roles: " + jwtRoles);
        String roleName = "ROLE_USER";
        for (String jwtRole : jwtRoles) {
            String upper = jwtRole.toUpperCase();
            if (upper.contains("ADMIN")) {
                roleName = "ROLE_ADMIN";
                break;
            }
        }

        var existingUserOpt = userRepository.getUserByEmail(email);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // JIT synchronize role if it has changed
            if (existingUser.getRole() == null || !existingUser.getRole().getName().equalsIgnoreCase(roleName)) {
                Role role = roleRepository.getRoleByName(roleName);
                if (role == null) {
                    role = new Role();
                    role.setName(roleName);
                    role = roleRepository.save(role);
                }
                existingUser.setRole(role);
                existingUser = userRepository.save(existingUser);
            }
            return existingUser;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword("EXTERNAL_AUTH0_MANAGED");

        if (firstName == null) {
            String fullName = getClaimBySuffix(jwt, "name");
            if (fullName != null && fullName.contains(" ")) {
                String[] parts = fullName.split(" ", 2);
                firstName = parts[0];
                lastName = parts[1];
            } else {
                firstName = fullName != null ? fullName : "Auth0";
                lastName = "User";
            }
        }

        user.setFirstName(firstName);
        user.setLastName(lastName != null ? lastName : "User");
        user.setActive(true);

        Role role = roleRepository.getRoleByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            role = roleRepository.save(role);
        }
        user.setRole(role);

        return userRepository.save(user);
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
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
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

    private String getClaimBySuffix(Jwt jwt, String suffix) {
        Object rawClaim = jwt.getClaim(suffix);
        if (rawClaim != null) {
            return rawClaim.toString();
        }

        if (jwt.getClaims() != null) {
            for (String claimName : jwt.getClaims().keySet()) {
                if (claimName.startsWith("http://") || claimName.startsWith("https://")) {
                    if (claimName.endsWith("/" + suffix) || claimName.endsWith(suffix)) {
                        Object customClaim = jwt.getClaim(claimName);
                        if (customClaim != null) {
                            return customClaim.toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getRolesFromJwt(Jwt jwt) {
        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        } else if (rolesClaim instanceof String) {
            return List.of((String) rolesClaim);
        }

        if (jwt.getClaims() != null) {
            for (String claimName : jwt.getClaims().keySet()) {
                if (claimName.startsWith("http://") || claimName.startsWith("https://")) {
                    if (claimName.endsWith("/roles") || claimName.endsWith("roles")) {
                        Object customRoles = jwt.getClaim(claimName);
                        if (customRoles instanceof List) {
                            return (List<String>) customRoles;
                        } else if (customRoles instanceof String) {
                            return List.of((String) customRoles);
                        }
                    }
                }
            }
        }

        Object permissionsClaim = jwt.getClaim("permissions");
        if (permissionsClaim instanceof List) {
            return (List<String>) permissionsClaim;
        }

        return java.util.Collections.emptyList();
    }
}
