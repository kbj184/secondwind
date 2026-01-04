package com.secondwind.controller;

import com.secondwind.dto.PostDTO;
import com.secondwind.dto.CommentDTO;
import com.secondwind.entity.Post;
import com.secondwind.entity.Comment;
import com.secondwind.entity.BoardCategory;
import com.secondwind.entity.Crew;
import com.secondwind.repository.PostRepository;
import com.secondwind.repository.CommentRepository;
import com.secondwind.repository.UserRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.CrewMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/board")
public class BoardController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;

    public BoardController(PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            CrewRepository crewRepository,
            CrewMemberRepository crewMemberRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
    }

    // 게시글 목록 조회
    @GetMapping("/posts")
    public Page<PostDTO> getPosts(
            @RequestParam BoardCategory category,
            @RequestParam(required = false) Long crewId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (keyword != null && !keyword.trim().isEmpty() && crewId != null) {
            posts = postRepository.searchByCrewId(category, crewId, keyword, pageable);
        } else if (crewId != null) {
            posts = postRepository.findByCategoryAndCrewIdOrderByIsPinnedDescCreatedAtDesc(category, crewId, pageable);
        } else {
            posts = postRepository.findByCategoryOrderByIsPinnedDescCreatedAtDesc(category, pageable);
        }

        return posts.map(this::convertToDTO);
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    @Transactional
    public PostDTO getPost(@PathVariable Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 조회수 증가
        postRepository.incrementViewCount(postId);

        return convertToDTO(post);
    }

    // 게시글 작성
    @PostMapping("/posts")
    public PostDTO createPost(@RequestBody PostDTO postDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 크루 게시판인 경우 멤버십 확인
        if (postDTO.getCategory() == BoardCategory.CREW && postDTO.getCrewId() != null) {
            boolean isMember = crewMemberRepository.existsByCrewIdAndUserIdAndStatus(
                    postDTO.getCrewId(), user.getId(), "APPROVED");
            if (!isMember) {
                throw new RuntimeException("Only crew members can post");
            }
        }

        // isPinned 권한 확인 (크루장만 가능)
        if (postDTO.getIsPinned() != null && postDTO.getIsPinned()) {
            if (postDTO.getCrewId() != null) {
                Crew crew = crewRepository.findById(postDTO.getCrewId())
                        .orElseThrow(() -> new RuntimeException("Crew not found"));
                if (!crew.getCaptainId().equals(user.getId())) {
                    throw new RuntimeException("Only captain can pin posts");
                }
            }
        }

        Post post = new Post();
        post.setCategory(postDTO.getCategory());
        post.setCrewId(postDTO.getCrewId());
        post.setAuthorId(user.getId());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setIsPinned(postDTO.getIsPinned() != null ? postDTO.getIsPinned() : false);

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public PostDTO updatePost(@PathVariable Long postId, @RequestBody PostDTO postDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 권한 확인: 작성자 또는 크루장
        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isCaptain = false;
        if (post.getCrewId() != null) {
            Crew crew = crewRepository.findById(post.getCrewId()).orElse(null);
            if (crew != null) {
                isCaptain = crew.getCaptainId().equals(user.getId());
            }
        }

        if (!isAuthor && !isCaptain) {
            throw new RuntimeException("No permission to edit this post");
        }

        // 수정
        if (postDTO.getTitle() != null)
            post.setTitle(postDTO.getTitle());
        if (postDTO.getContent() != null)
            post.setContent(postDTO.getContent());

        // isPinned는 크루장만 수정 가능
        if (postDTO.getIsPinned() != null && isCaptain) {
            post.setIsPinned(postDTO.getIsPinned());
        }

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 권한 확인
        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isCaptain = false;
        if (post.getCrewId() != null) {
            Crew crew = crewRepository.findById(post.getCrewId()).orElse(null);
            if (crew != null) {
                isCaptain = crew.getCaptainId().equals(user.getId());
            }
        }

        if (!isAuthor && !isCaptain) {
            throw new RuntimeException("No permission to delete this post");
        }

        postRepository.delete(post);
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public List<CommentDTO> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream().map(this::convertCommentToDTO).collect(Collectors.toList());
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    @Transactional
    public CommentDTO createComment(@PathVariable Long postId, @RequestBody CommentDTO commentDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 크루 게시판인 경우 멤버십 확인
        if (post.getCategory() == BoardCategory.CREW && post.getCrewId() != null) {
            boolean isMember = crewMemberRepository.existsByCrewIdAndUserIdAndStatus(
                    post.getCrewId(), user.getId(), "APPROVED");
            if (!isMember) {
                throw new RuntimeException("Only crew members can comment");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(user.getId());
        comment.setContent(commentDTO.getContent());

        Comment savedComment = commentRepository.save(comment);

        // 댓글 수 증가
        postRepository.incrementCommentCount(postId);

        return convertCommentToDTO(savedComment);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    @Transactional
    public void deleteComment(@PathVariable Long commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 권한 확인: 작성자만
        if (!comment.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No permission to delete this comment");
        }

        commentRepository.delete(comment);

        // 댓글 수 감소
        postRepository.decrementCommentCount(comment.getPostId());
    }

    // DTO 변환 헬퍼
    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setCategory(post.getCategory());
        dto.setCrewId(post.getCrewId());
        dto.setAuthorId(post.getAuthorId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setIsPinned(post.getIsPinned());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        // 작성자 정보 추가
        var author = userRepository.findById(post.getAuthorId()).orElse(null);
        if (author != null) {
            dto.setAuthorNickname(author.getNickname());
            dto.setAuthorImage(author.getNicknameImage());
        }

        return dto;
    }

    private CommentDTO convertCommentToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        // 작성자 정보 추가
        var author = userRepository.findById(comment.getAuthorId()).orElse(null);
        if (author != null) {
            dto.setAuthorNickname(author.getNickname());
            dto.setAuthorImage(author.getNicknameImage());
        }

        return dto;
    }
}
