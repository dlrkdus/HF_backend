package com.hf.healthfriend.domain.comment.dto;

import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.member.domain.Tier;
import java.util.List;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class CommentDto {
    private Long commentId;
    private Long postId;
    private Long writerId;
    private String writerNickname;
    private Tier writerTier;
    private String writerProfileImageUrl;
    private String content;
    private LocalDateTime creationTime;
    private Long parentId;
    private String parentWriterName;
    private List<CommentDto> replies;

    public static CommentDto of(Comment comment, String writerProfileUrl, String parentWriterName, Long parentId, List<CommentDto> replies) {
        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPost().getPostId())
                .writerId(comment.getWriter().getId())
                .writerNickname(comment.getWriter().getNickname())
                .writerTier(comment.getWriter().getTier())
                .writerProfileImageUrl(writerProfileUrl)
                .content(comment.getContent())
                .creationTime(comment.getCreationTime())
                .parentId(parentId)
                .parentWriterName(parentWriterName)
                .replies(replies)
                .build();
    }
}
