package com.hf.healthfriend.domain.post.service;
import com.hf.healthfriend.domain.comment.repository.CommentJpaRepository;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentJpaRepository commentJpaRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void testGetList() throws Exception {
        // Given
//        ArrayList<Post> posts = new ArrayList<>();
//        for (int i = 1; i <= 20; i++) {
//            Member writer = new Member("1212","1@naver.com","1111");
//            writer.setFitnessLevel(FitnessLevel.ADVANCED);
//            Post post = Post.builder()
//                    .postId((long) i)
//                    .title("Post " + i)
//                    .category(PostCategory.COUNSELING)
//                    .viewCount((long) (i * 10))
//                    .content("Content " + i)
//                    .member(writer)
//                    .build();
//            Comment comment = Comment.builder()
//                    .post(post)
//                    .writer(writer)
//                    .content("comment"+i)
//                    .build();
//            post.getComments().add(comment);
//
//            // 리플렉션을 사용해 creationTime 필드를 설정
//            Field creationTimeField = Post.class.getSuperclass().getDeclaredField("creationTime");
//            creationTimeField.setAccessible(true);
//            creationTimeField.set(post, LocalDateTime.now().minusDays(i));
//
//            posts.add(post);
//        }
//
//        // 페이지당 10개씩, 두 번째 페이지(인덱스 1) 요청
//        Pageable pageable = PageRequest.of(1, 10, Sort.by("creationTime").descending());
//        Page<Post> postPage = new PageImpl<>(posts.subList(10, 20), pageable, posts.size());
//
//        // Mock 설정: postRepository.findAll(pageable) 호출 시 postPage를 반환
//        when(postRepository.findAll(pageable)).thenReturn(postPage);
//        when(commentJpaRepository.countByPost_PostId(anyLong())).thenReturn(1L);
//
//        // When
//        List<PostListObject> result = postService.getList(2);
//
//        // Then
//        assertEquals(10, result.size()); // 두 번째 페이지의 포스트 10개가 반환되는지 확인
//        //두 번째 페이지 첫번째 포스트 값 검증
//        assertEquals("Post 11", result.get(0).title());
//        assertEquals(110, result.get(0).viewCount());
//        assertEquals("Content 11", result.get(0).content());
//
//        // 두 번째 페이지의 모든 포스트 출력
//        System.out.println("===== 2nd Page Posts =====");
//        result.forEach(System.out::println);

    }
}
