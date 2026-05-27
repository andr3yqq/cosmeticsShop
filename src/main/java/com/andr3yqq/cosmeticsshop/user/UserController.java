package com.andr3yqq.cosmeticsshop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    // @PostMapping("/login")
    // public ResponseEntity<UserResponseDTO> login(@RequestBody UserLoginDTO
    // userLoginDTO) {
    // try {
    //
    // }
    // }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserDTO userDto) {
        try {
            User user = userService.createUser(userDto);
            UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getEmail(),
                    user.getAddress().getId(),
                    user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getRole().getName(),
                    user.isActive(), "");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(userResponseDTO);
        }

    }
}
