package com.hf.healthfriend.domain.post.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PostSearchResponse(
        List<PostListObject> postList,
        Long totalPageSize
) {
}
