package com.hf.healthfriend.domain.post.repository.querydsl;

import com.hf.healthfriend.domain.member.constant.FitnessLevel;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.dto.response.PostListObject;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.entity.QPost;
import com.hf.healthfriend.global.file.FileUrlResolver;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final RedissonClient redissonClient;
    private final FileUrlResolver fileUrlResolver;
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;

    @Override
    public List<PostListObject> getList(FitnessLevel fitnessLevel, PostCategory postCategory, String keyword, Pageable pageable) {
        OrderSpecifier<?> orderSpecifier = new OrderSpecifier<>(Order.DESC, post.creationTime);
        BooleanBuilder builder = filter(fitnessLevel, postCategory, keyword);

        List<Post> posts = queryFactory
                .selectFrom(post)
                .where(builder)
                .groupBy(post)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return convertPostsToDto(posts, keyword);
    }

    @Override
    public List<PostListObject> getPopularList(List<Long> postIdList, FitnessLevel fitnessLevel, String keyword, Pageable pageable) {
        BooleanBuilder builder = filter(fitnessLevel, null, keyword);
        OrderSpecifier<?>[] orderSpecifier = new OrderSpecifier<?>[]{
                new OrderSpecifier<>(Order.DESC, post.likesCount),
                new OrderSpecifier<>(Order.DESC, post.creationTime)
        };

        List<Post> posts = queryFactory
                .selectFrom(post)
                .where(post.postId.in(postIdList).and(builder))
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return convertPostsToDto(posts, keyword);
    }

    @Override
    public Long getSearchedPostListSize(FitnessLevel fitnessLevel, PostCategory postCategory, String keyword) {
        BooleanBuilder builder = filter(fitnessLevel, postCategory, keyword);
        return queryFactory
                .select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();
    }

    public BooleanBuilder filter(FitnessLevel fitnessLevel, PostCategory postCategory, String keyword) {
        // 조건을 동적으로 추가하기 위한 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.isDeleted.eq(false));
        if (fitnessLevel != null) {
            builder.and(post.member.fitnessLevel.eq(fitnessLevel));
        }
        if (postCategory != null) {
            builder.and(post.category.eq(postCategory));
        }
        if (keyword != null) {
            builder.and(Expressions.booleanTemplate(
                    "function('match_against', {0}, {1}, {2}) > 0",
                    post.title,
                    post.content,
                    keyword
            ));
        }
        return builder;
    }

    public String getSentenceContainKeyword(String keyword, String content){
        String[] sentences = content.split("(?<=[.!?])");
        for (String sentence : sentences) {
            if (sentence.contains(keyword)) {
                return sentence.trim();
            }
        }
        return null;
    }

    @Override
    public Long getTotalPageSize(int size){
        Long totalPageSize = queryFactory
                .select(post.count())
                .from(post)
                .where(post.isDeleted.eq(false))
                .fetchOne();
        if (totalPageSize == null) return 0L;
        return (long) Math.ceil((double) totalPageSize / size);
    }

    private Map<Long, Long> getViewCountsFromRedis(List<Post> posts) {
        Map<Long, Long> viewCountsFromRedis = new HashMap<>();
        // getBuckets()로 키를 한 번에 조회해와 네트워크 I/O를 줄임
        Map<String, Long> redisValues = redissonClient.getBuckets().get(posts.stream()
                .map(post -> "post:viewCount:" + post.getPostId())
                .toArray(String[]::new));

        for (Post post : posts) {
            viewCountsFromRedis.put(post.getPostId(), redisValues.getOrDefault("post:viewCount:" + post.getPostId(), 0L));
        }

        return viewCountsFromRedis;
    }

    private List<PostListObject> convertPostsToDto(List<Post> posts, String keyword) {
        Map<Long, Long> viewCounts = getViewCountsFromRedis(posts);

        return posts.stream().map(post -> {
            String content = post.getContent();
            if (keyword != null) {
                String sentence = getSentenceContainKeyword(keyword, post.getContent());
                content = (sentence != null) ? sentence : content;
            }
            return PostListObject.of(post, content, fileUrlResolver, viewCounts.getOrDefault(post.getPostId(), 0L));
        }).toList();
    }
}
