package com.hf.healthfriend.domain.chat.service.delegator;

import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;

public interface ChatMessageProcessor {

    ChatMessageSendResponseDto sendMessage(Long chatroomId, ChatMessageSendRequestDto<Object> dto);
}
