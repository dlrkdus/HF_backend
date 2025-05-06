package com.hf.healthfriend.domain.chat.service.delegator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.content.MatchingResponseChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.entity.chatmessage.MatchingResponseChatMessage;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;
import com.hf.healthfriend.domain.matching.service.MatchingService;

import java.util.Map;

public class MatchingResponseChatMessageProcessor
        extends AbstractChatMessageProcessorDelegator<MatchingResponseChatMessageSendRequestContent> {
    private static final TypeReference<ChatMessageSendRequestDto<MatchingResponseChatMessageSendRequestContent>> DTO_TYPE =
            new TypeReference<>() {
            };

    private final ChatMessageRepository chatMessageRepository;
    private final MatchingService matchingService;

    public MatchingResponseChatMessageProcessor(ObjectMapper objectMapper,
                                                ChatMessageRepository chatMessageRepository,
                                                MatchingService matchingService) {
        super(objectMapper);
        this.chatMessageRepository = chatMessageRepository;
        this.matchingService = matchingService;
    }

    @Override
    protected TypeReference<ChatMessageSendRequestDto<MatchingResponseChatMessageSendRequestContent>> getTypeReference() {
        return DTO_TYPE;
    }

    @Override
    protected ChatMessageSendResponseDto processSendingMessage(Long chatroomId, ChatMessageSendRequestDto<MatchingResponseChatMessageSendRequestContent> dto) {
        MatchingResponseChatMessage saved = this.chatMessageRepository.saveMessageWithChatroomId(chatroomId, dto);
//        saved.getChatroom().updateLastChatMessage(saved); // TODO: 데드락이 발생해서 일단 텍스트 메시지만 lastMessage로 세팅

        this.matchingService.updateMatchingStatus(dto.getContent().getMatchingId(),
                switch (dto.getContent().getMatchingResponseType()) {
                    case ACCEPTED -> MatchingStatus.ACCEPTED;
                    case REJECTED -> MatchingStatus.REJECTED;
                });

        return ChatMessageSendResponseDto.builder()
                .chatMessageId(saved.getChatMessageId())
                .chatroomId(chatroomId)
                .senderId(dto.getSenderId())
                .creationTime(saved.getCreationTime())
                .lastModified(saved.getLastModified())
                .chatMessageType(dto.getChatMessageType())
                .content(Map.of(
                        "matchingResponseType", dto.getContent().getMatchingResponseType().name(),
                        "cancelMessage", dto.getContent().getCancelMessage() == null ? "" : dto.getContent().getCancelMessage()
                ))
                .build();
    }
}
