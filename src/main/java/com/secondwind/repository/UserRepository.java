package com.secondwind.repository;

import com.secondwind.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserAuth, Long> {
    UserAuth findUserAuthsByProviderId(String providerId);

    UserAuth findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    UserAuth findByNickname(String nickname);

}
