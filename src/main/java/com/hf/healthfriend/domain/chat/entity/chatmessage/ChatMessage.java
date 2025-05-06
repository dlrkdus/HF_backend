package com.hf.healthfriend.domain.chat.entity.chatmessage;

import com.hf.healthfriend.domain.BaseTimeEntity;
import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance
@DiscriminatorColumn(name = "message_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member sender;

    // 서비스에 일대일 채팅만 있으므로 ChatMessage 엔티티에서 채팅 읽었는지 여부 체크
    // 나중에 기능이 확장될 경우, 크게 어려움 없이 기능 확장할 수 있을 듯
    private boolean readByOpponent = false;

    protected ChatMessage(Long chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    protected ChatMessage(Chatroom chatroom, Member sender) {
        this.chatroom = chatroom;
        this.sender = sender;
    }

    /**
     * 이 채팅 메시지를 읽었는지 여부 설정. 메시지 발행자와 메시지를 읽는 회원이 일치하면 채팅 메시지를 읽음 여부에 변화가 없고,
     * 일치하지 않는다면 채팅 메시지 읽음 여부를 true로 변경.
     *
     * @param readerId 채팅을 읽는 회원의 ID
     */
    public void readIfOpponent(Long readerId) {
        // ID만 조회하는 것이기 때문에 추가 쿼리는 날아가지 않음
        if (this.sender.getId().equals(readerId)) {
            this.readByOpponent = true;
        }
    }

    /**
     * 이 채팅 메시지를 읽었는지 여부 설정. 메시지 발행자와 메시지를 읽는 회원이 일치하면 채팅 메시지를 읽음 여부에 변화가 없고,
     * 일치하지 않는다면 채팅 메시지 읽음 여부를 true로 변경.
     *
     * @param reader 채팅을 읽는 회원 엔티티
     */
    public void readIfOpponent(Member reader) {
        readIfOpponent(reader.getId());
    }

    public abstract String getMessageAsText();

    public abstract ChatMessageType getChatMessageType();
}
