package com.secondwind.repository;

import com.secondwind.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Find chat room between two users
    Optional<ChatRoom> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    // Find all chat rooms for a user (either as user1 or user2)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1Id = :userId OR cr.user2Id = :userId ORDER BY cr.lastMessageDate DESC")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}
