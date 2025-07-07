package com.hf.healthfriend.domain.post.entity;

import com.hf.healthfriend.domain.BaseTimeEntity;
import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.like.entity.Like;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Description;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "post_member_id_idx", columnList = "writer_id")
})
@Description("개별 쓰기와 수정, 읽기만 담당하는 원본 Post 테이블")
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @NotNull
    private String title;

    @NotNull
    @Column(length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    private Long viewCount;

    @Description("삭제한 post")
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    private Long likesCount = 0L;

    @Builder.Default
    private Long commentsCount = 0L;

    private String imagePath;

    public Post(Long postId){
        this.postId = postId;
    }

    public void delete(){
        this.isDeleted=true;
    }

    public void update(String title, String content, PostCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void updateViewCount(long newViewCount) {
        this.viewCount = newViewCount;
    }


}
