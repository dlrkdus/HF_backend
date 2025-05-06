package com.hf.healthfriend.domain.chat.service.delegator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.constant.MatchingResponseType;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.entity.chatmessage.MatchingRequestChatMessage;
import com.hf.healthfriend.domain.chat.entity.chatmessage.MatchingResponseChatMessage;
import com.hf.healthfriend.domain.chat.entity.chatmessage.TextChatMessage;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.matching.service.MatchingService;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.global.file.FileUrlResolver;
import com.hf.healthfriend.global.file.image.ImageExtension;
import com.hf.healthfriend.global.jackson.deserializer.ImageExtensionDeserializer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TestDelegatingChatMessageProcessorBean {
    private static final Long DUMMY_CHATROOM_ID = 1000L;
    private static final Long DUMMY_SENDER_ID = 2000L;
    private static final Long DUMMY_MATCHING_ID = 3000L;
    private static final Long DUMMY_CHAT_MESSAGE_ID = 4000L;
    private static final Map<String, Object> DUMMY_MATCHING_REQUEST_DATA = Map.of(
            "matchingTargetId", 5000L,
            "meetingTime", LocalDateTime.now().plusDays(1),
            "meetingPlace", "PLACE",
            "meetingPlaceAddress", "ADDRESS"
    );
    private static final String DUMMY_CHAT_MESSAGE = "Hello, World!";

    @TestConfiguration
    @ComponentScan(basePackages = "com.hf.healthfriend.domain.chat.service.delegator")
    static class TestDelegatingChatMessageProcessorBeanConfig {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

            SimpleModule imageExtensionModule = new SimpleModule();
            imageExtensionModule.addDeserializer(ImageExtension.class, new ImageExtensionDeserializer());
            objectMapper.registerModule(imageExtensionModule);

            return objectMapper;
        }

        @Bean
        public FileUrlResolver fileUrlResolver() {
            return Mockito.mock(FileUrlResolver.class);
        }

        @Bean
        public ChatMessageRepository chatMessageRepository() {
            return Mockito.mock(ChatMessageRepository.class);
        }

        @RequiredArgsConstructor
        static class MatchingResponseChatMessageArgumentMatcher implements ArgumentMatcher<MatchingResponseChatMessage> {
            private final MatchingResponseChatMessage thisMessage;

            @Override
            public boolean matches(MatchingResponseChatMessage argument) {
                return thisMessage.getMatchingResponseType() == argument.getMatchingResponseType();
            }
        }

        @Bean
        public MatchingService matchingService() {
            MatchingService matchingServiceMock = Mockito.mock(MatchingService.class);

            when(matchingServiceMock.requestMatching(any())).thenReturn(DUMMY_MATCHING_ID);

            return matchingServiceMock;
        }
    }

    @Autowired
    DelegatingChatMessageProcessorBean processor;

    @Autowired
    ChatMessageRepository chatMessageRepositoryMock;

    @Test
    @DisplayName("sendMessage() - 텍스트 메시지 처리 성공")
    void sendMessage_textMessage_success() throws Exception {
        // Given
        TextChatMessage textChatMessage = new TextChatMessage(
                new Chatroom(DUMMY_CHATROOM_ID),
                new Member(DUMMY_SENDER_ID),
                DUMMY_CHAT_MESSAGE
        );

        setCommonChatMessageField(textChatMessage, LocalDateTime.now());

        doReturn(textChatMessage).when(chatMessageRepositoryMock).saveMessageWithChatroomId(any(), any());

        Constructor<?> dtoConstructor = ChatMessageSendRequestDto.class.getDeclaredConstructors()[0];
        dtoConstructor.setAccessible(true);
        Object dtoInstance = dtoConstructor.newInstance();
        ReflectionTestUtils.setField(dtoInstance, "senderId", DUMMY_SENDER_ID);
        ReflectionTestUtils.setField(dtoInstance, "chatMessageType", ChatMessageType.TEXT);
        ReflectionTestUtils.setField(dtoInstance, "content", Map.of("text", DUMMY_CHAT_MESSAGE));

        // When
        ChatMessageSendResponseDto result = this.processor.sendMessage(DUMMY_CHATROOM_ID, (ChatMessageSendRequestDto<Object>) dtoInstance);

        // Then
        assertThat(result.chatMessageId()).isEqualTo(DUMMY_CHAT_MESSAGE_ID);
        assertThat(result.chatroomId()).isEqualTo(DUMMY_CHATROOM_ID);
        assertThat(result.senderId()).isEqualTo(DUMMY_SENDER_ID);
        assertThat(result.content()).isEqualTo(Map.of("text", DUMMY_CHAT_MESSAGE));
    }

    @Test
    @DisplayName("sendMessage() - 매칭 신청 메시지 전송 성공")
    void sendMessage_matchingRequest_success() throws Exception {
        // Given
        MatchingRequestChatMessage matchingRequestChatMessage = new MatchingRequestChatMessage(
                new Chatroom(DUMMY_CHATROOM_ID),
                new Member(DUMMY_SENDER_ID),
                (LocalDateTime) DUMMY_MATCHING_REQUEST_DATA.get("meetingTime"),
                (String) DUMMY_MATCHING_REQUEST_DATA.get("meetingPlace"),
                (String) DUMMY_MATCHING_REQUEST_DATA.get("meetingPlaceAddress")
        );

        setCommonChatMessageField(matchingRequestChatMessage, LocalDateTime.now());

        doReturn(matchingRequestChatMessage)
                .when(chatMessageRepositoryMock).saveMessageWithChatroomId(any(), any());

        Constructor<?> dtoConstructor = ChatMessageSendRequestDto.class.getDeclaredConstructors()[0];
        dtoConstructor.setAccessible(true);
        Object dtoInstance = dtoConstructor.newInstance();
        ReflectionTestUtils.setField(dtoInstance, "senderId", DUMMY_SENDER_ID);
        ReflectionTestUtils.setField(dtoInstance, "chatMessageType", ChatMessageType.MATCHING_REQUEST);
        ReflectionTestUtils.setField(dtoInstance, "content", DUMMY_MATCHING_REQUEST_DATA);

        // When
        ChatMessageSendResponseDto result = this.processor.sendMessage(DUMMY_CHATROOM_ID, (ChatMessageSendRequestDto<Object>) dtoInstance);

        // Then
        assertThat(result.chatMessageId()).isEqualTo(DUMMY_CHAT_MESSAGE_ID);
        assertThat(result.chatroomId()).isEqualTo(DUMMY_CHATROOM_ID);
        assertThat(result.senderId()).isEqualTo(DUMMY_SENDER_ID);
        assertThat(result.content()).isEqualTo(
                Map.of(
                        "matchingId", DUMMY_MATCHING_ID,
                        "meetingTime", DUMMY_MATCHING_REQUEST_DATA.get("meetingTime"),
                        "meetingPlace", DUMMY_MATCHING_REQUEST_DATA.get("meetingPlace"),
                        "meetingPlaceAddress", DUMMY_MATCHING_REQUEST_DATA.get("meetingPlaceAddress")
                )
        );
    }

    @Test
    @DisplayName("sendMessage() - 매칭 수락 응답 메시지 전송 성공")
    void sendMessage_matchingResponse_accept_success() throws Exception {
        // Given
        MatchingResponseChatMessage acceptMatchingResponseChatMessage =
                MatchingResponseChatMessage.createAcceptanceMessage(new Chatroom(DUMMY_CHATROOM_ID), new Member(DUMMY_SENDER_ID));

        setCommonChatMessageField(acceptMatchingResponseChatMessage, LocalDateTime.now());

        doReturn(acceptMatchingResponseChatMessage)
                .when(chatMessageRepositoryMock)
                .saveMessageWithChatroomId(any(), any());

        Constructor<?> dtoConstructor = ChatMessageSendRequestDto.class.getDeclaredConstructors()[0];
        dtoConstructor.setAccessible(true);
        Object dtoInstance = dtoConstructor.newInstance();
        ReflectionTestUtils.setField(dtoInstance, "senderId", DUMMY_SENDER_ID);
        ReflectionTestUtils.setField(dtoInstance, "chatMessageType", ChatMessageType.MATCHING_RESPONSE);
        ReflectionTestUtils.setField(dtoInstance, "content", Map.of(
                "matchingResponseType", MatchingResponseType.ACCEPTED.name(),
                "matchingId", DUMMY_MATCHING_ID
        ));

        // When
        ChatMessageSendResponseDto result = this.processor.sendMessage(DUMMY_CHATROOM_ID, (ChatMessageSendRequestDto<Object>) dtoInstance);

        // Then
        assertThat(result.chatMessageId()).isEqualTo(DUMMY_CHAT_MESSAGE_ID);
        assertThat(result.chatroomId()).isEqualTo(DUMMY_CHATROOM_ID);
        assertThat(result.senderId()).isEqualTo(DUMMY_SENDER_ID);
        assertThat(result.content()).isEqualTo(
                Map.of(
                        "matchingResponseType", MatchingResponseType.ACCEPTED.name(),
                        "cancelMessage", ""
                )
        );
    }

    @Test
    @DisplayName("sendMessage() - 매칭 거절 응답 메시지 전송 성공")
    void sendMessage_matchingResponse_reject_success() throws Exception {
        // Given
        MatchingResponseChatMessage rejectMatchingResponseChatMessage =
                MatchingResponseChatMessage.createRejectMessage(new Chatroom(DUMMY_CHATROOM_ID),
                        new Member(DUMMY_SENDER_ID),
                        DUMMY_CHAT_MESSAGE);

        setCommonChatMessageField(rejectMatchingResponseChatMessage, LocalDateTime.now());

        doReturn(rejectMatchingResponseChatMessage)
                .when(chatMessageRepositoryMock)
                .saveMessageWithChatroomId(any(), any());

        Constructor<?> dtoConstructor = ChatMessageSendRequestDto.class.getDeclaredConstructors()[0];
        dtoConstructor.setAccessible(true);
        Object dtoInstance = dtoConstructor.newInstance();
        ReflectionTestUtils.setField(dtoInstance, "senderId", DUMMY_SENDER_ID);
        ReflectionTestUtils.setField(dtoInstance, "chatMessageType", ChatMessageType.MATCHING_RESPONSE);
        ReflectionTestUtils.setField(dtoInstance, "content", Map.of(
                "matchingResponseType", MatchingResponseType.REJECTED.name(),
                "matchingId", DUMMY_MATCHING_ID,
                "cancelMessage", DUMMY_CHAT_MESSAGE
        ));

        // When
        ChatMessageSendResponseDto result = this.processor.sendMessage(DUMMY_CHATROOM_ID, (ChatMessageSendRequestDto<Object>) dtoInstance);

        // Then
        assertThat(result.chatMessageId()).isEqualTo(DUMMY_CHAT_MESSAGE_ID);
        assertThat(result.chatroomId()).isEqualTo(DUMMY_CHATROOM_ID);
        assertThat(result.senderId()).isEqualTo(DUMMY_SENDER_ID);
        assertThat(result.content()).isEqualTo(
                Map.of(
                        "matchingResponseType", MatchingResponseType.REJECTED.name(),
                        "cancelMessage", DUMMY_CHAT_MESSAGE
                )
        );
    }

    private void setCommonChatMessageField(Object toSet, LocalDateTime now) {
        ReflectionTestUtils.setField(toSet, "creationTime", now);
        ReflectionTestUtils.setField(toSet, "lastModified", now);
        ReflectionTestUtils.setField(toSet, "chatMessageId", DUMMY_CHAT_MESSAGE_ID);
    }
}