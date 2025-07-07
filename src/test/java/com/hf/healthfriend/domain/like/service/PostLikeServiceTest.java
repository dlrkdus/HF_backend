package com.hf.healthfriend.domain.like.service;

import com.hf.healthfriend.domain.comment.repository.CommentRepository;
import com.hf.healthfriend.domain.like.dto.PostLikeDto;
import com.hf.healthfriend.domain.like.exception.DuplicatePostLikeException;
import com.hf.healthfriend.domain.like.exception.PostOrMemberNotExistsException;
import com.hf.healthfriend.domain.like.repository.LikeRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles({
        "local-dev",
        "secret",
        "priv",
        "constants"
})
@Transactional
class PostLikeServiceTest {

    @Autowired
    LikeService likeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    Map<String, Member> sampleMembers;

    Map<String, Post> samplePosts;

    @BeforeEach
    void beforeEach() {
        this.sampleMembers = Map.of(
                "member1", SampleEntityGenerator.generateSampleMember("sample1@gmail.com", "nick1")
        );

        this.samplePosts = Map.of(
                "post1", generateSamplePost(this.sampleMembers.get("member1"))
        );

        this.sampleMembers.values().forEach(this.memberRepository::save);
        this.postRepository.saveAll(this.samplePosts.values());
    }

    private Post generateSamplePost(Member member) {
        return Post.builder()
                .title("sample-post")
                .content("sample-content")
                .category(PostCategory.COUNSELING)
                .member(member)
                .build();
    }

    @DisplayName("addPostLike - successfully save")
    @Test
    void addPostLike_success() {
        assertThatNoException()
                .isThrownBy(() -> this.likeService.addPostLike(
                        this.sampleMembers.get("member1").getId(),
                        this.samplePosts.get("post1").getPostId()
                ));
    }

    @DisplayName("addPostLike - 좋아요 취소 후 다시 addPostLike 호출")
    @Test
    void addPostLike_againAfterCancel() {
        Long generatedId = this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        this.likeService.cancelLike(generatedId);

        try {
            Long likeId = this.likeService.addPostLike(
                    this.sampleMembers.get("member1").getId(),
                    this.samplePosts.get("post1").getPostId()
            );

            assertThat(likeId).isEqualTo(generatedId);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @DisplayName("addPostLike - 중복 좋아요 시 예외 발생 - DuplicateLikeException")
    @Test
    void addPostLike_fail() {
        assertThatNoException()
                .isThrownBy(() -> this.likeService.addPostLike(
                        this.sampleMembers.get("member1").getId(),
                        this.samplePosts.get("post1").getPostId()
                ));

        assertThatExceptionOfType(DuplicatePostLikeException.class)
                .isThrownBy(() -> this.likeService.addPostLike(
                        this.sampleMembers.get("member1").getId(),
                        this.samplePosts.get("post1").getPostId()
                ));
    }

    @DisplayName("addPostLike - 존재하지 않는 Post나 Member와 연관관계 매핑을 가질 경우 예외 발생")
    @CsvSource(value = {
            "0:1", "1:0", "1:1"
    }, delimiter = ':')
    @ParameterizedTest
    void addPostLike_fail_postOfPostIdOrMemberOfMemberIdNotExists(int memberIdIncr, int postIdIncr) {
        Long memberId = this.sampleMembers.get("member1").getId();
        Long postId = this.samplePosts.get("post1").getPostId();
        assertThatExceptionOfType(PostOrMemberNotExistsException.class)
                .isThrownBy(() -> this.likeService.addPostLike(memberId + memberIdIncr, postId + + postIdIncr));
    }

    @DisplayName("getLike - success")
    @Test
    void getLike_success() {
        Long autogeneratedLikeId = this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        PostLikeDto result = this.likeService.getLike(autogeneratedLikeId);
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(this.sampleMembers.get("member1").getId());
        assertThat(result.getPostId()).isEqualTo(this.samplePosts.get("post1").getPostId());
    }

    @DisplayName("getLike - 존재하지 않는 Like를 찾으려 할 경우 예외 발생 - NoSuchElementException")
    @Test
    void getLike_noSuchElementException() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> this.likeService.getLike(13513421L))
                .withMessage("존재하지 않는 Like입니다. ID=%d", 13513421L); // TODO: 하드코딩
    }

    @DisplayName("getLikeOfPost - success")
    @Test
    void getLikeOfPost_success() {
        PostLikeDto postLikeDto = new PostLikeDto(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        List<PostLikeDto> result = this.likeService.getLikeOfPost(this.samplePosts.get("post1").getPostId());

        assertThat(result).size().isEqualTo(1);
        assertThat(result.get(0).getPostId()).isEqualTo(this.samplePosts.get("post1").getPostId());
    }

    @DisplayName("getPostLikeOfMember - success")
    @Test
    void getLikeOfMember_success() {
        this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        List<PostLikeDto> result = this.likeService.getPostLikeOfMember(this.sampleMembers.get("member1").getId());

        assertThat(result).size().isEqualTo(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(this.sampleMembers.get("member1").getId());
    }

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    DataSource dataSource;

    @DisplayName("cancelLike - Post 좋아요 취소 - 취소된 좋아요는 가져올 수 없음")
    @Test
    void cancelLike_success() throws SQLException {
        Long generatedId = this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        PostLikeDto like = this.likeService.getLike(generatedId);
        assertThat(like).isNotNull();

        this.likeService.cancelLike(generatedId);

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> this.likeService.getLike(generatedId));

        this.likeRepository.flush();

        // 실제 DB에서 레코드가 삭제된 게 아니라 is_canceled가 true로 세팅된 것 확인

        Connection connection = DataSourceUtils.getConnection(this.dataSource);
        PreparedStatement stmt = connection.prepareStatement("""
                SELECT is_canceled
                FROM likes
                WHERE like_id = ?
                """);
        stmt.setLong(1, generatedId);
        ResultSet rs = stmt.executeQuery();
        assertThat(rs.next()).isTrue();
        boolean canceled = rs.getBoolean("is_canceled");
        assertThat(canceled).isTrue();

        try {
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}