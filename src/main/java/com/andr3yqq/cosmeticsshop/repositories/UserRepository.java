package com.andr3yqq.cosmeticsshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andr3yqq.cosmeticsshop.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
