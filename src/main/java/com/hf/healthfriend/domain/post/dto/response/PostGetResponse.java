package com.hf.healthfriend.domain.post.dto.response;

import com.hf.healthfriend.domain.comment.dto.CommentDto;
import com.hf.healthfriend.domain.member.domain.Tier;
import com.hf.healthfriend.domain.post.entity.Post;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostGetResponse(
        long postId,
        long writerId,
        String postCategory,
        String writerNickname,
        String writerProfileImageUrl,
        Tier writerTier,
        String title,
        String content,
        String imagePath,
        LocalDateTime createDate,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        List<CommentDto> comments
) {
    public static PostGetResponse of(Post post, List<CommentDto> comments,Long viewCountFromRedis ,String imagePath, String writerProfileImageUrl) {
        return PostGetResponse.builder()
                .postId(post.getPostId())
                .writerId(post.getMember().getId())
                .writerNickname(post.getMember().getNickname())
                .writerProfileImageUrl(writerProfileImageUrl)
                .writerTier(post.getMember().getTier())
                .postCategory(post.getCategory().name())
                .title(post.getTitle())
                .content(post.getContent())
                .imagePath(imagePath)
                .createDate(post.getCreationTime())
                .viewCount(viewCountFromRedis)
                .likeCount(post.getLikesCount())
                .commentCount(post.getCommentsCount())
                .comments(comments)
                .build();
    }
}
