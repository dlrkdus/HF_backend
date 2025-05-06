package com.hf.healthfriend.domain.chat.dto.request.content;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class TextChatMessageSendRequestContent {

    @NotEmpty
    private String text;
}
