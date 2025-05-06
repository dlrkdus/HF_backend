package com.hf.healthfriend.domain.chat.constant;

import com.hf.healthfriend.domain.chat.service.delegator.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum ChatMessageType {
    IMAGE(ImageChatMessageProcessor.class),
    MATCHING_REQUEST(MatchingRequestChatMessageProcessor.class),
    MATCHING_RESPONSE(MatchingResponseChatMessageProcessor.class),
    TEXT(TextChatMessageProcessor.class);

    private final Class<? extends ChatMessageProcessor> processorClass;
}
