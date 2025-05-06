package com.hf.healthfriend.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatMessageListResponseDto(
        List<ChatMessageResponseDto> chatMessages,

        @Schema(description = "현재 페이지가 첫 번째 페이지인지 여부")
        boolean isFirst,

        @Schema(description = "현재 페이지가 마지막 페이지인지 여부")
        boolean isLast,

        @Schema(description = "현재 페이지")
        int page,

        @Schema(description = "페이지의 크기 (채팅 메시지를 불러온 개수)")
        int pageSize,

        @Schema(description = "채팅방 참여자 ID 리스트")
        List<Long> participantIds
) {
}
