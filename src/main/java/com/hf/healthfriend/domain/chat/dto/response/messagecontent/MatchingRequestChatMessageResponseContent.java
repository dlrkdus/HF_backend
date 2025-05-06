package com.hf.healthfriend.domain.chat.dto.response.messagecontent;

import java.time.LocalDateTime;

public record MatchingRequestChatMessageResponseContent(
        LocalDateTime meetingTime,
        String meetingPlace,
        String meetingPlaceAddress
) {
}
