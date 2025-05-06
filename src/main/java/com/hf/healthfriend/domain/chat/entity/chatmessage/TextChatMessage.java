package com.hf.healthfriend.domain.chat.entity.chatmessage;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TEXT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TextChatMessage extends ChatMessage {
    private String text;

    public TextChatMessage(Chatroom chatroom, Member sender, String text) {
        super(chatroom, sender);
        this.text = text;
    }

    @Override
    public String getMessageAsText() {
        return this.text;
    }

    @Override
    public ChatMessageType getChatMessageType() {
        return ChatMessageType.TEXT;
    }
}
