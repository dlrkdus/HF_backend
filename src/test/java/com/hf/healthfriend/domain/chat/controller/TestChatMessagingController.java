package com.hf.healthfriend.domain.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatParticipationResponseDto;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.chat.repository.ChatParticipationRepository;
import com.hf.healthfriend.domain.chat.repository.ChatroomRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.RedisTestConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({MysqlTestcontainerConfig.class, RedisTestConfig.class})
@Slf4j
@ActiveProfiles({
        "mock-notification",
        "no-auth",
        "secret",
        "constants",
        "priv"
})
class TestChatMessagingController {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatParticipationRepository chatParticipationRepository;

    @Autowired
    ChatroomRepository chatroomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    PlatformTransactionManager txManager;

    @LocalServerPort
    int port;

    StompSession senderSession;
    StompSession receiverSession;
    Member sender;
    Member receiver;

    @BeforeEach
    void beforeEach() throws ExecutionException, InterruptedException {
        // 채팅에 참여할 두 Member 엔티티 생성 및 데이터베이스에 삽입
        // 여기서 저장된 sender와 receiver 엔티티를 다른 트랜잭션에서 사용해야 하므로
        // 데이터베이스에 커밋 (afterEach에서 삭제)
        TransactionStatus status = this.txManager.getTransaction(new DefaultTransactionAttribute());
        this.sender = SampleEntityGenerator.generateSampleMember("sender@gmail.com", "sender");
        this.receiver = SampleEntityGenerator.generateSampleMember("receiver@gmail.com", "receiver");
        this.memberRepository.save(this.sender);
        this.memberRepository.save(this.receiver);
        this.txManager.commit(status);

        // 웹 소켓 테스트를 위한 Client 생성
        WebSocketStompClient senderClient = generateStompClient();
        WebSocketStompClient receiverClient = generateStompClient();

        // 웹 소켓 handshake
        String websocketUrl = "ws://localhost:" + this.port + "/hf/portfolio";

        // 웹 소켓 통신을 위한 SessionHandler 생성
        StompSessionHandler stompSessionHandler = new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, @NotNull StompHeaders connectedHeaders) {
                log.info("sessionId={}", session.getSessionId());
                log.info("connectedHeaders={}", connectedHeaders);
            }
        };

        // 웹 소켓 통신을 위한 Session 생성
        this.senderSession = senderClient.connectAsync(websocketUrl + "?member-id=" + this.sender.getId(), stompSessionHandler).get();
        this.receiverSession = receiverClient.connectAsync(websocketUrl + "?member-id=" + this.receiver.getId(), stompSessionHandler).get();
    }

    @Autowired
    ObjectMapper objectMapper;

    private WebSocketStompClient generateStompClient() {
        // SockJS 기반 웹 소켓 클라이언트 생성
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter(this.objectMapper));
        return stompClient;
    }

    @AfterEach
    void afterEach() {
        // sender와 receiver는 beforeEach에서 별도의 트랜잭션에서 생성되었기 때문에
        // 직접 삭제
        this.memberRepository.delete(this.sender);
        this.memberRepository.delete(this.receiver);
    }

    // 채팅 신청에 대한 응답, 채팅 메시지 전송에 대한 응답이 제대로 도착했는지 확인하기 위한 카운트다운
    CountDownLatch latch = new CountDownLatch(4);
    LocalDateTime now;

    /**
     * <p>채팅 신청 및 채팅 메시지 전송 테스트
     * <p>흐름은 다음과 같다.
     * <p>1. 채팅에 참여하는 회원 (sender, receiver)는 미리 데이터베이스에 레코드 추가해 둠
     * <p>2. 채팅 신청 메시지 전송 -> 채팅 신청 시 Chatroom 엔티티와 ChatParticipation 엔티티 생성됨
     * <p>2.1. 채팅 신청 엔드포인트: /hf/app/chat/request
     * <p>2.2. 채팅 신청 subscription 엔드포인트: /hf/user/chat/request
     * <p>2.3. ChatMessagingController.requestChat() 메소드에서 처리
     * <p>2.4. sender, receiver에게 각각 메시지 전송
     * <p>3. 채팅 신청 완료 후 채팅 전송 - 아래 messageSendTest() 메소드
     * <p>4. sender와 receiver 각각 채팅 메시지 엔드포인트 subscription
     * <p>4.1. 채팅 메시지 전송 엔드포인트: /hf/app/chat/messages/{chatroomId}
     * <p>4.2. 채팅 메시지 subscription 엔드포인트: /hf/topic/chat/messages/{chatroomId}
     * <p>채팅 신청 응답 수신 시 한 번, 채팅 메시지 응답 수신 시 두 번 (sender와 receiver 각각 한 번)
     * 총 세 번 CountDownLatch의 count가 하나씩 줄어듦
     * <p>웹소켓 통신이 비동기 방식으로 처리되기 때문에 콜백 메소드 안에서 통신 처리 로직을 정의해야 하기
     * 때문에 웹소켓 응답 결과를 return할 수 없음. 그래서 결과값을 ConcurrentHashMap에 저장. 이후
     * ConcurrentHashMap에 담긴 값 검증
     */
    @Test
    @DisplayName("채팅 신청 후 메시지 보내기 테스트 - 성공")
    void chatRequestAndThenSendMessage() throws InterruptedException {
        now = LocalDateTime.now();

        // 새로운 트랜잭션에서 생성
        TransactionStatus txStatus = this.txManager.getTransaction(new DefaultTransactionAttribute());

        // 비동기 처리 상황에서 결과값을 검증할 객체를 담기 위한 ConcurrentHashMap
        Map<String, Object> resultMap = new ConcurrentHashMap<>();

        // 채팅 신청 엔드포인트에 대한 subscription
        // 신청자와 피신청자 둘 다 메시지를 받게 되지만, 우선 신청자만 구독
        this.senderSession.subscribe("/hf/user/" + this.sender.getId() + "/chat/request", new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return ChatParticipationResponseDto.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                log.info("payload={}", payload);
                log.info("payload type: {}", payload.getClass());

                ChatParticipationResponseDto dto = (ChatParticipationResponseDto) payload;
                resultMap.put("ChatParticipationResponseDto", dto);

                // 채팅 신청 완료 후, 채팅 전송 테스트를 수행하는 메소드 호출
                messageSendTest(dto, resultMap);
                latch.countDown();
            }
        });

        // 채팅 신청 메시지 전송
        this.senderSession.send("/hf/app/chat/request", Map.of(
                "requesterId", this.sender.getId(),
                "chatTargetId", this.receiver.getId()
        ));

        // 비동기 처리가 모두 끝날 때까지 대기
        boolean await = latch.await(8, TimeUnit.SECONDS);
        log.info("count={}", latch.getCount());

        // Then
        assertThat(await).isTrue();

        // 채팅 신청 검증
        assertThat(this.chatParticipationRepository.findAll()).size().isEqualTo(2);

        ChatParticipationResponseDto chatParticipationResponseDto =
                (ChatParticipationResponseDto) resultMap.get("ChatParticipationResponseDto");
        assertThat(chatParticipationResponseDto.newChatroomId()).isNotNull();
        assertThat(chatParticipationResponseDto.creatorId()).isEqualTo(sender.getId());
        assertThat(chatParticipationResponseDto.participantIds()).containsExactlyInAnyOrder(sender.getId(), receiver.getId());

        // 채팅 메시지 전송 검증
        assertThat(this.chatMessageRepository.findAll()).size().isEqualTo(2);

        Long chatroomId = (Long) resultMap.get("chatroomId");
        String chatMessage = (String) resultMap.get("expectedChatMessage");

        validatePerChatMessageType(
                (ChatMessageSendResponseDto) resultMap.get(this.sender.getId() + "ChatMessageSendResponseDto" + ChatMessageType.TEXT),
                chatroomId, sender.getId(), chatMessage
        );
        validatePerChatMessageType(
                (ChatMessageSendResponseDto) resultMap.get(this.sender.getId() + "ChatMessageSendResponseDto" + ChatMessageType.MATCHING_REQUEST),
                chatroomId, sender.getId(), chatMessage
        );

        validatePerChatMessageType(
                (ChatMessageSendResponseDto) resultMap.get(this.receiver.getId() + "ChatMessageSendResponseDto" + ChatMessageType.TEXT),
                chatroomId, sender.getId(), chatMessage
        );
        validatePerChatMessageType(
                (ChatMessageSendResponseDto) resultMap.get(this.receiver.getId() + "ChatMessageSendResponseDto" + ChatMessageType.MATCHING_REQUEST),
                chatroomId, sender.getId(), chatMessage
        );

        this.txManager.rollback(txStatus);
    }

    private void validatePerChatMessageType(ChatMessageSendResponseDto result, Long chatroomId, Long senderId, String chatMessage) {
        assertThat(result.chatMessageId()).isNotNull();
        assertThat(result.chatroomId()).isEqualTo(chatroomId);
        assertThat(result.senderId()).isEqualTo(senderId);
        switch (result.chatMessageType()) {
            case TEXT -> assertThat(result.content()).isEqualTo(Map.of("text", chatMessage));
            case MATCHING_REQUEST -> {
                Map<String, Object> content = (Map<String, Object>) result.content();
                assertThat(content.get("matchingId")).isNotNull();
                assertThat(content.get("meetingPlace")).isEqualTo("MEETINGPLACE");
                assertThat(content.get("meetingPlaceAddress")).isEqualTo("someAddress");
                LocalDateTime actualMeetingTime = LocalDateTime.parse((String) content.get("meetingTime"));
                LocalDateTime expectedMeetingTime = this.now.plusDays(2);

                assertThat(actualMeetingTime.getDayOfYear()).isEqualTo(expectedMeetingTime.getDayOfYear());
                assertThat(actualMeetingTime.getHour()).isEqualTo(expectedMeetingTime.getHour());
                assertThat(actualMeetingTime.getMinute()).isEqualTo(expectedMeetingTime.getMinute());
                assertThat(actualMeetingTime.getSecond()).isEqualTo(expectedMeetingTime.getSecond());
            }
            default -> fail("Something wrong: " + result);
        }
    }

    // 위에서는 채팅 신청 테스트, 여기서는 메시지 전송 테스트
    private void messageSendTest(ChatParticipationResponseDto chatParticipationInfo,
                                 Map<String, Object> resultMap) {
        final String chatMessage = "Hello, World!";

        resultMap.put("expectedChatMessage", chatMessage);
        resultMap.put("chatroomId", chatParticipationInfo.newChatroomId());

        Long chatroomId = chatParticipationInfo.newChatroomId();

        subscribeEach(this.senderSession, this.sender, chatroomId, resultMap);
        subscribeEach(this.receiverSession, this.receiver, chatroomId, resultMap);

        this.senderSession.send("/hf/app/chat/messages/" + chatroomId, Map.of(
                "senderId", this.sender.getId(),
                "chatMessageType", ChatMessageType.TEXT,
                "content", Map.of(
                        "text", chatMessage
                )
        ));

        this.senderSession.send("/hf/app/chat/messages/" + chatroomId, Map.of(
                "senderId", this.sender.getId(),
                "chatMessageType", ChatMessageType.MATCHING_REQUEST,
                "content", Map.of(
                        "matchingTargetId", this.receiver.getId(),
                        "meetingTime", now.plusDays(2),
                        "meetingPlace", "MEETINGPLACE",
                        "meetingPlaceAddress", "someAddress"
                )
        ));
    }

    private void subscribeEach(StompSession session,
                               Member member,
                               Long chatroomId,
                               Map<String, Object> resultMap) {
        session.subscribe("/hf/topic/chat/messages/" + chatroomId, new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return ChatMessageSendResponseDto.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                log.info("payload={}", payload);
                log.info("payload type: {}", payload.getClass());
                ChatMessageSendResponseDto result = (ChatMessageSendResponseDto) payload;
                resultMap.put(member.getId() + "ChatMessageSendResponseDto" + result.chatMessageType(), result);
                latch.countDown();
            }
        });
    }
}