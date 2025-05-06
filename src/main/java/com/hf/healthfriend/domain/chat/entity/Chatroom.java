package com.hf.healthfriend.domain.chat.entity;

import com.hf.healthfriend.domain.BaseTimeEntity;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Chatroom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatroomId;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipation> participations = new ArrayList<>();

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_chat_message_id")
    private ChatMessage lastChatMessage;

    private Integer unreadMessageCount = 0;

    public static Chatroom newChatroom(Member... participants) {
        Chatroom newChatroom = new Chatroom();
        newChatroom.participations = Arrays.stream(participants)
                .map((m) -> new ChatParticipation(newChatroom, m))
                .toList();
        return newChatroom;
    }

    public Chatroom(Long chatroomId) {
        this.chatroomId = chatroomId;
    }

    public Chatroom(List<ChatParticipation> chatParticipations) {
        this.participations = chatParticipations;
    }

    public void addChatParticipation(ChatParticipation chatParticipation) {
        this.participations.add(chatParticipation);
    }

    public void updateLastChatMessage(ChatMessage chatMessage) {
        this.lastChatMessage = chatMessage;
    }

    public void updateUnreadMessageCount(Integer count) {
        this.unreadMessageCount = count;
    }

    public String getLastChatMessageAsText() {
        if (this.lastChatMessage == null) {
            return "";
        }
        return this.lastChatMessage.getMessageAsText();
    }
}
