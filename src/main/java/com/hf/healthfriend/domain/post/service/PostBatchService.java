package com.hf.healthfriend.domain.post.service;


import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostBatchService {

    private final RedissonClient redissonClient;
    private final PostRepository postRepository;

    private static final String VIEW_COUNT_PREFIX = "post:viewCount:";

    @Transactional
    @Scheduled(fixedRate = 1000*60*60*24) // 24시간 주기
    public void syncVIewCountsToDB(){
        log.info("Redis 조회수를 DB로 동기화 시작");

        // 1. Redis 에 저장된 조회수 key 찾기
        RKeys keys = redissonClient.getKeys();
        Iterable<String> redisKeysIterable = keys.getKeysByPattern(VIEW_COUNT_PREFIX + "*");
        Set<String> redisKeys = StreamSupport.stream(redisKeysIterable.spliterator(), false)
                .collect(Collectors.toSet());

        if (redisKeys.isEmpty()) {
            log.info("저장된 조회수가 없어 동기화를 종료합니다.");
        }else{
            // Redis 에서 모든 조회수를 가져와서 postId 별로 매핑
            Map<Long, Long> viewCounts = redisKeys.stream()
                    .collect(Collectors.toMap(
                            key -> Long.parseLong(key.replace(VIEW_COUNT_PREFIX, "")), // postId 추출
                            key -> redissonClient.getAtomicLong(key).get() // 조회수 가져오기
                    ));

            // DB 에서 해당 postId 목록 가져오기
            List<Post> posts = postRepository.findAllById(viewCounts.keySet());

            for (Post post : posts) {
                Long redisViewCount = viewCounts.get(post.getPostId());

                if (redisViewCount != null && redisViewCount > post.getViewCount()) {
                    log.info("postId={} 조회수 업데이트: DB={}, Redis={}",
                            post.getPostId(), post.getViewCount(), redisViewCount);
                    post.updateViewCount(redisViewCount);
                }
            }

            postRepository.saveAll(posts); // 일괄 저장

            // 동기화 후 Redis 에서 조회수 초기화
            redisKeys.forEach(key -> redissonClient.getAtomicLong(key).delete());

            log.info("Redis 조회수를 DB로 동기화 완료");
        }
    }
}
