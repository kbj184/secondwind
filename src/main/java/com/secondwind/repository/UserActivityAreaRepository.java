package com.secondwind.repository;

import com.secondwind.entity.UserActivityArea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserActivityAreaRepository extends JpaRepository<UserActivityArea, Long> {
    Optional<UserActivityArea> findByUserId(Long userId);
}
