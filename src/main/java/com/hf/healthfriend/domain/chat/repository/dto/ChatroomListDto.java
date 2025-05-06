package com.hf.healthfriend.domain.chat.repository.dto;

import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.matching.constant.MatchingStatus;

public record ChatroomListDto(
        Chatroom chatroom,
        Long opponentParticipantId,
        String opponentParticipantNickname,
        String opponentParticipantProfileImageUrl,
        MatchingStatus matchingStatus
) {
}
