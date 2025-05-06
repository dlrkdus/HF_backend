package com.hf.healthfriend.domain.chat.entity;

import com.hf.healthfriend.domain.BaseTimeEntity;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatParticipation extends BaseTimeEntity {

    @EmbeddedId
    private ChatParticipationId chatParticipationId;

    @MapsId("chatroomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "is_notification_active")
    private boolean notificationActive = true;

    @Column(name = "is_discononected")
    private boolean disconnected = false;

    public ChatParticipation(ChatParticipationId id) {
        this.chatParticipationId = id;
    }

    public ChatParticipation(Chatroom chatroom, Member member) {
        this(new ChatParticipationId(chatroom.getChatroomId(), member.getId()));
        this.chatroom = chatroom;
        this.member = member;
    }
}
