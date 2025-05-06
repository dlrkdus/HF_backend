package com.hf.healthfriend.domain.chat.dto.request;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class ChatMessageSendRequestDto<T> {
    private Long senderId;
    private ChatMessageType chatMessageType;
    private T content;
}
