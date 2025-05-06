package com.hf.healthfriend.domain.like.service;

import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.like.constant.LikeType;
import com.hf.healthfriend.domain.like.dto.CommentLikeDto;
import com.hf.healthfriend.domain.like.dto.PostLikeDto;
import com.hf.healthfriend.domain.like.entity.Like;
import com.hf.healthfriend.domain.like.exception.CommentOrMemberNotExistsException;
import com.hf.healthfriend.domain.like.exception.DuplicateCommentLikeException;
import com.hf.healthfriend.domain.like.exception.DuplicatePostLikeException;
import com.hf.healthfriend.domain.like.exception.PostOrMemberNotExistsException;
import com.hf.healthfriend.domain.like.repository.LikeRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.notification.service.NotificationPublishService;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final RedissonClient redissonClient;
    private final NotificationPublishService notificationPublishService;
    private final MemberRepository memberRepository;

    /**
     * 좋아요를 추가한다.
     *
     * @param memberId 좋아요를 남기는 회원의 ID. Not null
     * @param postId   좋아요를 남길 글의 ID. Not Null
     * @return 자동 생성된 Like entity의 ID
     * @throws DuplicatePostLikeException DuplicateLikeException 특정 회원이 특정 포스트에 대해 이미 좋아요를 남겼는데 중복해서
     *                                *                                좋아요를 남기려고 할 경우
     */
    public Long addPostLike(Long memberId, Long postId) throws DuplicatePostLikeException {
        Optional<Like> likeOp = this.likeRepository.findByMemberIdAndPostIdIncludingCanceled(memberId, postId);
        if (likeOp.isEmpty()) {
            Like like = new Like(
                    new Member(memberId),
                    Post.builder()
                            .postId(postId)
                            .build(),
                    LikeType.POST
            );
            try {
                Like savedLike = this.likeRepository.save(like);
                postRepository.incrementLikeCount(postId);
                notificationPublishService.publishPostLikeNot(memberId, postId);
                savePopularPost(postId);
                return savedLike.getLikeId();
            } catch (DataIntegrityViolationException e) {
                throw new PostOrMemberNotExistsException(e, memberId, postId);
            }
        }else{

            Like like = likeOp.get();
            if (!like.isCanceled()) {
                throw new DuplicatePostLikeException(postId, memberId);
            }
            like.uncancel();
            postRepository.incrementLikeCount(postId);
            savePopularPost(postId);
            notificationPublishService.publishPostLikeNot(memberId, postId);
            return like.getLikeId();
        }
    }

    /**
     * Comment 도 likeCount를 도입할지 고려해봐야 한다. (아마 해야될듯..)
     */
    public Long addCommentLike(Long memberId, Long commentId) throws DuplicatePostLikeException {
        Optional<Like> likeOp = this.likeRepository.findByMemberIdAndCommentId(memberId, commentId);
        if (likeOp.isEmpty()) {
            Like like = new Like(
                    new Member(memberId),
                    Comment.builder()
                            .commentId(commentId)
                            .build(),
                    LikeType.COMMENT
            );
            try {
                Like savedLike = this.likeRepository.save(like);
                notificationPublishService.publishCommentLikeNot(memberId, commentId);
                return savedLike.getLikeId();
            } catch (DataIntegrityViolationException e) {
                throw new CommentOrMemberNotExistsException(e, memberId, commentId);
            }
        }else {

            Like like = likeOp.get();

            if (!like.isCanceled()) {
                throw new DuplicateCommentLikeException(commentId, memberId);
            }

            like.uncancel();

            notificationPublishService.publishCommentLikeNot(memberId, commentId);

            return like.getLikeId();
        }
    }

    /**
     * likeId를 search key로 하여 단일 Like 정보를 가져온다.
     * 이 Service에 의존성을 가지는 객체에서 사용할 수 있으므로 남겨 놓음.
     *
     * @param likeId 찾고자 하는 Like의 ID
     * @return Like entity의 정보를 가진 Like entity
     * @throws NoSuchElementException likeId를 ID로 하는 Like entity가 없을 경우
     */
    public PostLikeDto getLike(Long likeId) throws NoSuchElementException {
        Like searchedLike = this.likeRepository.findById(likeId).orElseThrow(() ->
                new NoSuchElementException(String.format("존재하지 않는 Like입니다. ID=%d", likeId)));// TODO 하드코딩 고치기
        return PostLikeDto.of(searchedLike);
    }

    /**
     * 특정 회원이 특정 글에 남긴 좋아요 ID를 반환
     *
     * @param memberId 좋아요를 남겼는지 체크할 회원의 ID
     * @param postId 회원이 좋아요를 남겼는지 체크할 Post의 ID
     * @return member가 post에 남긴 좋아요의 ID를 반환. 만약 주어진 member가 주어진 post에 좋아요를 남기지 않았을 경우, null 반환
     */
    public Long getLikeIdOfMemberToPost(Long memberId, Long postId) {
        return this.likeRepository.findByMemberIdAndPostId(memberId, postId)
                .orElse(new Like(null)).getLikeId();
    }

    /**
     * 특정 회원이 특정 댓글에 남긴 좋아요 ID 반환
     *
     * @param memberId 좋아요를 남겼는지 체크할 회원의 ID
     * @param commentId 회원이 좋아요를 남겼는지 체크할 Comment의 ID
     * @return member가 comment에 남긴 좋아요의 ID를 반환. 만약 주어진 member가 주어진 comment에 좋아요를 남기지 않았을 경우, null 반환
     */
    public Long getLikeIdOfMemberToComment(Long memberId, Long commentId) {
        return this.likeRepository.findByMemberIdAndCommentId(memberId, commentId)
                .orElse(new Like(null)).getLikeId();
    }

    public List<PostLikeDto> getLikeOfPost(Long postId) {
        return postEntityListToDtoList(this.likeRepository.findByPostId(postId));
    }

    public List<CommentLikeDto> getLikeOfComment(Long commentId) {
        return commentEntityListToDtoList(this.likeRepository.findByCommentId(commentId));
    }

    public Long getLikeCountOfPost(Long postId) {
        return this.likeRepository.countByPostId(postId);
    }

    public List<PostLikeDto> getPostLikeOfMember(Long memberId) {
        return postEntityListToDtoList(this.likeRepository.findPostLikeByMemberId(memberId));
    }

    private List<PostLikeDto> postEntityListToDtoList(List<Like> entities) {
        return entities.stream().map(PostLikeDto::of).toList();
    }

    private List<CommentLikeDto> commentEntityListToDtoList(List<Like> entities) {
        return entities.stream().map(CommentLikeDto::of).toList();
    }

    public void cancelLike(Long likeIdToCancel) throws NoSuchElementException {
        Like likeEntity = this.likeRepository.findById(likeIdToCancel).orElseThrow(NoSuchElementException::new);

        // TODO: 나중에 좋아요 타입이 늘어날 경우, 각 경우에 맞게 로직 처리
        if (likeEntity.getLikeType() == LikeType.COMMENT) {
            // 해당 댓글에 대해서만 좋아요 취소
            likeEntity.cancel();
            return;
        }

        long postId = likeRepository.findPostIdByLikeId(likeIdToCancel)
                .orElseThrow(NoSuchElementException::new);

        likeEntity.cancel();
        postRepository.decrementLikeCountByLikeId(likeIdToCancel);
        // TODO 동시성 처리 필요
        deletePopularPost(postId);

    }

    public void savePopularPost(Long postId) {
        Long likeCount = this.likeRepository.countByPostId(postId);
        if(likeCount>=5) {
            RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet("popular_posts");
            sortedSet.add(-(double)likeCount, postId);
            log.info("인기글 등록 완료 {}", postId);
            notificationPublishService.publishPopularPostNot(postId);
        }
    }

    public void deletePopularPost(Long postId) {
        Long likeCount = this.likeRepository.countByPostId(postId);
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet("popular_posts");
        if (likeCount<5){
            sortedSet.remove(postId);
            log.info("popularPost sortedSet 제거 완료 ");
        }
        else {
            sortedSet.add(-(double)likeCount, postId);
            log.info("popularPost sortedSet 좋아요 수 감소");
        }
        // 주간 TOP 인기글이니까 일주일 뒤엔 만료
        redissonClient.getKeys().expire("popular_posts", 7, TimeUnit.DAYS); // TTL 설정

    }
}
