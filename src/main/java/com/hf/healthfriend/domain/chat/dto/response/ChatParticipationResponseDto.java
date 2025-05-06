package com.hf.healthfriend.domain.chat.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatParticipationResponseDto(
        Long newChatroomId,
        List<Long> participantIds,
        Long creatorId
) {
}
