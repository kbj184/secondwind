package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorNickname;
    private String authorImage;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
