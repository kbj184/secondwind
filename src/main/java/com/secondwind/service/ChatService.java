package com.secondwind.service;

import com.secondwind.dto.ChatMessageDTO;
import com.secondwind.dto.ChatRoomDTO;
import com.secondwind.entity.*;
import com.secondwind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    /**
     * Get or create chat room between two users
     */
    @Transactional
    public Long getOrCreateChatRoom(Long currentUserId, Long otherUserId) {
        // Ensure user1Id < user2Id for consistency
        Long user1Id = Math.min(currentUserId, otherUserId);
        Long user2Id = Math.max(currentUserId, otherUserId);

        ChatRoom room = chatRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .user1Id(user1Id)
                            .user2Id(user2Id)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return room.getId();
    }

    /**
     * Send a message
     */
    @Transactional
    public ChatMessageDTO sendMessage(Long roomId, Long senderId, String messageText) {
        // Verify room exists
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        // Create message
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .message(messageText)
                .isRead(false)
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update room's last message date
        room.setLastMessageDate(savedMessage.getCreatedDate());
        chatRoomRepository.save(room);

        // Determine recipient
        Long recipientId = room.getUser1Id().equals(senderId) ? room.getUser2Id() : room.getUser1Id();

        // Send FCM notification
        try {
            UserAuth sender = userRepository.findById(senderId).orElse(null);
            if (sender != null) {
                String title = sender.getNickname() + "님의 메시지";
                String preview = messageText.length() > 50 ? messageText.substring(0, 50) + "..." : messageText;

                fcmService.sendToUser(
                        recipientId,
                        title,
                        preview,
                        NotificationType.CHAT_MESSAGE,
                        Map.of("roomId", roomId.toString()));
            }
        } catch (Exception e) {
            System.err.println("Failed to send chat notification: " + e.getMessage());
        }

        return convertToMessageDTO(savedMessage);
    }

    /**
     * Get messages in a room (paginated)
     */
    public Page<ChatMessageDTO> getMessages(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedDateAsc(roomId, pageable);
        return messages.map(this::convertToMessageDTO);
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        chatMessageRepository.markMessagesAsRead(roomId, userId);
    }

    /**
     * Get all chat rooms for a user
     */
    public List<ChatRoomDTO> getChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
        List<ChatRoomDTO> roomDTOs = new ArrayList<>();

        for (ChatRoom room : rooms) {
            // Determine other user
            Long otherUserId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
            UserAuth otherUser = userRepository.findById(otherUserId).orElse(null);

            if (otherUser == null)
                continue;

            // Get last message
            ChatMessage lastMessage = chatMessageRepository.findTop1ByRoomIdOrderByCreatedDateDesc(room.getId())
                    .orElse(null);

            // Get unread count
            long unreadCount = chatMessageRepository.countUnreadMessages(room.getId(), userId);

            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setRoomId(room.getId());
            dto.setOtherUserId(otherUserId);
            dto.setOtherUserNickname(otherUser.getNickname());
            dto.setOtherUserImage(otherUser.getNicknameImage());
            dto.setLastMessage(lastMessage != null ? lastMessage.getMessage() : "");
            dto.setLastMessageDate(room.getLastMessageDate());
            dto.setUnreadCount(unreadCount);

            roomDTOs.add(dto);
        }

        return roomDTOs;
    }

    /**
     * Get total unread message count
     */
    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countTotalUnreadMessages(userId);
    }

    /**
     * Convert ChatMessage to DTO
     */
    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setMessage(message.getMessage());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedDate(message.getCreatedDate());
        return dto;
    }
}
