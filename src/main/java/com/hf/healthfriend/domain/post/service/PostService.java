package com.hf.healthfriend.domain.post.service;

import com.hf.healthfriend.domain.comment.constant.CommentSortType;
import com.hf.healthfriend.domain.comment.dto.CommentDto;
import com.hf.healthfriend.domain.comment.service.CommentService;
import com.hf.healthfriend.domain.like.repository.LikeRepository;
import com.hf.healthfriend.domain.member.constant.FitnessLevel;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.exception.MemberNotFoundException;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.dto.request.PostWriteRequest;
import com.hf.healthfriend.domain.post.dto.response.PostGetResponse;
import com.hf.healthfriend.domain.post.dto.response.PostListObject;
import com.hf.healthfriend.domain.post.dto.response.PostSearchResponse;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.exception.PostErrorCode;
import com.hf.healthfriend.domain.post.exception.PostException;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.global.file.FileUrlResolver;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentService commentService;
    private final LikeRepository likeRepository;
    private final RedissonClient redissonClient;

    private final FileUrlResolver fileUrlResolver;

    public Long save(PostWriteRequest postWriteRequest){
        Long memberId = postWriteRequest.getWriterId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Post post = postWriteRequest.toEntity(member);
        return postRepository.save(post).getPostId();
    }

    public Long update(PostWriteRequest postWriteRequest, Long postId){
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND, HttpStatus.NOT_FOUND,postId + "번 post가 존재하지 않습니다."));
        post.update(postWriteRequest.getTitle(), postWriteRequest.getContent(), PostCategory.valueOf(postWriteRequest.getCategory()));
        return post.getPostId();
    }

    public PostGetResponse get(Long postId, boolean canUpdateViewCount, CommentSortType sortType) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND, HttpStatus.NOT_FOUND, postId + "번 post가 존재하지 않습니다."));

        long viewCount = increaseViewCount(postId,post,canUpdateViewCount);
        List<CommentDto> commentList = commentService.getCommentsOfPost(postId, sortType);

        String imagePath = fileUrlResolver.resolveFileUrl(post.getImagePath());
        String writerProfileImageUrl = fileUrlResolver.resolveFileUrl(post.getMember().getProfileImageUrl());

        return PostGetResponse.of(post, commentList, viewCount, imagePath, writerProfileImageUrl);
    }

    public void delete(Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND, HttpStatus.NOT_FOUND,postId + "번 post가 존재하지 않습니다."));
        post.delete();
        // Redis 에서도 조회수 제거
        String redisKey = "post:viewCount:" + postId;
        redissonClient.getAtomicLong(redisKey).delete();
        likeRepository.deleteLikeByPostId(postId);
    }

    public PostSearchResponse getList(int pageNumber, int size, FitnessLevel fitnessLevel, PostCategory postCategory, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        Long totalPageSize = postRepository.getTotalPageSize(size);
        List<PostListObject> postList = postRepository.getList(fitnessLevel, postCategory, keyword, pageable);
        return PostSearchResponse.builder()
                .postList(postList)
                .totalPageSize(totalPageSize)
                .build();
    }

    public PostSearchResponse getPopularList(int pageNumber, int size, FitnessLevel fitnessLevel, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet("popular_posts");
        List<Long> postIdList = new ArrayList<>( sortedSet.readAll().stream().toList());
        Long totalPageSize = (long) (postIdList.size() / size);
        List<PostListObject> popularPostList = postRepository.getPopularList(postIdList,fitnessLevel,keyword,pageable);
        return PostSearchResponse.builder()
                .postList(popularPostList)
                .totalPageSize(totalPageSize)
                .build();
    }

    private Long increaseViewCount(Long postId, Post post, boolean canUpdateViewCount) {
        String redisKey = "post:viewCount:" + postId;
        RAtomicLong counter = redissonClient.getAtomicLong(redisKey);

        long viewCount = counter.get();

        // 값이 없으면 DB 값으로 초기화 + TTL 6시간
        if (viewCount == 0) {
            viewCount = post.getViewCount();
            counter.set(viewCount);
            counter.expire(Duration.ofHours(6));
            log.info("Redis에 조회수 초기화: postId={}, newViewCount={}", postId, viewCount);
        }

        // 조회수 증가 + TTL 연장
        if (canUpdateViewCount) {
            viewCount = counter.incrementAndGet();
            counter.expire(Duration.ofHours(6));  // 연장
            log.info("조회수 증가: postId={}, newViewCount={}", postId, viewCount);
        }

        return viewCount;
    }
}
