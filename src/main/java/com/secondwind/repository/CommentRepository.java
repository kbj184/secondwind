package com.secondwind.repository;

import com.secondwind.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 조회 (최신순)
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 게시글별 댓글 수 조회
    long countByPostId(Long postId);
}
