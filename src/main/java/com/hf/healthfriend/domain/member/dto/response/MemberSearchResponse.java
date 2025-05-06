package com.hf.healthfriend.domain.member.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record MemberSearchResponse(
        List<MemberListResponse> memberList,
        Long totalPageSize
) {
}
