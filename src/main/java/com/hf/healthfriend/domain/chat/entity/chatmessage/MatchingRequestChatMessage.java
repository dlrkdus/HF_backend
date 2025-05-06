package com.hf.healthfriend.domain.chat.entity.chatmessage;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("MAT_REQ")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MatchingRequestChatMessage extends ChatMessage {
    private LocalDateTime meetingTime;
    private String meetingPlace;
    private String meetingPlaceAddress;

    public MatchingRequestChatMessage(Long chatMessageId) {
        super(chatMessageId);
    }

    public MatchingRequestChatMessage(Chatroom chatroom,
                                      Member sender,
                                      LocalDateTime meetingTime,
                                      String meetingPlace,
                                      String meetingPlaceAddress) {
        super(chatroom, sender);
        this.meetingTime = meetingTime;
        this.meetingPlace = meetingPlace;
        this.meetingPlaceAddress = meetingPlaceAddress;
    }

    @Override
    public String getMessageAsText() {
        return "매칭 신청이 도착했습니다";
    }

    @Override
    public ChatMessageType getChatMessageType() {
        return ChatMessageType.MATCHING_REQUEST;
    }
}
