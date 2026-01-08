package com.secondwind.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdDate;
}
