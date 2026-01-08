package com.secondwind.controller;

import com.secondwind.dto.ChatMessageDTO;
import com.secondwind.dto.ChatRoomDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import com.secondwind.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    /**
     * Get or create chat room with another user
     */
    @PostMapping("/room/{userId}")
    public ResponseEntity<?> getOrCreateRoom(@PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Long roomId = chatService.getOrCreateChatRoom(currentUser.getId(), userId);

        // Get other user info
        UserAuth otherUser = userRepository.findById(userId).orElse(null);
        String otherUserNickname = otherUser != null ? otherUser.getNickname() : "사용자";

        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "otherUserNickname", otherUserNickname));
    }

    /**
     * Get all chat rooms for current user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getChatRooms() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<ChatRoomDTO> rooms = chatService.getChatRooms(currentUser.getId());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Send a message
     */
    @PostMapping("/{roomId}/message")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        try {
            ChatMessageDTO messageDTO = chatService.sendMessage(roomId, currentUser.getId(), message);
            return ResponseEntity.ok(messageDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get messages in a room
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Page<ChatMessageDTO> messages = chatService.getMessages(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/{roomId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        chatService.markMessagesAsRead(roomId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        long count = chatService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
