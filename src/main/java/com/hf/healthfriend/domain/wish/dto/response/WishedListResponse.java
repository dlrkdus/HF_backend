package com.hf.healthfriend.domain.wish.dto.response;

import lombok.Builder;

@Builder
public record WishedListResponse(
        Long wishedId,
        String imageUrl,
        String wishedNickname
) {
}
