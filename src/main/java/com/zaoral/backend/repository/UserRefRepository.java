package com.zaoral.backend.repository;

import com.zaoral.backend.entity.UserRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRefRepository extends JpaRepository<UserRef, String> {
}
