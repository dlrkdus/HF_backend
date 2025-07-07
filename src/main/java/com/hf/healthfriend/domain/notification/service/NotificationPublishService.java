package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.comment.repository.CommentJpaRepository;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.entity.Outbox;
import com.hf.healthfriend.domain.notification.exception.NotificationErrorCode;
import com.hf.healthfriend.domain.notification.exception.NotificationException;
import com.hf.healthfriend.domain.notification.publisher.NotificationPublisher;
import com.hf.healthfriend.domain.notification.repository.OutboxRepository;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublishService {

    private final PostRepository postRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final MemberRepository memberRepository;
    private final OutboxRepository outboxRepository;
    private final NotificationPublisher notificationPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JsonUtils jsonUtils;

    public void publishPostLikeNot(Long memberId, Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.POST_NOT_FOUND));
        String actor = post.getMember().getNickname();
        publish(memberId, NotificationType.ADD_LIKE_TO_POST, actor, postId, "글에 달린 좋아요");
    }

    public void publishCommentLikeNot(Long memberId, Long commentId) {
        Post post = commentJpaRepository.findPostByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.COMMENT_NOT_FOUND));
        String actor = post.getMember().getNickname();
        publish(memberId, NotificationType.ADD_LIKE_TO_COMMENT, actor, post.getPostId(), "댓글에 달린 좋아요");
    }

    public void publishCommentNot(Comment parentComment, Post post, Member writer) {
        if (parentComment == null) {
            publish(post.getMember().getId(), NotificationType.ADD_COMMENT_TO_POST, writer.getNickname(), post.getPostId(), "글에 달린 댓글");
        } else {
            publish(parentComment.getWriter().getId(), NotificationType.ADD_COMMENT_TO_COMMENT, writer.getNickname(), post.getPostId(), "댓글에 달린 댓글");
        }
    }

    public void publishPopularPostNot(Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.POST_NOT_FOUND));
        publish(post.getMember().getId(), NotificationType.POPULAR_POST, null, postId, "인기글 선정");
    }

    public void publishReviewNot(Long reviewerId, Long revieweeId, Long reviewId) {
        Member actor = memberRepository.findByMemberId(reviewerId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.MEMBER_NOT_FOUND));
        publish(revieweeId, NotificationType.MATCH_END_REVIEW, actor.getNickname(), reviewId, "리뷰 작성");
    }

    public void publishMatchRequestNot(Matching matching) {
        publish(matching.getTargetMember().getId(), NotificationType.MATCH_REQUEST, matching.getTargetMember().getNickname(), null, "매칭 신청");
    }

    public void publishMatchAcceptNot(Matching matching) {
        publish(matching.getTargetMember().getId(), NotificationType.MATCH_ACCEPT, matching.getTargetMember().getNickname(), null, "매칭 수락");
    }

    public void publishMatchRejectNot(Matching matching) {
        publish(matching.getTargetMember().getId(), NotificationType.MATCH_REJECT, matching.getTargetMember().getNickname(), null, "매칭 거절");
    }

    private void publish(Long memberId, NotificationType type, String actor, Long targetId, String context) {
        String notificationId = makeNotificationId(type, memberId, targetId);
        NotificationEvent event = NotificationEvent.builder()
                .notificationId(notificationId)
                .memberId(memberId)
                .type(type)
                .actor(actor)
                .targetId(targetId)
                .build();
        saveOutbox(event);
        applicationEventPublisher.publishEvent(event);
        log.info("{} 알림 전송 → type: {}, memberId: {}, targetId: {}, actor: {}", context, type, memberId, targetId, actor);
    }

    public String makeNotificationId(NotificationType type, long memberId, Long targetId) {
        return String.format("%s-actor:%d-target:%d-time:%s",
                type.name(),
                memberId,
                targetId != null ? targetId : 0L,
                System.currentTimeMillis()
        );
    }

    private void saveOutbox(NotificationEvent event){
        Outbox outbox = Outbox.builder()
                .payload(jsonUtils.serialize(event))
                .notificationId(event.getNotificationId())
                .status(MessageStatus.PENDING)
                .type(event.getType())
                .tryCount(0)
                .build();
        outboxRepository.save(outbox);
    }
}