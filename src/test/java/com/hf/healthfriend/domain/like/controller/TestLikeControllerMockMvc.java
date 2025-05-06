package com.hf.healthfriend.domain.like.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.like.dto.PostLikeDto;
import com.hf.healthfriend.domain.like.exception.handler.LikeErrorControllerAdvice;
import com.hf.healthfriend.domain.like.service.LikeService;
import com.hf.healthfriend.domain.member.constant.*;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles({
        "local-dev",
        "secret",
        "constants",
        "priv",
        "no-auth"
})
class TestLikeControllerMockMvc {

    MockMvc mockMvc;

    @Autowired
    LikeService likeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ObjectMapper objectMapper;

    Map<String, Member> sampleMembers;

    Map<String, Post> samplePosts;

    @BeforeEach
    void beforeEach() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new LikeController(this.likeService))
                .setControllerAdvice(new LikeErrorControllerAdvice())
                .build();

        this.sampleMembers = Map.of(
                "member1", SampleEntityGenerator.generateSampleMember("sample1@gmail.com", "nick1", "010-1234-1234"),
                "member2", SampleEntityGenerator.generateSampleMember("sample2@gmail.com", "nick2", "010-1234-1235"),
                "member3", SampleEntityGenerator.generateSampleMember("sample3@gmail.com", "nick3", "010-1234-1236")
        );

        this.samplePosts = Map.of(
                "post1", generateSamplePost(this.sampleMembers.get("member1")),
                "post2", generateSamplePost(this.sampleMembers.get("member2"))
        );

        this.sampleMembers.values().forEach(this.memberRepository::save);
        this.postRepository.saveAll(this.samplePosts.values());
    }

    private Member generateSampleMember(String email, String nickname) {
        Member member = new Member(email);
        member.setNickname(nickname);
        member.setBirthDate(LocalDate.of(1997, 9, 16));
        member.setGender(Gender.MALE);
        member.setIntroduction("");
        member.setFitnessLevel(FitnessLevel.BEGINNER);
        member.setCompanionStyle(CompanionStyle.GROUP);
        member.setFitnessEagerness(FitnessEagerness.EAGER);
        member.setFitnessObjective(FitnessObjective.BULK_UP);
        member.setFitnessKind(FitnessKind.FUNCTIONAL);
        return member;
    }

    private Post generateSamplePost(Member member) {
        return Post.builder()
                .title("sample-post")
                .content("sample-content")
                .category(PostCategory.COUNSELING)
                .member(member)
                .build();
    }

    @DisplayName("POST /hf/posts/{postId}/likes - 좋아요 추가 성공")
    @Test
    void addPostLike_success() throws Exception {
        String responseBodyAsString = this.mockMvc.perform(
                MockMvcRequestBuilders.post("/hf/posts/{postId}/likes", this.samplePosts.get("post1").getPostId())
                        .queryParam("memberId", String.valueOf(this.sampleMembers.get("member3").getId()))
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn().getResponse().getContentAsString();
        ApiBasicResponse<Long> responseBody = this.objectMapper.readValue(responseBodyAsString, new TypeReference<ApiBasicResponse<Long>>() {
        });

        assertThat(responseBody.getContent()).isNotNull();
    }

    @DisplayName("POST /hf/posts/{postId}/likes - 좋아요 중복 추가 시 400 Bad Request")
    @Test
    void addDuplicateLikes_expect400BadRequest() throws Exception {
        this.likeService.addPostLike(this.sampleMembers.get("member1").getId(), this.samplePosts.get("post1").getPostId());

        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/hf/posts/{postId}/likes", this.samplePosts.get("post1").getPostId())
                        .queryParam("memberId", String.valueOf(this.sampleMembers.get("member1").getId()))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @DisplayName("POST /hf/posts/{postId}/likes - 해당 회원이나 글이 존재하지 않을 경우 400 Bad Request")
    @Test
    void addDuplicateLikes_noMemberOrPost_expect400BadRequest() throws Exception {
        Long notExistsPostId = findNotExistsPostId();
        Long notExistsMemberId = findNotExistsMemberId();
        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/hf/posts/{postId}/likes", notExistsPostId)
                        .queryParam("memberId", String.valueOf(notExistsMemberId))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private Long findNotExistsPostId() {
        Random random = new Random();
        long id = random.nextLong();
        Optional<Post> postOp = this.postRepository.findById(id);
        if (postOp.isPresent()) {
            return findNotExistsPostId();
        }
        return id;
    }

    private Long findNotExistsMemberId() {
        Random random = new Random();
        long id = random.nextLong();
        Optional<Member> memberOp = this.memberRepository.findById(id);
        if (memberOp.isPresent()) {
            return findNotExistsPostId();
        }
        return id;
    }

    @DisplayName("GET /hf/posts/{postId}/likes - 회원이 해당 글에 좋아요를 남겼는지 여부 조회")
    @Test
    void doesMemberLikeThePost_success() throws Exception {
        this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/hf/posts/{postId}/likes", this.samplePosts.get("post1").getPostId())
                                .param("memberId", String.valueOf(this.sampleMembers.get("member1").getId()))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(String.format("""
                        {
                            "content": true
                        }
                        """, this.samplePosts.get("post1").getPostId(), this.sampleMembers.get("member1").getId())));
    }

    @DisplayName("GET /hf/posts/{postId}/likes - 특정 글에 남긴 좋아요 조회")
    @Test
    void getLikesInAPost() throws Exception {
        // Given
        this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );
        this.likeService.addPostLike(
                this.sampleMembers.get("member2").getId(),
                this.samplePosts.get("post1").getPostId()
        );
        this.likeService.addPostLike(
                this.sampleMembers.get("member3").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        // Then
        String responseBodyAsString = this.mockMvc.perform(
                MockMvcRequestBuilders.get("/hf/posts/{postId}/likes", this.samplePosts.get("post1").getPostId())
        ).andReturn().getResponse().getContentAsString();

        ApiBasicResponse<List<PostLikeDto>> responseBody =
                this.objectMapper.readValue(responseBodyAsString, new TypeReference<ApiBasicResponse<List<PostLikeDto>>>() {
                });

        assertThat(
                responseBody.getContent()
                        .stream()
                        .map(PostLikeDto::getMemberId)
                        .toList())

                .containsExactlyInAnyOrder(
                        this.sampleMembers.values()
                                .stream()
                                .map(Member::getId)
                                .toArray(Long[]::new)
                );
    }

    @DisplayName("GET /hf/posts/{postId}/likes/count - 특정 글에 남긴 좋아요 개수 조회")
    @Test
    void getLikesCount() throws Exception {
        // Given
        this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );
        this.likeService.addPostLike(
                this.sampleMembers.get("member2").getId(),
                this.samplePosts.get("post1").getPostId()
        );
        this.likeService.addPostLike(
                this.sampleMembers.get("member3").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        // Then
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/hf/posts/{postId}/likes/count", this.samplePosts.get("post1").getPostId())
        ).andExpect(
                MockMvcResultMatchers.content().json("""
                        {
                            "content": 3
                        }
                        """)
        );
    }

//    @DisplayName("GET /hf/members/{memberId}/likes - 특정 회원이 남긴 좋아요 목록 조회")
//    @Test
//    void getLikesOfSpecificMember() throws Exception {
//        // Given
//        this.likeService.addPostLike(
//                this.sampleMembers.get("member1").getId(),
//                this.samplePosts.get("post1").getPostId()
//        );
//        this.likeService.addPostLike(
//                this.sampleMembers.get("member2").getId(),
//                this.samplePosts.get("post1").getPostId()
//        );
//        this.likeService.addPostLike(
//                this.sampleMembers.get("member1").getId(),
//                this.samplePosts.get("post2").getPostId()
//        );
//
//        Long member1Id = this.sampleMembers.get("member1").getId();
//        Long post1Id = this.samplePosts.get("post1").getPostId();
//        Long post2Id = this.samplePosts.get("post2").getPostId();
//
//        // Then
//        this.mockMvc.perform(
//                MockMvcRequestBuilders.get("/hf/members/{memberId}/likes", member1Id)
//        ).andExpect(
//                MockMvcResultMatchers.content().json(String.format("""
//                        {
//                            "content": [
//                                {
//                                    "memberId": %d,
//                                    "postId": %d
//                                },
//                                {
//                                    "memberId": %d,
//                                    "postId": %d
//                                }
//                            ]
//                        }
//                        """, member1Id, post1Id, member1Id, post2Id))
//        );
//    }

    @DisplayName("DELETE /hf/likes/{likeId} - 좋아요 취소")
    @Test
    void cancelLike() throws Exception {
        Long generatedId = this.likeService.addPostLike(
                this.sampleMembers.get("member1").getId(),
                this.samplePosts.get("post1").getPostId()
        );

        this.mockMvc.perform(
                MockMvcRequestBuilders.delete("/hf/likes/{likeId}", generatedId)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> this.likeService.getLike(generatedId));
    }

    @DisplayName("DELETE /hf/likes/{likeId} - 좋아요 취소 - 존재하지 않는 좋아요 취소 시도 때문에 404")
    @Test
    void cancelLike_404NotFoundExpected() throws Exception {
        this.mockMvc.perform(
                MockMvcRequestBuilders.delete("/hf/likes/{likeId}", 15134124L)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}