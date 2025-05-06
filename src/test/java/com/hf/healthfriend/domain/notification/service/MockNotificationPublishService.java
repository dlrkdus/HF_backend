package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.comment.repository.CommentJpaRepository;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.notification.publisher.NotificationPublisher;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("mock-notification")
@Primary
public class MockNotificationPublishService extends NotificationPublishService {

    public MockNotificationPublishService(PostRepository postRepository,
                                          CommentJpaRepository commentJpaRepository,
                                          MemberRepository memberRepository,
                                          NotificationPublisher notificationPublisher) {
        super(postRepository, commentJpaRepository, memberRepository, notificationPublisher);
    }

    @Override
    public void publishPostLikeNot(Long memberId, Long postId) {
        log.info("publishPostLikeNot");
    }

    @Override
    public void publishCommentLikeNot(Long memberId, Long commentId) {
        log.info("publishCommentLikeNot");
    }

    @Override
    public void publishCommentNot(Comment parentComment, Post post, Member writer) {
        log.info("publishCommentNot");
    }

    @Override
    public void publishPopularPostNot(Long postId) {
        log.info("publishPopularPostNot");
    }

    @Override
    public void publishReviewNot(Long reviewerId, Long revieweeId, Long reviewId) {
        log.info("publishReviewNot");
    }

    @Override
    public void publishMatchRequestNot(Matching matching) {
        log.info("publishMatchRequestNot");
    }

    @Override
    public void publishMatchAcceptNot(Matching matching) {
        log.info("publishMatchAcceptNot");
    }

    @Override
    public void publishMatchRejectNot(Matching matching) {
        log.info("publishMatchRejectNot");
    }

    @Override
    public void publishMatchPunkNot(Matching matching) {
        log.info("publishMatchPunkNot");
    }
}
