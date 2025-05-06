package com.hf.healthfriend.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class ChatParticipationRequestDto {

    @Schema(description = "채팅 신청을 거는 회원 ID")
    @NotNull
    @Min(1)
    private Long requesterId;

    @Schema(description = "채팅 신청 받는 회원 ID")
    @NotNull
    @Min(1)
    private Long chatTargetId;
}
