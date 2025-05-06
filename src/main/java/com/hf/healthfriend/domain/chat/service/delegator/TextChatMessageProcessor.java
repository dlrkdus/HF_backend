package com.hf.healthfriend.domain.chat.service.delegator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.content.TextChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.entity.chatmessage.TextChatMessage;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;

import java.util.Map;

public class TextChatMessageProcessor extends AbstractChatMessageProcessorDelegator<TextChatMessageSendRequestContent> {
    private static TypeReference<ChatMessageSendRequestDto<TextChatMessageSendRequestContent>> DTO_TYPE =
            new TypeReference<>() {
            };

    private final ChatMessageRepository chatMessageRepository;

    public TextChatMessageProcessor(ObjectMapper objectMapper, ChatMessageRepository chatMessageRepository) {
        super(objectMapper);
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    protected TypeReference<ChatMessageSendRequestDto<TextChatMessageSendRequestContent>> getTypeReference() {
        return DTO_TYPE;
    }

    @Override
    protected ChatMessageSendResponseDto processSendingMessage(Long chatroomId, ChatMessageSendRequestDto<TextChatMessageSendRequestContent> dto) {
        TextChatMessage savedMessage = this.chatMessageRepository.saveMessageWithChatroomId(chatroomId, dto);
        savedMessage.getChatroom().updateLastChatMessage(savedMessage);

        return ChatMessageSendResponseDto.builder()
                .chatMessageId(savedMessage.getChatMessageId())
                .chatroomId(chatroomId)
                .senderId(dto.getSenderId())
                .creationTime(savedMessage.getCreationTime())
                .lastModified(savedMessage.getLastModified())
                .chatMessageType(dto.getChatMessageType())
                .content(Map.of("text", dto.getContent().getText()))
                .build();
    }
}
