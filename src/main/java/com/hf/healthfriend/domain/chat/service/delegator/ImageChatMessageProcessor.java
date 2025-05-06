package com.hf.healthfriend.domain.chat.service.delegator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.content.ImageChatMessageSendRequestContent;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.global.file.FileUrlResolver;

public class ImageChatMessageProcessor extends AbstractChatMessageProcessorDelegator<ImageChatMessageSendRequestContent> {
    private static final TypeReference<ChatMessageSendRequestDto<ImageChatMessageSendRequestContent>> DTO_TYPE =
            new TypeReference<>() {
            };

    private final ChatMessageRepository chatMessageRepository;
    private final FileUrlResolver fileUrlResolver;

    public ImageChatMessageProcessor(ObjectMapper objectMapper,
                                     ChatMessageRepository chatMessageRepository,
                                     FileUrlResolver fileUrlResolver) {
        super(objectMapper);
        this.chatMessageRepository = chatMessageRepository;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    protected TypeReference<ChatMessageSendRequestDto<ImageChatMessageSendRequestContent>> getTypeReference() {
        return DTO_TYPE;
    }

    @Override
    protected ChatMessageSendResponseDto processSendingMessage(Long chatroomId, ChatMessageSendRequestDto<ImageChatMessageSendRequestContent> dto) {
        // TODO: 채팅방 사진 업로드를 어떻게 다룰까?
        throw new UnsupportedOperationException();
    }
}
