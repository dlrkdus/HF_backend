package com.hf.healthfriend.domain.chat.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * <p>하나의 Chatroom에 대한 한 명의 Member의 참여는 오직 하나만 존재하기 때문에
 * ChatParticipation의 PK를 복합키로 설정. 이를 통해 ChatParticipation의 중복 삽입을 체크하는 로직 없이
 * 중복 삽입 방지
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class ChatParticipationId implements Serializable {
    private Long chatroomId;
    private Long memberId;
}
