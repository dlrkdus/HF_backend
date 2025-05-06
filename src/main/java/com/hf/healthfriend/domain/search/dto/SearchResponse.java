package com.hf.healthfriend.domain.search.dto;

import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.post.dto.response.PostListObject;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchResponse(
        List<PostListObject> postList,
        Long postListSize,
        List<MemberListResponse> profileList,
        Long profileListSize,
        List<String> recentSearchList
) {
}
