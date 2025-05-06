package com.hf.healthfriend.domain.comment.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class CommentCreationRequestDto {

    @NotNull
    private Long writerId;

    @NotBlank
    @Size(max = 500, message = "댓글은 최대 500자까지 입력할 수 있습니다.")
    private String content;

    @Nullable
    private Long parentCommentId;
}
