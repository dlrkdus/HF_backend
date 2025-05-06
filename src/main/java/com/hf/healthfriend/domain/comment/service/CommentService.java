package com.hf.healthfriend.domain.comment.service;

import com.hf.healthfriend.domain.comment.constant.CommentSortType;
import com.hf.healthfriend.domain.comment.dto.CommentDto;
import com.hf.healthfriend.domain.comment.dto.request.CommentCreationRequestDto;
import com.hf.healthfriend.domain.comment.dto.response.CommentCreationResponseDto;
import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.comment.exception.CommentErrorCode;
import com.hf.healthfriend.domain.comment.exception.CommentException;
import com.hf.healthfriend.domain.comment.repository.CommentJpaRepository;
import com.hf.healthfriend.domain.comment.repository.CommentRepository;
import com.hf.healthfriend.domain.comment.repository.dto.CommentUpdateDto;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.exception.MemberNotFoundException;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.notification.service.NotificationPublishService;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.global.file.FileUrlResolver;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final FileUrlResolver fileUrlResolver;
    private final NotificationPublishService notificationPublishService;

    public CommentCreationResponseDto createComment(Long postId, CommentCreationRequestDto requestDto)
            throws DataIntegrityViolationException {

        Comment parentComment = null;
        if (requestDto.getParentCommentId()!=null){
            parentComment = commentJpaRepository.findById(requestDto.getParentCommentId())
                    .orElseThrow(() -> new CommentException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND,HttpStatus.NOT_FOUND,
                            requestDto.getParentCommentId()+"번 댓글은 존재하지 않는 댓글입니다."));
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));

        Member writer = memberRepository.findByMemberId(requestDto.getWriterId())
                .orElseThrow(() -> new CommentException(CommentErrorCode.MEMBER_NOT_EXISTS));

        Comment toSave = Comment.builder()
                .post(new Post(postId))
                .writer(new Member(requestDto.getWriterId()))
                .content(requestDto.getContent())
                .parentComment(parentComment)
                .build();

        Comment newComment = this.commentRepository.save(toSave);
        postRepository.incrementCommentsCount(postId);
        log.info("[Comment Creation] postId={}, commenterId={}", postId, requestDto.getWriterId());

        notificationPublishService.publishCommentNot(parentComment, post, writer);

        return CommentCreationResponseDto.builder()
                .commentId(newComment.getCommentId())
                .postId(newComment.getPost().getPostId())
                .writerId(newComment.getWriter().getId())
                .content(newComment.getContent())
                .creationTime(newComment.getCreationTime())
                .parentCommentId(requestDto.getParentCommentId())
                .build();
    }

    public void deleteComment(Long commentId) throws CommentException {
        if(!commentJpaRepository.existsByCommentIdAndIsDeletedFalse(commentId)) {
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND,
                    commentId+"번 댓글은 존재하지 않습니다.");
        }
        // CTE 쿼리로 하위 댓글 모두 soft delete
        commentJpaRepository.deleteAllReplies(commentId);
        // 부모 댓글 soft delete
        commentJpaRepository.softDeleteById(commentId);
        postRepository.decrementCommentsCountByParentCommentId(commentId);
    }

    public List<CommentDto> getCommentsOfPost(Long postId, CommentSortType sortType) {
        if (!postRepository.existsById(postId)) {
            throw new CommentException(CommentErrorCode.POST_NOT_FOUND,HttpStatus.NOT_FOUND,
                    postId+"번 포스트는 존재하지 않습니다.");
        }

        List<Comment> allComments = commentJpaRepository.findAllCommentsByPostIdWithSorting(postId,sortType);

        Map<Long, CommentDto> commentMap = allComments.stream()
                .map(this::toCommentDto) // Comment 엔티티를 CommentDto로 변환
                .collect(Collectors.toMap(CommentDto::getCommentId, Function.identity()));

        List<CommentDto> topLevelComments = new ArrayList<>();
        for (CommentDto commentDto : commentMap.values()) {
            Long parentCommentId = commentDto.getParentId();
            if (parentCommentId == null) {
                // 최상위 댓글은 리스트에 추가
                topLevelComments.add(commentDto);
            } else {
                // 자식 댓글은 부모의 replies 리스트에 추가
                CommentDto parentCommentDto = commentMap.get(parentCommentId);
                if (parentCommentDto != null) {
                    parentCommentDto.getReplies().add(commentDto);
                }
            }
        }

        return topLevelComments;
    }

    public List<CommentDto> getCommentsOfWriter(Long writerId) {
        if (!this.memberRepository.existsById(writerId)) {
            throw new MemberNotFoundException(writerId, "writerId에 해당하는 Member가 없음");
        }

        return this.commentRepository.findCommentsByWriterId(writerId).stream()
                .map(this::toCommentDto)
                .toList();
    }

    public CommentDto updateComment(Long commentId, CommentUpdateDto updateDto) throws CommentException {
        Comment updatedComment = this.commentRepository.updateComment(commentId, updateDto);
        return toCommentDto(updatedComment);
    }

    private CommentDto toCommentDto(Comment comment) {
        String writerProfileUrl = fileUrlResolver.resolveFileUrl(comment.getWriter().getProfileImageUrl());
        String parentWriterName = comment.getParentComment() != null ? comment.getParentComment().getWriter().getName() : null;
        Long parentId = comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null;
        List<CommentDto> replies = new ArrayList<>();
        return CommentDto.of(comment, writerProfileUrl, parentWriterName, parentId, replies);
    }
}
