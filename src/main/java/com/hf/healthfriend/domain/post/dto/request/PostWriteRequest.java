package com.hf.healthfriend.domain.post.dto.request;

import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.entity.Post;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PostWriteRequest{
    @NotBlank
    String category;
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(min = 3, max = 100, message = "제목은 3자 이상 100자 이하로 입력해주세요.")
    String title;
    @NotBlank(message = "내용을 입력해주세요.")
    String content;
    @NotNull(message="작성자 아이디는 필수값입니다. ")
    Long writerId;
    String imagePath;

    public Post toEntity(Member member){
        PostCategory postCategory = PostCategory.valueOf(category);
        return Post.builder()
                .category(postCategory)
                .title(title)
                .content(content)
                .viewCount(0L)
                .member(member)
                .imagePath(imagePath)
                .build();
    }

}
