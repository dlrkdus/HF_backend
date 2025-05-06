package com.hf.healthfriend.domain.chat.entity.chatmessage;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ImageChatMessage extends ChatMessage {
    private String imageUrl;

    public ImageChatMessage(Long chatMessageId) {
        super(chatMessageId);
    }

    public ImageChatMessage(Chatroom chatroom, Member sender, String imageUrl) {
        super(chatroom, sender);
        this.imageUrl = imageUrl;
    }

    @Override
    public String getMessageAsText() {
        return "이미지";
    }

    @Override
    public ChatMessageType getChatMessageType() {
        return ChatMessageType.IMAGE;
    }
}
