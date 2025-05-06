package com.hf.healthfriend.domain.chat.dto.request.content;

import com.hf.healthfriend.domain.chat.constant.MatchingResponseType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class MatchingResponseChatMessageSendRequestContent {

    @NotNull
    private MatchingResponseType matchingResponseType;

    @NotNull
    private Long matchingId;

    @Nullable
    private String cancelMessage;
}
