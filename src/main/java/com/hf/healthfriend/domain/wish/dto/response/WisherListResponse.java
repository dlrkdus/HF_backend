package com.hf.healthfriend.domain.wish.dto.response;

import lombok.Builder;

@Builder
public record WisherListResponse(
        Long wisherId,
        String imageUrl,
        String wisherNickname
) {
}
