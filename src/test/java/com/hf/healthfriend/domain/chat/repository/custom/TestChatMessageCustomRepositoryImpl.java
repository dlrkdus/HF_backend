package com.hf.healthfriend.domain.chat.repository.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.constant.MatchingResponseType;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.content.ImageChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.request.content.MatchingRequestChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.request.content.MatchingResponseChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.request.content.TextChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.chat.repository.ChatParticipationRepository;
import com.hf.healthfriend.domain.chat.repository.ChatroomRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Map;
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
class TestChatMessageCustomRepositoryImpl {

    @Autowired
    ChatMessageCustomRepositoryImpl chatMessageCustomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatroomRepository chatroomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatParticipationRepository chatParticipationRepository;

    static Stream<Arguments> saveMessageWithChatroomId_success() {
        return Stream.of(
                Arguments.of("텍스트 채팅 메시지", ChatMessageType.TEXT, Map.of("text", "Hello!")),
                Arguments.of(
                        "매칭 신청 채팅 메시지",
                        ChatMessageType.MATCHING_REQUEST,
                        Map.of(
                                "matchingTargetId", 0L,
                                "meetingTime", LocalDateTime.now().plusDays(1),
                                "meetingPlace", "Somewhere",
                                "meetingPlaceAddress", "Somewhere"
                        )
                ),
                Arguments.of(
                        "매칭 수락 채팅 메시지",
                        ChatMessageType.MATCHING_RESPONSE,
                        Map.of(
                                "matchingResponseType", MatchingResponseType.ACCEPTED,
                                "matchingId", 0L,
                                "cancelMessage", ""
                        )
                ),
                Arguments.of(
                        "매칭 거절 채팅 메시지",
                        ChatMessageType.MATCHING_RESPONSE,
                        Map.of(
                                "matchingResponseType", MatchingResponseType.REJECTED,
                                "matchingId", 0L,
                                "cancelMessage", "No"
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("save() - {0} - 저장 성공")
    void saveMessageWithChatroomId_success(String testCaseName, ChatMessageType chatMessageType, Map<String, Object> content) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Member sender = SampleEntityGenerator.generateSampleMember("sender@gmail.com", "sender");
        Member receiver = SampleEntityGenerator.generateSampleMember("receiver@gmail.com", "receiver");
        this.memberRepository.save(sender);
        this.memberRepository.save(receiver);

        Chatroom dummyChatroom = SampleEntityGenerator.generateSampleChatroom(sender, receiver);
        this.chatroomRepository.save(dummyChatroom);

        Constructor<ChatMessageSendRequestDto> constructor = ChatMessageSendRequestDto.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ChatMessageSendRequestDto<?> dto = constructor.newInstance();
        ReflectionTestUtils.setField(dto, "senderId", sender.getId());
        ReflectionTestUtils.setField(dto, "chatMessageType", chatMessageType);
        ReflectionTestUtils.setField(dto, "content",
                switch (chatMessageType) {
                    case TEXT -> objectMapper.convertValue(content, TextChatMessageSendRequestContent.class);
                    case IMAGE -> objectMapper.convertValue(content, ImageChatMessageSendRequestContent.class);
                    case MATCHING_REQUEST -> objectMapper.convertValue(content, MatchingRequestChatMessageSendRequestContent.class);
                    case MATCHING_RESPONSE -> objectMapper.convertValue(content, MatchingResponseChatMessageSendRequestContent.class);
                }
        );

        // When
        ChatMessage message = this.chatMessageCustomRepository.saveMessageWithChatroomId(dummyChatroom.getChatroomId(), dto);

        // Then
        Optional<ChatMessage> findMessageOp = this.chatMessageRepository.findById(message.getChatMessageId());
        assertThat(findMessageOp).isNotEmpty();
    }
}