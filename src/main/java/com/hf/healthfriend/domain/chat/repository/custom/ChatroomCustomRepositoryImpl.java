package com.hf.healthfriend.domain.chat.repository.custom;

import com.hf.healthfriend.domain.chat.constant.ChatroomListSearchCondition;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.entity.QChatParticipation;
import com.hf.healthfriend.domain.chat.entity.QChatroom;
import com.hf.healthfriend.domain.chat.entity.chatmessage.QChatMessage;
import com.hf.healthfriend.domain.chat.repository.dto.ChatroomListDto;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;
import com.hf.healthfriend.domain.matching.entity.QMatching;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatroomCustomRepositoryImpl implements ChatroomCustomRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final QChatroom chatroom = QChatroom.chatroom;
    private final QMember opponentParticipant = QMember.member;
    private final QMatching matching = QMatching.matching;
    private final QChatMessage chatMessage = QChatMessage.chatMessage;
    private final QChatParticipation chatParticipation = QChatParticipation.chatParticipation;

    @Override
    public Chatroom saveWithParticipants(Member... participants) {
        Chatroom newChatroom = Chatroom.newChatroom(
                Arrays.stream(participants)
                        .map((e) -> this.em.getReference(Member.class, e.getId()))
                        .toArray(Member[]::new)
        );
        this.em.persist(newChatroom);
        return newChatroom;
    }

    @Override
    public List<ChatroomListDto> findByParticipantIdAndSearchCondition(Long participantId,
                                                                       ChatroomListSearchCondition searchCondition,
                                                                       Pageable page) {
        return this.queryFactory.select(
                        Projections.constructor(
                                ChatroomListDto.class,
                                this.chatroom,
                                this.opponentParticipant.id,
                                this.opponentParticipant.nickname,
                                this.opponentParticipant.profileImageUrl,
                                this.matching.status
                        )
                )
                .from(this.chatroom)
                .innerJoin(this.chatParticipation).on(this.chatroom.eq(this.chatParticipation.chatroom)).fetchJoin()
                .innerJoin(this.opponentParticipant).on(this.opponentParticipant.eq(this.chatParticipation.member))
                .leftJoin(this.matching).on(this.matching.requester.eq(this.opponentParticipant)
                        .or(this.matching.targetMember.eq(this.opponentParticipant)))
                .leftJoin(this.chatMessage).on(this.chatMessage.eq(this.chatroom.lastChatMessage)).fetchJoin()
                .where(getConditionForChatroomListQuery(participantId, searchCondition))
                .orderBy(this.chatMessage.creationTime.desc(), this.chatroom.creationTime.asc())
                .limit(page.getPageSize())
                .offset(page.getOffset())
                .fetch();
    }

    private BooleanBuilder getConditionForChatroomListQuery(Long participantId,
                                                            ChatroomListSearchCondition searchCondition) {
        BooleanBuilder builder = new BooleanBuilder(
                this.chatroom.chatroomId.in(
                        JPAExpressions
                                .select(this.chatroom.chatroomId)
                                .from(this.chatroom)
                                .innerJoin(this.chatParticipation).on(this.chatParticipation.chatroom.eq(this.chatroom))
                                .where(this.chatParticipation.member.id.eq(participantId))
                )
        );

        builder.and(this.opponentParticipant.id.ne(participantId));

        switch (searchCondition) {
            case MATCHING_IN_PROGRESS -> builder.and(this.matching.status.eq(MatchingStatus.ACCEPTED));
            case MATCHING_TERMINATED ->
                    builder.and(this.matching.status.in(MatchingStatus.FINISHED, MatchingStatus.UNEXPECTEDLY_HALTED));
        }

        builder.and(this.matching.isNull().or(this.matching.requester.id.eq(participantId)
                .or(this.matching.targetMember.id.eq(participantId))));

        builder.and(this.chatroom.deleted.isFalse());

        return builder;
    }
}
