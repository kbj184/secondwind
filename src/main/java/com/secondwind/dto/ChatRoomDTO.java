package com.secondwind.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatRoomDTO {
    private Long roomId;
    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserImage;
    private String lastMessage;
    private LocalDateTime lastMessageDate;
    private long unreadCount;
}
