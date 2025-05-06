package com.hf.healthfriend.domain.chat.repository;

import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ImageChatMessage;
import com.hf.healthfriend.domain.chat.entity.chatmessage.TextChatMessage;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Import({
        TestConfig.class,
        MysqlTestcontainerConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
class TestChatMessageRepository {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatroomRepository chatroomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatParticipationRepository chatParticipationRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("save() - 이미지 메시지 저장 성공")
    void saveImageMessage_success() {
        // Given
        Member sender = SampleEntityGenerator.generateSampleMember("sender@gmail.com", "sender");
        Member receiver = SampleEntityGenerator.generateSampleMember("receiver@gmail.com", "receiver");
        this.memberRepository.save(sender);
        this.memberRepository.save(receiver);

        Chatroom dummyChatroom = SampleEntityGenerator.generateSampleChatroom(sender, receiver);
        this.chatroomRepository.save(dummyChatroom);

        // When
        ImageChatMessage message = new ImageChatMessage(dummyChatroom, sender, "http://localhost/image.jpg");
        assertThatNoException().isThrownBy(() -> this.chatMessageRepository.save(message));

        // Then
        Optional<ChatMessage> findMessageOp = this.chatMessageRepository.findById(message.getChatMessageId());
        assertThat(findMessageOp).isNotEmpty();
        ChatMessage chatMessage = findMessageOp.get();
        assertThat(chatMessage instanceof ImageChatMessage).isTrue();
        assertThat(((ImageChatMessage) chatMessage).getImageUrl()).isEqualTo(message.getImageUrl());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1:1",
            "1:10",
            "1:100",
            "2:1",
            "2:10",
            "2:100",
            "3:100",
            "3:25",
            "3:47"
    }, delimiter = ':')
    @DisplayName("findByChatroomId() - Pagination을 적용한 채팅 메시지 목록 불러오기 테스트")
    void findByChatroomId_success_fetchWithPagination(int page, int pageSize) {
        // Given
        int zeroBasedPage = page - 1;
        Member participant1 = SampleEntityGenerator.generateSampleMember("participant1@gmail.com", "part1");
        Member participant2 = SampleEntityGenerator.generateSampleMember("participant2@gmail.com", "part2");
        this.memberRepository.save(participant1);
        this.memberRepository.save(participant2);

        Chatroom chatroom = SampleEntityGenerator.generateSampleChatroom(participant1, participant2);
        this.chatroomRepository.save(chatroom);

        List<ChatMessage> messages = inputSampleChatMessages(chatroom, participant1, participant2);

        for (ChatMessage m : messages) {
            log.info("chatMessageId={}", m.getChatMessageId());
            log.info("creationTime={}", m.getCreationTime());
        }

        // When
        PageRequest pageObj = PageRequest.of(zeroBasedPage, pageSize);
        log.info("pageObj={}", pageObj);
        Page<ChatMessage> result = this.chatMessageRepository.findByChatroomId(chatroom.getChatroomId(),
                pageObj);

        // Then
        List<ChatMessage> expected = getSubList(messages, zeroBasedPage, pageSize);

        assertThat(result.getTotalElements()).isEqualTo(messages.size());
        assertThat(result.getTotalPages())
                .isEqualTo(messages.size() / pageSize + (messages.size() % pageSize > 0 ? 1 : 0));

        Long[] resultIds = result.getContent().stream().map(ChatMessage::getChatMessageId).toArray(Long[]::new);
        log.info("resultIds={}", Arrays.toString(resultIds));
        log.info("expected={}", expected.stream().map(ChatMessage::getChatMessageId).toList());
        // 순서 보장
        assertThat(expected.stream().map(ChatMessage::getChatMessageId))
                .containsExactly(resultIds);
    }

    private List<ChatMessage> inputSampleChatMessages(Chatroom chatroom, Member participant1, Member participant2) {
        final int DUMMY_COUNT = 100;
        List<ChatMessage> chatMessages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Set<Integer> usedRandomValue = new HashSet<>();
        for (int i = 0; i < DUMMY_COUNT; i++) {
            int num = i + 1;
            Random random = new Random();
            int randomValue;
            while (true) {
                randomValue = random.nextInt();
                if (!usedRandomValue.contains(randomValue)) {
                    usedRandomValue.add(randomValue);
                    break;
                }
            }
            LocalDateTime randomCreationTime = now.minus(randomValue, ChronoUnit.MILLIS);
            TextChatMessage message = new TextChatMessage(chatroom, num % 2 == 0 ? participant1 : participant2, "text" + num);
            ReflectionTestUtils.setField(message, "creationTime", randomCreationTime);
            ReflectionTestUtils.setField(message, "lastModified", randomCreationTime);
            chatMessages.add(message);
        }

        this.chatMessageRepository.saveAll(chatMessages);

        chatMessages.sort((c1, c2) -> {
            if (c1.getCreationTime().isAfter(c2.getCreationTime())) {
                return -1;
            } else if (c1.getCreationTime().isBefore(c2.getCreationTime())) {
                return 1;
            } else {
                return 0;
            }
        });

        return chatMessages;
    }

    private List<ChatMessage> getSubList(List<ChatMessage> original, int zeroBasedPage, int pageSize) {
        int originalSize = original.size();
        int fromIndex = zeroBasedPage * pageSize;
        int toIndex = Math.min((zeroBasedPage + 1) * pageSize, originalSize);
        if (fromIndex >= originalSize) {
            return new ArrayList<>();
        }

        return original.subList(fromIndex, toIndex);
    }

    @Test
    @DisplayName("readMessagesInChatroomByOpponent() - success")
    void readByChatroomId_success() {
        // Given
        Member participant1 = SampleEntityGenerator.generateSampleMember("participant1@gmail.com", "part1");
        Member participant2 = SampleEntityGenerator.generateSampleMember("participant2@gmail.com", "part2");
        this.memberRepository.save(participant1);
        this.memberRepository.save(participant2);

        Chatroom chatroom = SampleEntityGenerator.generateSampleChatroom(participant1, participant2);
        this.chatroomRepository.save(chatroom);

        List<ChatMessage> chatMessages = inputSampleChatMessages(chatroom, participant1, participant2);
        int chatMessageCount = chatMessages.size();

        // When
        this.chatMessageRepository.readMessagesInChatroomByOpponent(chatroom.getChatroomId());

        // Then
        List<ChatMessage> found =
                this.chatMessageRepository.findByChatroomId(chatroom.getChatroomId(), PageRequest.of(0, chatMessageCount))
                        .getContent();

        found.forEach((f) -> assertThat(f.isReadByOpponent()).isTrue());
    }
}