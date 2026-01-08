package com.secondwind.repository;

import com.secondwind.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    // Find all users that the given user is following
    List<UserFollow> findByFollowerId(Long followerId);

    // Find all users who follow the given user
    List<UserFollow> findByFollowingId(Long followingId);

    // Check if follower is following the user
    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Count how many users the given user is following
    long countByFollowerId(Long followerId);

    // Count how many followers the given user has
    long countByFollowingId(Long followingId);

    // Delete follow relationship
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Get mutual follows - users who follow each other
    @Query("SELECT uf1.followingId FROM UserFollow uf1 " +
            "WHERE uf1.followerId = :userId " +
            "AND EXISTS (SELECT 1 FROM UserFollow uf2 " +
            "WHERE uf2.followerId = uf1.followingId AND uf2.followingId = :userId)")
    List<Long> findMutualFollowIds(@Param("userId") Long userId);
}
