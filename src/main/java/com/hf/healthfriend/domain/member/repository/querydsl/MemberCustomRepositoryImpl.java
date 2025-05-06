package com.hf.healthfriend.domain.member.repository.querydsl;

import com.hf.healthfriend.domain.member.constant.MemberSortType;
import com.hf.healthfriend.domain.member.dto.request.MembersSearchRequest;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.entity.QMember;
import com.hf.healthfriend.domain.spec.entity.QSpec;
import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.member.exception.MemberNotFoundException;
import com.hf.healthfriend.domain.member.repository.dto.MemberUpdateDto;
import com.hf.healthfriend.domain.member.repository.dto.ProfileQueryResultDto;
import com.hf.healthfriend.domain.spec.dto.SpecDto;
import com.hf.healthfriend.global.util.mapping.BeanMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final QMember member = QMember.member;
    private final QSpec spec = QSpec.spec;
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private final BeanMapper beanMapper;

    @Override
    public List<MemberListResponse> searchMembers (String keyword, MembersSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = filter(keyword, request);
        OrderSpecifier<?>[] orderSpecifier = getSortType(request);
        return queryFactory
                .select(Projections.constructor(MemberListResponse.class,
                        member.id,
                        member.profileImageUrl,
                        member.introduction,
                        member.nickname,
                        member.wishedCount,
                        member.reviewScore,
                        member.matchedCount,
                        member.fitnessLevel.stringValue(),
                        member.companionStyle.stringValue(),
                        member.fitnessEagerness.stringValue(),
                        member.fitnessKind.stringValue(),
                        member.fitnessObjective.stringValue()))
                .from(member)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Long getSearchedMembersSize(String keyword, MembersSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = filter(keyword, request);
        return queryFactory
                .select(member.count())
                .from(member)
                .where(builder)
                .fetchOne();
    }

    public OrderSpecifier<?>[] getSortType(MembersSearchRequest request) {
        MemberSortType sortType = request.getMemberSortType();
        if (sortType == null) {
            return new OrderSpecifier<?>[]{member.matchedCount.desc()};
        }
        return switch(sortType) {
            case SCORE -> new OrderSpecifier<?>[]{member.reviewScore.desc()};
            case WISH_COUNT -> new OrderSpecifier<?>[]{member.wishedCount.desc()};
            case RESPONSE_RATE -> new OrderSpecifier<?>[]{}; // TODO: 구현 필요
            default -> new OrderSpecifier<?>[]{member.matchedCount.desc()};
        };
    }

    public BooleanBuilder filter(String keyword, MembersSearchRequest request) {
        // TODO : 필터링 요소가 너무 많아지므로 ENUM 에 인덱스를 거는 것을 고려해야 한다.
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(member.isDeleted.eq(false));

        if (request.getFitnessLevels() != null && !request.getFitnessLevels().isEmpty()) {
            builder.and(member.fitnessLevel.stringValue().in(request.getFitnessLevels()));
        }
        if (request.getCompanionStyles() != null && !request.getCompanionStyles().isEmpty()) {
            builder.and(member.companionStyle.stringValue().in(request.getCompanionStyles()));
        }
        if (request.getFitnessEagernesses() != null && !request.getFitnessEagernesses().isEmpty()) {
            builder.and(member.fitnessEagerness.stringValue().in(request.getFitnessEagernesses()));
        }
        if (request.getFitnessKinds() != null && !request.getFitnessKinds().isEmpty()) {
            builder.and(member.fitnessKind.stringValue().in(request.getFitnessKinds()));
        }
        if (request.getFitnessObjectives() != null && !request.getFitnessObjectives().isEmpty()) {
            builder.and(member.fitnessObjective.stringValue().in(request.getFitnessObjectives()));
        }
        if(request.getCd1()!=null){
            builder.and(member.cd1.eq(request.getCd1()));
        }
        if(request.getCd2()!=null){
            builder.and(member.cd2.eq(request.getCd2()));
        }
        if(request.getCd3()!=null){
            builder.and(member.cd3.eq(request.getCd3()));
        }
        // TODO : Full-Text-Search 로 변환
        if(keyword!=null){
                builder.and(member.introduction.containsIgnoreCase(keyword)
                        .or(member.nickname.containsIgnoreCase(keyword)));
        }
        log.info(builder.toString());
        return builder;
    }

    @Override
    public Optional<Member> findByMemberId(Long memberId) {
        Member findMember = this.queryFactory.selectFrom(this.member)
                .leftJoin(this.spec).on(this.spec.member.id.eq(this.member.id))
                .where(this.spec.isDeleted.isNull().or(this.spec.isDeleted.isFalse()), this.member.id.eq(memberId), this.member.isDeleted.isFalse())
                .fetchOne();
        return Optional.ofNullable(findMember);
    }

    @Override
    public Member update(Long memberId, MemberUpdateDto updateDto) {
        Member member = this.em.find(Member.class, memberId);
        if (member == null) {
            throw new MemberNotFoundException(memberId);
        }

        member.update(updateDto);
        return member;
    }

    @Override
    public Optional<ProfileQueryResultDto> findProfileByMemberId(Long memberId) {
        List<ProfileQueryResultDto> result = this.queryFactory.selectFrom(this.member)
                .leftJoin(this.spec).on(this.spec.member.eq(this.member))
                .where(this.member.id.eq(memberId),
                        this.member.isDeleted.isFalse(),
                        this.spec.isDeleted.isFalse().or(this.spec.isNull()))
                .orderBy(this.spec.startDate.desc(), this.spec.endDate.desc().nullsFirst())
                .transform(GroupBy.groupBy(this.member.id).list(
                        Projections.constructor(
                                ProfileQueryResultDto.class,
                                this.member.id,
                                this.member.introduction,
                                GroupBy.list(
                                        Projections.constructor(
                                                SpecDto.class,
                                                this.spec.specId,
                                                this.spec.startDate,
                                                this.spec.endDate,
                                                this.spec.isCurrent,
                                                this.spec.title,
                                                this.spec.description
                                        )
                                ),
                                this.member.reviewScore,
                                this.member.matchedCount,
                                this.member.wishedCount
                        )
                ));
        if (result.size() > 1) {
            log.warn("findProfileByMemberId - 쿼리 결과 리스트의 사이즈가 1을 초과합니다.");
            result.forEach((r) -> log.warn("memberId={}", r.memberId()));
        }

        return result.isEmpty() ? Optional.empty() : Optional.of(removeNull(result.get(0)));
    }

    private ProfileQueryResultDto removeNull(ProfileQueryResultDto dto) {
        for (int i = 0; i < dto.specs().size(); i++) {
            SpecDto specDto = dto.specs().get(i);
            if (specDto.getSpecId() == null) {
                dto.specs().remove(i--);
            }
        }
        return dto;
    }

    public Long getTotalPageSize(int size){
        Long totalPageSize = queryFactory
                .select(member.count())
                .from(member)
                .where(member.isDeleted.eq(false))
                .fetchOne();
        if (totalPageSize == null) return 0L;
        return (long) Math.ceil((double) totalPageSize / size);
    }
}
