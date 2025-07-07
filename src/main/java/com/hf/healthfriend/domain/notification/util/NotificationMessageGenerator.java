package com.hf.healthfriend.domain.notification.util;

import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageGenerator {
    public String generateMessage(NotificationEvent event) {
        return switch (event.getType()) {
            case ADD_COMMENT_TO_COMMENT -> event.getActor() + "님이 회원님의 댓글에 답글을 남겼습니다.";
            case ADD_COMMENT_TO_POST -> event.getActor() + "님이 회원님의 글에 댓글을 작성했습니다.";
            case ADD_LIKE_TO_COMMENT -> event.getActor() + "님이 회원님의 댓글을 좋아합니다.";
            case ADD_LIKE_TO_POST -> event.getActor() + "님이 회원님의 글을 좋아합니다.";
            case POPULAR_POST -> "회원님의 글이 인기글이 되었습니다!";
            case MATCH_REQUEST -> "1:1 매칭 신청이 들어왔습니다.";
            case MATCH_ACCEPT -> event.getActor() + "님이 매칭 신청을 수락했습니다.";
            case MATCH_REJECT -> event.getActor() + "님이 매칭 신청을 거절했습니다.";
            case MATCH_PUNK -> event.getActor() + "님의 신청으로 매칭이 중단 되었습니다.";
            case MATCH_END_REVIEW -> event.getActor() + "님이 후기를 작성했습니다.";
        };
    }
}
