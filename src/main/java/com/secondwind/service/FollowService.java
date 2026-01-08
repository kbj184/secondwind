package com.secondwind.service;

import com.secondwind.dto.UserProfileDTO;
import com.secondwind.entity.NotificationType;
import com.secondwind.entity.UserAuth;
import com.secondwind.entity.UserFollow;
import com.secondwind.repository.UserFollowRepository;
import com.secondwind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Follow a user
     * 
     * @param followerId  User who is following
     * @param followingId User being followed
     * @param silent      If true, don't send notification (for crew auto-follows)
     */
    @Transactional
    public void followUser(Long followerId, Long followingId, boolean silent) {
        // Prevent self-follow
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // Check if already following
        if (userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent()) {
            return; // Already following, do nothing
        }

        // Create follow relationship
        UserFollow follow = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        userFollowRepository.save(follow);

        // Send notification if not silent
        if (!silent) {
            UserAuth follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
            UserAuth following = userRepository.findById(followingId)
                    .orElseThrow(() -> new IllegalArgumentException("Following user not found"));

            String title = follower.getNickname() + "님이 팔로우하기 시작했습니다";
            String message = follower.getNickname() + "님이 회원님을 팔로우합니다.";
            String relatedUrl = NotificationType.FOLLOW.getRoute(followerId);

            notificationService.createNotification(following, NotificationType.FOLLOW, title, message, relatedUrl);
        }
    }

    /**
     * Unfollow a user
     */
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * Check if user is following another user
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
    }

    /**
     * Get list of followers with user details
     */
    public List<UserProfileDTO> getFollowers(Long userId) {
        List<UserFollow> follows = userFollowRepository.findByFollowingId(userId);
        return follows.stream()
                .map(follow -> {
                    UserAuth follower = userRepository.findById(follow.getFollowerId()).orElse(null);
                    if (follower == null)
                        return null;
                    return convertToDTO(follower);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Get list of users that the user is following
     */
    public List<UserProfileDTO> getFollowing(Long userId) {
        List<UserFollow> follows = userFollowRepository.findByFollowerId(userId);
        return follows.stream()
                .map(follow -> {
                    UserAuth following = userRepository.findById(follow.getFollowingId()).orElse(null);
                    if (following == null)
                        return null;
                    return convertToDTO(following);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Get mutual follows (users who follow each other)
     */
    public List<UserProfileDTO> getMutualFollows(Long userId) {
        List<Long> mutualIds = userFollowRepository.findMutualFollowIds(userId);
        return mutualIds.stream()
                .map(id -> {
                    UserAuth user = userRepository.findById(id).orElse(null);
                    if (user == null)
                        return null;
                    return convertToDTO(user);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Get follower count
     */
    public long getFollowerCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    /**
     * Get following count
     */
    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    /**
     * Get follow status between two users
     */
    public FollowStatus getFollowStatus(Long currentUserId, Long targetUserId) {
        boolean isFollowing = isFollowing(currentUserId, targetUserId);
        boolean isFollower = isFollowing(targetUserId, currentUserId);
        return new FollowStatus(isFollowing, isFollower);
    }

    /**
     * Convert UserAuth to UserProfileDTO
     */
    private UserProfileDTO convertToDTO(UserAuth user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setNicknameImage(user.getNicknameImage());
        dto.setRunnerGrade(user.getRunnerGrade() != null ? user.getRunnerGrade().name() : "BEGINNER");
        return dto;
    }

    /**
     * Inner class for follow status
     */
    public static class FollowStatus {
        private final boolean isFollowing;
        private final boolean isFollower;

        public FollowStatus(boolean isFollowing, boolean isFollower) {
            this.isFollowing = isFollowing;
            this.isFollower = isFollower;
        }

        public boolean isFollowing() {
            return isFollowing;
        }

        public boolean isFollower() {
            return isFollower;
        }

        public boolean isMutual() {
            return isFollowing && isFollower;
        }
    }
}
