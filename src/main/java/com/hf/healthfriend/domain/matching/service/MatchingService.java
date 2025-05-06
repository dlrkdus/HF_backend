package com.hf.healthfriend.domain.matching.service;

import com.hf.healthfriend.domain.matching.constant.MatchingFetchType;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;
import com.hf.healthfriend.domain.matching.constant.MatchingStatusCondition;
import com.hf.healthfriend.domain.matching.dto.MatchingResponseDto;
import com.hf.healthfriend.domain.matching.dto.request.MatchingRequestDto;
import com.hf.healthfriend.domain.matching.dto.response.MatchingListResponseDto;
import com.hf.healthfriend.domain.matching.dto.response.PageResponseDto;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.matching.exception.OutOfLimitMatchingRequestException;
import com.hf.healthfriend.domain.matching.repository.MatchingRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.exception.MemberNotFoundException;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.global.concurrency.SynchronizedOperation;
import com.hf.healthfriend.domain.notification.service.NotificationPublishService;
import com.hf.healthfriend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
    private static final Comparator<Matching> MATCHING_LIST_SORT_COMPARATOR = (d1, d2) -> {
        LocalDateTime creationTime1 = d1.getMeetingTime();
        LocalDateTime creationTime2 = d2.getMeetingTime();
        if (creationTime1.isAfter(creationTime2)) {
            return -1;
        } else if (creationTime1.equals(creationTime2)) {
            return 0;
        } else {
            return 1;
        }
    };

    private final MatchingRepository matchingRepository;
    private final MemberRepository memberRepository;
    private final NotificationPublishService notificationPublishService;

    @SynchronizedOperation
    public Long requestMatching(MatchingRequestDto requestDto) {
        if (this.matchingRepository.existsDuplicateMatchingRequest(requestDto.getRequesterId(), LocalDate.now())) {
            throw new OutOfLimitMatchingRequestException("매칭 중복");
        }
        try {
            Matching savedMatching = this.matchingRepository.save(
                    new Matching(
                            new Member(requestDto.getRequesterId()),
                            new Member(requestDto.getTargetId()),
                            requestDto.getMeetingPlace(),
                            requestDto.getMeetingPlaceAddress(),
                            requestDto.getMeetingTime()
                    )
            );
            notificationPublishService.publishMatchRequestNot(savedMatching);
            return savedMatching.getMatchingId();
        } catch (DataIntegrityViolationException e) {
            throw new MemberNotFoundException(e);
        }
    }

    public void updateMatchingStatus(Long matchingId, MatchingStatus matchingStatus) {
        Matching matching = this.matchingRepository.findById(matchingId)
                .orElseThrow(NoSuchElementException::new);
        switch (matchingStatus) {
            case ACCEPTED -> {
                    matching.accept();
                    notificationPublishService.publishMatchAcceptNot(matching);
            }
            case REJECTED -> {
                matching.reject();
                notificationPublishService.publishMatchRejectNot(matching);

            }
            case FINISHED -> matching.finish();
        }
    }

    public MatchingResponseDto getMatching(Long matchingId) {
        return MatchingResponseDto.of(
                this.matchingRepository.findById(matchingId)
                        .orElseThrow(NoSuchElementException::new)
        );
    }

    @Deprecated
    public List<MatchingResponseDto> getAllMatchingOfMember(Long memberId) {
        Member member = this.memberRepository.findById(memberId)
                .orElseThrow(NoSuchElementException::new);
        return Stream.concat(member.getMatchingRequests().stream(), member.getMatchingsReceived().stream())
                .sorted(MATCHING_LIST_SORT_COMPARATOR)
                .map(MatchingResponseDto::of)
                .toList();
    }

    /**
     * 특정 회원의 매칭 리스트를 조건에 맞게 가져온다.
     *
     * @param memberId 매칭을 조회하려는 회원의 ID
     * @param fetchType 회원이 신청한 매칭인지, 회원이 신청받은 매칭인지, 혹은 둘 다인지 조건
     *                  <p>ALL: 둘 다 가져옴
     *                  <p>WHAT_I_REQUESTED: memberId에 해당하는 회원이 신청한 매칭
     *                  <p>WHAT_I_RECEIVED: memberId에 해당하는 회원이 신청받은 매칭<br>
     * @param statusCondition 가져오려는 매칭의 상태
     *                        <p>ALL: 필터링 없이 모든 매칭을 가져옴
     *                        <p>FINISHED: 종료된 매칭을 가져옴 (MatchingStatus.FINISHED)
     *                        <p>IN_PROGRESS: 진행 중인 매칭을 가져옴 (MatchingStatus.ACCEPTED)
     *                        <p>HALTED: 중단된 매칭을 가져옴 (MatchingStatus.REJECTED, MatchingStatus.UNEXPECTEDLY_HALTED)<br>
     * @param page 가져오려는 매칭 리스트의 페이지. 1부터 시작하며, 페이지 범위를 벗어날 경우 empty list를 반환한다.
     * @param pageSize 가져오려는 매칭 리스트의 페이지 크기.
     * @return 쿼리 결과가 반영된 PageResponseDto 객체.
     * @throws MemberNotFoundException memberId에 해당하는 회원이 존재하지 않을 경우 예외 발생
     */
    public PageResponseDto<MatchingListResponseDto> searchMatchingListOfMember(Long memberId,
                                                                               MatchingFetchType fetchType,
                                                                               MatchingStatusCondition statusCondition,
                                                                               int page,
                                                                               int pageSize) {
        if (!this.memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId);
        }
        Page<MatchingListResponseDto> result = this.matchingRepository.findByMemberIdWithConditions(memberId,
                fetchType,
                statusCondition,
                PageRequest.of(page - 1, pageSize));
        return PageResponseDto.<MatchingListResponseDto>builder()
                .page(page)
                .pageSize(pageSize)
                .totalElementCount(result.getTotalElements())
                .totalPageCount(result.getTotalPages())
                .content(result.getContent())
                .build();
    }
}
