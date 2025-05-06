package com.hf.healthfriend.domain.chat.dto.response;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageSendResponseDto(
        Long chatMessageId,
        Long chatroomId,
        Long senderId,
        LocalDateTime creationTime,
        LocalDateTime lastModified,
        ChatMessageType chatMessageType,
        Object content
) {
}
