package com.secondwind.repository;

import com.secondwind.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get messages in a room (paginated, chronological order)
    Page<ChatMessage> findByRoomIdOrderByCreatedDateAsc(Long roomId, Pageable pageable);

    // Get last message in a room
    Optional<ChatMessage> findTop1ByRoomIdOrderByCreatedDateDesc(Long roomId);

    // Count unread messages in a room for a specific user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.roomId = :roomId AND m.senderId != :userId AND m.isRead = false")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // Count total unread messages for a user across all rooms
    @Query("SELECT COUNT(m) FROM ChatMessage m JOIN ChatRoom cr ON m.roomId = cr.id " +
            "WHERE (cr.user1Id = :userId OR cr.user2Id = :userId) AND m.senderId != :userId AND m.isRead = false")
    long countTotalUnreadMessages(@Param("userId") Long userId);

    // Mark all messages in a room as read
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.roomId = :roomId AND m.senderId != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
