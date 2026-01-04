package com.secondwind.dto;

import com.secondwind.entity.BoardCategory;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostDTO {
    private Long id;
    private BoardCategory category;
    private Long crewId;
    private Long authorId;
    private String authorNickname;
    private String authorImage;
    private String title;
    private String content;
    private Boolean isPinned;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
