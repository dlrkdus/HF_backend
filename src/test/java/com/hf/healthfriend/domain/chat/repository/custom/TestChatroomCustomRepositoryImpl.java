package com.hf.healthfriend.domain.chat.repository.custom;

import com.hf.healthfriend.domain.chat.constant.ChatroomListSearchCondition;
import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.ChatParticipationId;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.repository.ChatParticipationRepository;
import com.hf.healthfriend.domain.chat.repository.ChatroomRepository;
import com.hf.healthfriend.domain.chat.repository.dto.ChatroomListDto;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.matching.repository.MatchingRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        TestConfig.class,
        MysqlTestcontainerConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
class TestChatroomCustomRepositoryImpl {

    @Autowired
    ChatroomCustomRepositoryImpl chatroomCustomRepository;

    @Autowired
    ChatroomRepository chatroomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatParticipationRepository chatParticipationRepository;

    @Autowired
    MatchingRepository matchingRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("saveWithParticipants() - success")
    void saveWithParticipants_success() {
        // Given
        Member requester = SampleEntityGenerator.generateSampleMember("requester@gmail.com", "REQ");
        Member target = SampleEntityGenerator.generateSampleMember("target@gmail.com", "TAR");
        this.memberRepository.save(requester);
        this.memberRepository.save(target);

        this.em.detach(requester);
        this.em.detach(target);

        // When
        Chatroom chatroom =
                this.chatroomCustomRepository.saveWithParticipants(new Member(requester.getId()),
                        new Member(target.getId()));

        // Then
        Optional<Chatroom> chatroomOp = this.chatroomRepository.findById(chatroom.getChatroomId());
        assertThat(chatroomOp).isNotEmpty();
        List<ChatParticipation> findParticipations = this.chatParticipationRepository.findAll();
        assertThat(findParticipations).size().isEqualTo(2);

        Chatroom findChatroom = chatroomOp.get();

        assertThat(findChatroom.getParticipations().stream().map(ChatParticipation::getChatParticipationId))
                .containsExactlyInAnyOrder(findParticipations.stream().map(ChatParticipation::getChatParticipationId).toArray(ChatParticipationId[]::new));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALL",
            "MATCHING_IN_PROGRESS",
            "MATCHING_TERMINATED",
            ""
    })
    @DisplayName("findByParticipantIdAndSearchCondition() - success")
    void findByParticipantIdAndSearchCondition_success(String searchConditionInString) {
        // Given
        ChatroomListSearchCondition searchCondition = ChatroomListSearchCondition.ALL;
        try {
            searchCondition = ChatroomListSearchCondition.valueOf(searchConditionInString);
        } catch (IllegalArgumentException e) {
            log.info("without condition. String={}", searchConditionInString);
        }
        ChatDummyData chatDummyData = insertRecords_for_findByParticipantIdAndSearchCondition();

        this.em.flush();

        // When
        List<ChatroomListDto> result =
                this.chatroomRepository.findByParticipantIdAndSearchCondition(chatDummyData.mainMember.getId(),
                        searchCondition, PageRequest.of(0, 100));

        for (ChatroomListDto r : result) {
            log.info("chatroomId={}, chatMessageLen={}, opponent={}", r.chatroom().getChatroomId(), r.chatroom().getChatMessages().size(), r.opponentParticipantId());//, r.matchingStatus());
        }

        // Then
        Collection<Chatroom> expectedChatrooms = switch (searchCondition) {
            case MATCHING_IN_PROGRESS -> chatDummyData.chatroomsMatchingInProgress;
            case MATCHING_TERMINATED -> chatDummyData.chatroomsMatchingTerminated;
            default -> Stream.concat(
                            Stream.concat(
                                    chatDummyData.chatroomsWithoutMatching.stream(),
                                    chatDummyData.chatroomsMatchingInProgress.stream()
                            ),
                            chatDummyData.chatroomsMatchingTerminated.stream()
                    )
                    .toList();
        };

        assertThat(result.stream().map(ChatroomListDto::chatroom).map(Chatroom::getChatroomId))
                .containsExactlyInAnyOrder(expectedChatrooms.stream().map(Chatroom::getChatroomId).toArray(Long[]::new));
    }

    private ChatDummyData insertRecords_for_findByParticipantIdAndSearchCondition() {
        Member mainMember = SampleEntityGenerator.generateSampleMember("main@gmail.com", "main");

        List<Member> chatterWithoutMatching = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .map((num) -> SampleEntityGenerator.generateSampleMember("withoutMatching" + num, "ni" + num))
                .toList();
        List<Member> chatterMatchingInProgress = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .map((num) -> SampleEntityGenerator.generateSampleMember("matchingInProgress" + num, "nic" + num))
                .toList();
        List<Member> chatterMatchingTerminated = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .map((num) -> SampleEntityGenerator.generateSampleMember("matchingTerminated" + num, "nik" + num))
                .toList();

        this.memberRepository.save(mainMember);
        this.memberRepository.saveAll(chatterWithoutMatching);
        this.memberRepository.saveAll(chatterMatchingInProgress);
        this.memberRepository.saveAll(chatterMatchingTerminated);

        Collection<Chatroom> chatroomsWithoutMatching = Stream.of(chatterWithoutMatching.toArray(new Member[0]))
                .map((chatter) -> this.chatroomRepository.saveWithParticipants(mainMember, chatter))
                .toList();
        Collection<Chatroom> chatroomsMatchingInProgress = Stream.of(chatterMatchingInProgress.toArray(new Member[0]))
                .map((chatter) -> this.chatroomRepository.saveWithParticipants(mainMember, chatter))
                .toList();
        Collection<Chatroom> chatroomsMatchingTerminated = Stream.of(chatterMatchingTerminated.toArray(new Member[0]))
                .map((chatter) -> this.chatroomRepository.saveWithParticipants(mainMember, chatter))
                .toList();

        Stream.concat(Stream.of(chatterMatchingInProgress.toArray(new Member[0])),
                        Stream.of(chatterMatchingTerminated.toArray(new Member[0])))
                .forEach((matcher) -> {
                    Matching matching =
                            new Matching(mainMember, matcher, "h", "h", LocalDateTime.now().plusDays(1));
                    if (matcher.getEmail().startsWith("matchingTerminated")) {
                        ReflectionTestUtils.setField(matching, "status", MatchingStatus.FINISHED);
                    } else if (matcher.getEmail().startsWith("matchingInProgress")) {
                        ReflectionTestUtils.setField(matching, "status", MatchingStatus.ACCEPTED);
                    } else {
                        return;
                    }
                    this.matchingRepository.save(matching);
                });

        chatterMatchingInProgress.forEach((chInProgress) -> {
            chatterMatchingTerminated.forEach((chTerminated) -> {
                Matching matching =
                        new Matching(chInProgress, chTerminated, "h", "h", LocalDateTime.now().plusDays(1));
                ReflectionTestUtils.setField(matching, "status", MatchingStatus.ACCEPTED);
                this.matchingRepository.save(matching);
            });
        });

        return new ChatDummyData(
                mainMember,
                chatterWithoutMatching,
                chatterMatchingInProgress,
                chatterMatchingTerminated,
                chatroomsWithoutMatching,
                chatroomsMatchingInProgress,
                chatroomsMatchingTerminated
        );
    }

    @AllArgsConstructor
    private static class ChatDummyData {
        Member mainMember;
        Collection<Member> chatterWithoutMatching;
        Collection<Member> chatterMatchingInProgress;
        Collection<Member> chatterMatchingTerminated;
        Collection<Chatroom> chatroomsWithoutMatching;
        Collection<Chatroom> chatroomsMatchingInProgress;
        Collection<Chatroom> chatroomsMatchingTerminated;
    }
}