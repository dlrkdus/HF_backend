package com.hf.healthfriend.domain.chat.dto.request.content;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class MatchingRequestChatMessageSendRequestContent {

    @NotNull
    private Long matchingTargetId;

    @NotNull
    @Future
    private LocalDateTime meetingTime;

    @NotNull
    @NotEmpty
    private String meetingPlace;

    @NotNull
    private String meetingPlaceAddress;
}
