package com.hf.healthfriend.domain.review.repository;

import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.matching.repository.MatchingRepository;
import com.hf.healthfriend.domain.member.constant.FitnessLevel;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.review.constants.EvaluationType;
import com.hf.healthfriend.domain.review.entity.Review;
import com.hf.healthfriend.domain.review.entity.ReviewEvaluation;
import com.hf.healthfriend.domain.review.repository.dto.RevieweeStatisticsQueryResultDto;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@Import({
        TestConfig.class,
        MysqlTestcontainerConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestReviewRepository {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingRepository matchingRepository;

    Member sampleAdvanced;
    Member sampleBeginner1;
    Member sampleBeginner2;
    Matching sampleMatching1;
    Matching sampleMatching2;
    Matching sampleMatching3;

    @BeforeEach
    void beforeEach() {
        Member advanced = SampleEntityGenerator.generateSampleMember("advanced@gmail.com", "nick1");
        advanced.setFitnessLevel(FitnessLevel.ADVANCED);
        Member beginner1 = SampleEntityGenerator.generateSampleMember("beginner1@gmail.com", "nick2");
        beginner1.setFitnessLevel(FitnessLevel.BEGINNER);
        Member beginner2 = SampleEntityGenerator.generateSampleMember("beginner2@gmail.com", "nick3");
        beginner2.setFitnessLevel(FitnessLevel.BEGINNER);
        this.sampleAdvanced = this.memberRepository.save(advanced);
        this.sampleBeginner1 = this.memberRepository.save(beginner1);
        this.sampleBeginner2 = this.memberRepository.save(beginner2);

        Matching matching1 = new Matching(beginner1, advanced, "스포애니", "서울시 영등포구 당산역", LocalDateTime.now().plusDays(1));
        this.sampleMatching1 = this.matchingRepository.save(matching1);

        Matching matching2 = new Matching(beginner2, advanced, "에이블짐", "서울시 영등포구 당산역", LocalDateTime.now().plusDays(1));
        this.sampleMatching2 = this.matchingRepository.save(matching2);

        Matching matching3 = new Matching(beginner1, beginner2, "aa", "aa", LocalDateTime.now().plusDays(1));
        this.sampleMatching3 = this.matchingRepository.save(matching3);
    }

    @DisplayName("getRevieweeStatistics - success")
    @Test
    void findByRevieweeId_success() {
        // Given
        Review review1 = SampleEntityGenerator.generateSampleReview(
                this.sampleMatching1,
                this.sampleBeginner1,
                this.sampleAdvanced,
                4,
                List.of(
                        new ReviewEvaluation(EvaluationType.GOOD, 1),
                        new ReviewEvaluation(EvaluationType.GOOD, 2),
                        new ReviewEvaluation(EvaluationType.NOT_GOOD, 1)
                ));
        this.reviewRepository.save(review1);
        Review review2 = SampleEntityGenerator.generateSampleReview(
                this.sampleMatching2,
                this.sampleBeginner2,
                this.sampleAdvanced,
                List.of(
                        new ReviewEvaluation(EvaluationType.GOOD, 1),
                        new ReviewEvaluation(EvaluationType.NOT_GOOD, 1)
                ));
        this.reviewRepository.save(review2);

        // 이 데이터는 이 테스트 쿼리 결과에 반영되지 않음. 이 데이터를 예상대로 포함하지 않는가 체크
        Review irrelevantReview = SampleEntityGenerator.generateSampleReview(
                this.sampleMatching3, this.sampleBeginner1, this.sampleBeginner2, 5
        );
        this.reviewRepository.save(irrelevantReview);

        // When
        List<RevieweeStatisticsQueryResultDto> result = this.reviewRepository.getRevieweeStatistics(this.sampleAdvanced.getId());

        // log for debug
        for (RevieweeStatisticsQueryResultDto mapping : result) {
            log.info("mapping.getEvaluationType={}, mapping.getEvaluationDetailId={}, mapping.getEvaluationDetailCount={}",
                    mapping.getEvaluationType(), mapping.getEvaluationDetailId(), mapping.getEvaluationDetailCount());
            log.info("evaluationType={}", mapping.getEvaluationType());
        }

        // Then
        // Expected:
        //     - GOOD 1 count: 2
        //     - GOOD 2 count: 1
        //     - NOT_GOOD 1 count: 2

        // GOOD 1 개수는 2개
        List<Long> onlyGood1Count = result.stream()
                .filter((r) -> r.getEvaluationType() == EvaluationType.GOOD)
                .filter((r) -> r.getEvaluationDetailId() == 1)
                .map(RevieweeStatisticsQueryResultDto::getEvaluationDetailCount)
                .toList();
        assertThat(onlyGood1Count).size().isEqualTo(1);
        assertThat(onlyGood1Count.get(0)).isEqualTo(2);

        // GOOD 2 개수는 1개
        List<Long> onlyGood2Count = result.stream()
                .filter((r) -> r.getEvaluationType() == EvaluationType.GOOD)
                .filter((r) -> r.getEvaluationDetailId() == 2)
                .map(RevieweeStatisticsQueryResultDto::getEvaluationDetailCount)
                .toList();
        assertThat(onlyGood2Count).size().isEqualTo(1);
        assertThat(onlyGood2Count.get(0)).isEqualTo(1);

        // NOT_GOOD 1 개수는 2개
        List<Long> onlyNotGood1Count = result.stream()
                .filter((r) -> r.getEvaluationType() == EvaluationType.NOT_GOOD)
                .filter((r) -> r.getEvaluationDetailId() == 1)
                .map(RevieweeStatisticsQueryResultDto::getEvaluationDetailCount)
                .toList();
        assertThat(onlyNotGood1Count).size().isEqualTo(1);
        assertThat(onlyNotGood1Count.get(0)).isEqualTo(2);

        // GOOD-1, GOOD-2, NOT_GOOD-1 외의 다른 평가는 없어야 함
        for (RevieweeStatisticsQueryResultDto mapping : result) {
            EvaluationType evaluationType = mapping.getEvaluationType();
            Integer evaluationDetailId = mapping.getEvaluationDetailId();
            if (!(evaluationType == EvaluationType.GOOD && evaluationDetailId == 1
                    || evaluationType == EvaluationType.GOOD && evaluationDetailId == 2
                    || evaluationType == EvaluationType.NOT_GOOD && evaluationDetailId == 1)) {
                Assertions.fail("mapping.evaluationType=" + mapping.getEvaluationType()
                        + ", mapping.evaluationDetailId=" + mapping.getEvaluationDetailId());
            }
        }
    }

    @DisplayName("calculateAverageScoreByRevieweeId - success")
    @Test
    void calculateAverageScoreByRevieweeId_success() {
        // Given
        Review review1 = SampleEntityGenerator.generateSampleReview(this.sampleMatching1, this.sampleBeginner1, this.sampleAdvanced, 3);
        review1 = this.reviewRepository.save(review1);
        Review review2 = SampleEntityGenerator.generateSampleReview(this.sampleMatching2, this.sampleBeginner2, this.sampleAdvanced, 4);
        review2 = this.reviewRepository.save(review2);

        // When
        double result = this.reviewRepository.calculateAverageScoreByRevieweeId(this.sampleAdvanced.getId());

        log.info("result={}", result);

        // Then
        assertThat(result).isEqualTo(((double) review1.getScore() + review2.getScore()) / 2);
    }

    @DisplayName("existsByMatchingIdAndReviewerId - return true")
    @Test
    void existsByMatchingIdAndReviewerId_returnTrue() {
        // Given
        Review review1 = SampleEntityGenerator.generateSampleReview(this.sampleMatching1, this.sampleBeginner1, this.sampleAdvanced, 3);
        review1 = this.reviewRepository.save(review1);

        // When
        boolean result = this.reviewRepository.existsByMatchingIdAndReviewerId(this.sampleMatching1.getMatchingId(),
                this.sampleBeginner1.getId());

        // Then
        assertThat(result).isTrue();
    }

    @DisplayName("existsByMatchingIdAndReviewerId - return false")
    @Test
    void existsByMatchingIdAndReviewerId_returnFalse() {
        // Given
        // No review exists

        // When
        boolean result = this.reviewRepository.existsByMatchingIdAndReviewerId(this.sampleMatching1.getMatchingId(),
                this.sampleBeginner1.getId());

        // Then
        assertThat(result).isFalse();
    }
}