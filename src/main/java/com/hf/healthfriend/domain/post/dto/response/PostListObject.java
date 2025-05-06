package com.hf.healthfriend.domain.post.dto.response;

import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.global.file.FileUrlResolver;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostListObject(
        long postId,
        String category,
        String title,
        String content,
        LocalDateTime creationTime,
        long viewCount,
        long likeCount,
        long commentCount,
        String fitnessLevel,
        String writerProfileImageUrl
) {
    public static PostListObject of(Post post, String content, FileUrlResolver fileUrlResolver, long viewCountFromRedis) {
        return PostListObject.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .category(post.getCategory().name())
                .viewCount(viewCountFromRedis)
                .creationTime(post.getCreationTime())
                .content(content)
                .fitnessLevel(post.getMember().getFitnessLevel().name())
                .likeCount(post.getLikesCount())
                .commentCount(post.getCommentsCount())
                .writerProfileImageUrl(fileUrlResolver.resolveFileUrl(post.getMember().getProfileImageUrl()))
                .build();
    }
}
