package com.hf.healthfriend.domain.comment.repository;

import com.hf.healthfriend.domain.comment.constant.CommentSortType;
import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.comment.repository.querydsl.CommentCustomRepositoryImpl;
import com.hf.healthfriend.domain.like.constant.LikeType;
import com.hf.healthfriend.domain.like.entity.Like;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.global.file.FileUrlResolver;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class CommentSortTest {

    @MockBean
    private FileUrlResolver fileUrlResolver;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentJpaRepository commentRepository;

    @Autowired
    private CommentCustomRepositoryImpl commentCustomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Post testPost;
    private Member testMember;

    @BeforeEach
    public void setUp() {
        testMember = SampleEntityGenerator.generateSampleMember("sample@post.writer", "testMember");

        memberRepository.save(testMember);
        testPost = Post.builder()
                .content("content")
                .title("title")
                .member(testMember)
                .build();
        postRepository.save(testPost);
        entityManager.flush();
        entityManager.clear();

        for(int i=1; i<=5; i++){
            commentRepository.save(Comment.builder()
                            .post(testPost)
                            .commentId((long)i)
                            .content("Comment "+i)
                            .isDeleted(false)
                            .writer(testMember)
                    .build());
        }
        entityManager.flush();
        entityManager.clear();
    }

    @DisplayName("댓글 목록을 최신순으로 정렬 ")
    @Test
    void sortByCreationDate(){
        //Given
        Long postId = testPost.getPostId();

        //When
        List<Comment> comments = commentCustomRepository.findAllCommentsByPostIdWithSorting(postId, CommentSortType.LATEST);

        //Then
        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getContent()).isEqualTo("Comment 5");
    }

    @DisplayName("댓글 목록을 공감순으로 정렬")
    @Test
    void sortByLikeCount(){
        //Given
        Long postId = testPost.getPostId();
        Comment mostLikedComment = commentRepository.findAll().get(0);
        for (int i = 0; i < 3; i++) {
            //좋아요 3개 추가
            mostLikedComment.getLikes().add(new Like(
                    testMember, mostLikedComment, LikeType.COMMENT));
        }
        commentRepository.save(mostLikedComment);

        //When
        List<Comment> comments = commentCustomRepository.findAllCommentsByPostIdWithSorting(postId, CommentSortType.MOST_LIKES);

        //Then
        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getContent()).isEqualTo(mostLikedComment.getContent()); // 좋아요가 가장 많은 댓글이 첫 번째

    }


}
