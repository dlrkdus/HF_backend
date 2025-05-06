package com.hf.healthfriend.domain.chat.service.delegator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractChatMessageProcessorDelegator<C> implements ChatMessageProcessor {
    private final ObjectMapper objectMapper;

    @Override
    public ChatMessageSendResponseDto sendMessage(Long chatroomId, ChatMessageSendRequestDto<Object> dto) {
        return processSendingMessage(chatroomId, this.objectMapper.convertValue(dto, getTypeReference()));
    }

    protected abstract TypeReference<ChatMessageSendRequestDto<C>> getTypeReference();

    protected abstract ChatMessageSendResponseDto processSendingMessage(Long chatroomId, ChatMessageSendRequestDto<C> dto);
}
