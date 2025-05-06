package com.hf.healthfriend.testutil;

import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.member.constant.*;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.review.constants.EvaluationType;
import com.hf.healthfriend.domain.review.entity.Review;
import com.hf.healthfriend.domain.review.entity.ReviewEvaluation;
import com.hf.healthfriend.domain.spec.entity.Spec;

import java.time.LocalDate;
import java.util.List;

public class SampleEntityGenerator {

    public static Member generateSampleMember(String email) {
        return generateSampleMember(email, "샘플닉네임");
    }

    public static Member generateSampleMember(String email, String nickname) {
        return generateSampleMember(email, nickname, "010-1234-1234");
    }

    @Deprecated
    public static Member generateSampleMember(String email, String nickname, String phoneNumber) {
        Member member = new Member(email);
        member.setEmail(email);
        member.setNickname(nickname);
        member.setBirthDate(LocalDate.of(1997, 9, 16));
        member.setName("김샘플");
        member.setCd1("01");
        member.setCd2("110");
        member.setCd3("111");
        member.setGender(Gender.MALE);
        member.setIntroduction("");
        member.setFitnessLevel(FitnessLevel.BEGINNER);
        member.setCompanionStyle(CompanionStyle.GROUP);
        member.setFitnessEagerness(FitnessEagerness.EAGER);
        member.setFitnessObjective(FitnessObjective.BULK_UP);
        member.setFitnessKind(FitnessKind.FUNCTIONAL);
        return member;
    }

    public static Post generateSamplePost(Member writer) {
        return Post.builder()
                .title("Sample Post")
                .content("content")
                .likesCount(10L)
                .viewCount(50L)
                .category(PostCategory.FREE_COMMUNITY)
                .isDeleted(false)
                .member(writer)
                .build();
    }

    public static Spec generateSampleSpec(Member member) {
        return new Spec(
                member,
                LocalDate.of(2005, 3, 5),
                LocalDate.of(2006, 4, 7),
                false,
                "sample-spec",
                "this is sample"
        );
    }

    public static Review generateSampleReview(Matching matching, Member reviewer, Member reviewee) {
        return generateSampleReview(matching, reviewer, reviewee, generateSampleReviewEvaluation());
    }

    public static Review generateSampleReview(Matching matching, Member reviewer, Member reviewee, List<ReviewEvaluation> evaluations) {
        return generateSampleReview(matching, reviewer, reviewee, 3, evaluations);
    }

    public static Review generateSampleReview(Matching matching, Member reviewer, Member reviewee, Integer score) {
        return generateSampleReview(matching, reviewer, reviewee, score, generateSampleReviewEvaluation());
    }

    public static Review generateSampleReview(Matching matching, Member reviewer, Member reviewee, Integer score, List<ReviewEvaluation> evaluations) {
        return new Review(
                reviewer,
                reviewee,
                matching,
                score,
                evaluations
        );
    }

    public static List<ReviewEvaluation> generateSampleReviewEvaluation() {
        return List.of(
                new ReviewEvaluation(EvaluationType.GOOD, 1),
                new ReviewEvaluation(EvaluationType.GOOD, 2),
                new ReviewEvaluation(EvaluationType.NOT_GOOD, 1),
                new ReviewEvaluation(EvaluationType.NOT_GOOD, 2)
        );
    }

    public static Chatroom generateSampleChatroom(Member... participants) {
        return Chatroom.newChatroom(participants);
    }
}
