package com.secondwind.controller;

import com.secondwind.dto.UserProfileDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import com.secondwind.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    /**
     * Follow a user
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            followService.followUser(currentUser.getId(), userId, false);
            return ResponseEntity.ok(Map.of("message", "Successfully followed user"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        followService.unfollowUser(currentUser.getId(), userId);
        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user"));
    }

    /**
     * Get my followers
     */
    @GetMapping("/followers")
    public ResponseEntity<List<UserProfileDTO>> getFollowers() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<UserProfileDTO> followers = followService.getFollowers(currentUser.getId());
        return ResponseEntity.ok(followers);
    }

    /**
     * Get who I'm following
     */
    @GetMapping("/following")
    public ResponseEntity<List<UserProfileDTO>> getFollowing() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<UserProfileDTO> following = followService.getFollowing(currentUser.getId());
        return ResponseEntity.ok(following);
    }

    /**
     * Get mutual follows
     */
    @GetMapping("/mutual")
    public ResponseEntity<List<UserProfileDTO>> getMutualFollows() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<UserProfileDTO> mutualFollows = followService.getMutualFollows(currentUser.getId());
        return ResponseEntity.ok(mutualFollows);
    }

    /**
     * Get follow status with another user
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        FollowService.FollowStatus status = followService.getFollowStatus(currentUser.getId(), userId);
        return ResponseEntity.ok(Map.of(
                "isFollowing", status.isFollowing(),
                "isFollower", status.isFollower(),
                "isMutual", status.isMutual()));
    }

    /**
     * Get follower and following counts for a user
     */
    @GetMapping("/counts/{userId}")
    public ResponseEntity<?> getFollowCounts(@PathVariable Long userId) {
        long followerCount = followService.getFollowerCount(userId);
        long followingCount = followService.getFollowingCount(userId);

        return ResponseEntity.ok(Map.of(
                "followers", followerCount,
                "following", followingCount));
    }
}
