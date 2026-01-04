package com.secondwind.repository;

import com.secondwind.entity.Post;
import com.secondwind.entity.BoardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 카테고리별 게시글 조회 (페이징)
    Page<Post> findByCategoryOrderByIsPinnedDescCreatedAtDesc(BoardCategory category, Pageable pageable);

    // 크루별 게시글 조회 (페이징)
    Page<Post> findByCategoryAndCrewIdOrderByIsPinnedDescCreatedAtDesc(
            BoardCategory category, Long crewId, Pageable pageable);

    // 제목+내용 검색 (크루별)
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.crewId = :crewId " +
            "AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
            "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> searchByCrewId(@Param("category") BoardCategory category,
            @Param("crewId") Long crewId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 댓글 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    // 댓글 수 감소
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);
}
