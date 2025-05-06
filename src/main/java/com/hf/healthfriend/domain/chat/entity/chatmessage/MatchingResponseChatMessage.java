package com.hf.healthfriend.domain.chat.entity.chatmessage;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.constant.MatchingResponseType;
import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("MAT_RESP")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MatchingResponseChatMessage extends ChatMessage {
    private MatchingResponseType matchingResponseType;
    private String cancelMessage; // 매칭 수락일 경우 null

    public MatchingResponseChatMessage(Long chatMessageId) {
        super(chatMessageId);
    }

    public MatchingResponseChatMessage(Chatroom chatroom,
                                       Member sender,
                                       MatchingResponseType matchingResponseType) {
        super(chatroom, sender);
        this.matchingResponseType = matchingResponseType;
    }

    public MatchingResponseChatMessage(Chatroom chatroom,
                                       Member sender,
                                       MatchingResponseType matchingResponseType,
                                       String cancelMessage) {
        this(chatroom, sender, matchingResponseType);
        this.cancelMessage = cancelMessage;
    }

    public static MatchingResponseChatMessage createAcceptanceMessage(Chatroom chatroom, Member sender) {
        return new MatchingResponseChatMessage(chatroom, sender, MatchingResponseType.ACCEPTED);
    }

    public static MatchingResponseChatMessage createRejectMessage(Chatroom chatroom,
                                                                  Member sender,
                                                                  String cancelMessage) {
        return new MatchingResponseChatMessage(chatroom,
                sender,
                MatchingResponseType.REJECTED,
                cancelMessage);
    }

    @Override
    public String getMessageAsText() {
        return "매칭 신청에 대한 응답이 도착했습니다";
    }

    @Override
    public ChatMessageType getChatMessageType() {
        return ChatMessageType.MATCHING_RESPONSE;
    }
}
