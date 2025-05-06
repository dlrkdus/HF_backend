package com.hf.healthfriend.domain.chat.dto.response;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResponseDto(
        Long chatMessageId,
        Long senderId,
        LocalDateTime creationTime,
        LocalDateTime lastModified,
        ChatMessageType chatMessageType,
        Object content,
        boolean read
) {
}
