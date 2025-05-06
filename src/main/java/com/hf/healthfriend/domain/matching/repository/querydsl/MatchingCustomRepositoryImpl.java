package com.hf.healthfriend.domain.matching.repository.querydsl;

import com.hf.healthfriend.domain.matching.constant.MatchingFetchType;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;
import com.hf.healthfriend.domain.matching.constant.MatchingStatusCondition;
import com.hf.healthfriend.domain.matching.dto.response.MatchingListResponseDto;
import com.hf.healthfriend.domain.matching.dto.response.ProfileOfMemberInMatchingResponseDto;
import com.hf.healthfriend.domain.matching.entity.QMatching;
import com.hf.healthfriend.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchingCustomRepositoryImpl implements MatchingCustomRepository {
    private final JPAQueryFactory queryFactory;
    private final QMatching matching = QMatching.matching;
    private final QMember member = QMember.member;

    @Override
    public Page<MatchingListResponseDto> findByMemberIdWithConditions(@NotNull Long memberId,
                                                                      @NotNull MatchingFetchType matchingFetchType,
                                                                      @NotNull MatchingStatusCondition matchingStatusCondition,
                                                                      @NotNull Pageable page) {
        JPAQuery<MatchingListResponseDto> query = this.queryFactory.select(
                        Projections.constructor(
                                MatchingListResponseDto.class,
                                this.matching.matchingId,
                                this.matching.meetingPlace,
                                this.matching.meetingPlaceAddress,
                                this.matching.status,
                                this.matching.meetingTime,
                                this.matching.finishTime,
                                Projections.constructor(
                                        ProfileOfMemberInMatchingResponseDto.class,
                                        this.member.id,
                                        this.member.nickname,
                                        this.member.profileImageUrl,
                                        this.member.fitnessLevel,
                                        this.member.companionStyle,
                                        this.member.fitnessEagerness,
                                        this.member.fitnessKind,
                                        this.member.fitnessObjective,
                                        this.member.cd1,
                                        this.member.cd2,
                                        this.member.cd3,
                                        this.member.matchedCount
                                )
                        )
                )
                .from(this.member);

        switch (matchingFetchType) {
            case WHAT_I_RECEIVED -> query = query.innerJoin(this.matching).on(this.member.eq(this.matching.requester));
            case WHAT_I_REQUESTED ->
                    query = query.innerJoin(this.matching).on(this.member.eq(this.matching.targetMember));
            case ALL ->
                    query = query.innerJoin(this.matching).on(this.member.eq(this.matching.requester).or(this.member.eq(this.matching.targetMember))); // ALL 포함
        }

        BooleanBuilder queryCondition = generateQueryCondition(memberId, matchingFetchType, matchingStatusCondition);
        List<MatchingListResponseDto> result = query.where(queryCondition)
                .orderBy(this.matching.meetingTime.desc())
                .limit(page.getPageSize())
                .offset(page.getOffset())
                .fetch();

        return PageableExecutionUtils.getPage(result, page, () -> {
                    JPAQuery<Long> countQuery = this.queryFactory.select(this.matching.count())
                            .from(this.member);
                    switch (matchingFetchType) {
                        case WHAT_I_RECEIVED ->
                                countQuery = countQuery.innerJoin(this.matching).on(this.member.eq(this.matching.requester));
                        case WHAT_I_REQUESTED ->
                                countQuery = countQuery.innerJoin(this.matching).on(this.member.eq(this.matching.targetMember));
                        case ALL ->
                                countQuery = countQuery.innerJoin(this.matching).on(this.member.eq(this.matching.requester).or(this.member.eq(this.matching.targetMember))); // ALL 포함
                    }
                    return countQuery.where(queryCondition)
                            .fetchFirst();
                }

        );
    }

    private BooleanBuilder generateQueryCondition(Long memberId,
                                                  MatchingFetchType matchingFetchType,
                                                  MatchingStatusCondition matchingStatus) {
        BooleanBuilder builder = new BooleanBuilder(this.member.id.ne(memberId));

        if (matchingStatus != MatchingStatusCondition.ALL) {
            builder.and(this.matching.status.in(matchingStatus.getCorrespondingStatus()));
        }
        return builder;
    }

    @Override
    public boolean existsDuplicateMatchingRequest(Long memberId, LocalDate date) {
        BooleanExpression compareDatePart =
                Expressions.dateTimeOperation(LocalDate.class, Ops.DateTimeOps.DATE, this.matching.creationTime)
                        .eq(date);

        BooleanBuilder booleanBuilder = new BooleanBuilder(compareDatePart);

        booleanBuilder.or(this.matching.status.in(MatchingStatus.ACCEPTED, MatchingStatus.PENDING));
        booleanBuilder.and(this.matching.requester.id.eq(memberId));

        Integer result = this.queryFactory.selectOne()
                .from(this.matching)
                .where(booleanBuilder)
                .fetchFirst();
        return result != null;
    }
}
