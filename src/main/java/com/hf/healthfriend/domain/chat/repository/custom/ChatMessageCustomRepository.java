package com.hf.healthfriend.domain.chat.repository.custom;

import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;

public interface ChatMessageCustomRepository {

    <D extends ChatMessage> D saveMessageWithChatroomId(Long chatroomId,
                                                        ChatMessageSendRequestDto<?> dto);
}
