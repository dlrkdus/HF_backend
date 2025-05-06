package com.hf.healthfriend.domain.chat.controller;

import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.ChatParticipationRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatParticipationResponseDto;
import com.hf.healthfriend.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessagingController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/request")
    public void requestChat(@Payload ChatParticipationRequestDto dto) {
        log.debug("payload={}", dto);
        ChatParticipationResponseDto result = this.chatService.requestChat(dto);
        result.participantIds().forEach((participantId) ->
                this.messagingTemplate.convertAndSendToUser(String.valueOf(participantId),
                        "chat/request", result));
    }


    @MessageMapping("/chat/messages/{chatroomId}")
    public void sendChatMessage(@DestinationVariable("chatroomId") Long chatroomId,
                                @Payload ChatMessageSendRequestDto<Object> messageDto) {
        ChatMessageSendResponseDto result = this.chatService.sendMessage(chatroomId, messageDto);
        if (log.isDebugEnabled()) {
            log.debug("request chatroomId={}", chatroomId);
            log.debug("request payload={}", messageDto);
            log.debug("response={}", result);
        }
        this.messagingTemplate.convertAndSend(
                "/hf/topic/chat/messages/" + result.chatroomId(),
                result
        );
    }
}
