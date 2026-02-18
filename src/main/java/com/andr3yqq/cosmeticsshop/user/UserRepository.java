package com.andr3yqq.cosmeticsshop.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByEmail(String email);

    User getUserByid(Long id);
}
