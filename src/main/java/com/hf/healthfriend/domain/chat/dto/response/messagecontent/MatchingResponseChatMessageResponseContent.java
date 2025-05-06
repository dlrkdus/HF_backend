package com.hf.healthfriend.domain.chat.dto.response.messagecontent;

import com.hf.healthfriend.domain.chat.constant.MatchingResponseType;
import io.swagger.v3.oas.annotations.media.Schema;

public record MatchingResponseChatMessageResponseContent(

        @Schema(description = "매칭 신청의 응답 타입")
        MatchingResponseType matchingResponseType,

        @Schema(description = "매칭 신청을 거절할 경우의 메시지. matchingResponsetype이 " +
                "ACCEPTED일 경우 null")
        String cancelMessage
) {
}
