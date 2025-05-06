package com.hf.healthfriend.domain.matching.service;


import com.hf.healthfriend.domain.matching.constant.MatchingFetchType;
import com.hf.healthfriend.domain.matching.constant.MatchingStatusCondition;
import com.hf.healthfriend.domain.matching.dto.request.MatchingRequestDto;
import com.hf.healthfriend.domain.matching.dto.response.MatchingListResponseDto;
import com.hf.healthfriend.domain.matching.exception.OutOfLimitMatchingRequestException;
import com.hf.healthfriend.domain.matching.repository.MatchingRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({
        "secret",
        "no-auth",
        "constants",
        "local-dev",
        "priv"
})
@SpringBootTest
// TODO: 추후 SpringBootTest를 제거하고 더 빠른 유닛 테스트로 리팩토링
@Slf4j
class MatchingServiceConcurrencyTest {

    @Autowired
    MatchingService matchingService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingRepository matchingRepository;

    @Autowired
    EntityManager em;

    @Autowired
    PlatformTransactionManager txManager;

    @DisplayName("requestMatching - 동시성 테스트")
    @Test
    void requestMatching_concurrency() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            log.info("TEST COUNT: {}", i + 1);
            try {
                runSingleTest_requestMatching_concurrency();
            } finally {
                TransactionStatus status = this.txManager.getTransaction(new DefaultTransactionAttribute());
                this.em.createNativeQuery("DELETE FROM matching").executeUpdate();
                this.em.createNativeQuery("DELETE FROM members").executeUpdate();
                this.txManager.commit(status);
                System.out.println("-------------------");
            }
        }
    }

    void runSingleTest_requestMatching_concurrency() throws InterruptedException {
        // Given
        Member sampleRequester =
                SampleEntityGenerator.generateSampleMember("requester@gmail.com", "requester");
        Member sampleTarget = SampleEntityGenerator.generateSampleMember("target@gmail.com", "target");

        this.memberRepository.save(sampleRequester);
        this.memberRepository.save(sampleTarget);

        ExecutorService executorService = Executors.newCachedThreadPool();

        MatchingRequestDto sameDto = MatchingRequestDto.builder()
                .requesterId(sampleRequester.getId())
                .targetId(sampleTarget.getId())
                .meetingPlace("pp")
                .meetingPlaceAddress("add")
                .meetingTime(LocalDateTime.now().plusDays(1))
                .build();

        // When
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    this.matchingService.requestMatching(sameDto);
                } catch (OutOfLimitMatchingRequestException e) {
                    log.info("OutOfLimit");
                }
            });
        }

        // Then
        executorService.shutdown();
        boolean terminatedCorrectly = executorService.awaitTermination(5, TimeUnit.SECONDS);
        log.info("정상 종료?: {}", terminatedCorrectly);

        Page<MatchingListResponseDto> result = this.matchingRepository.findByMemberIdWithConditions(
                sameDto.getRequesterId(),
                MatchingFetchType.ALL,
                MatchingStatusCondition.ALL,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);

        log.info("result size={}", result.getTotalElements());
        log.info("content size={}", result.getContent().size());
        for (MatchingListResponseDto l : result.getContent()) {
            log.info("matchingId={}", l.matchingId());
            log.info("opponent={}\n----------", l.opponentInfo());
        }
    }
}
