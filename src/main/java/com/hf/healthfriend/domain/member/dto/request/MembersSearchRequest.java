package com.hf.healthfriend.domain.member.dto.request;

import com.hf.healthfriend.domain.member.constant.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class MembersSearchRequest {

    // 필터링
    private String cd1;

    private String cd2;

    private String cd3;

    private List<String> fitnessLevels;

    private List<String> companionStyles;

    private List<String> fitnessEagernesses;

    private List<String> fitnessKinds;

    private List<String> fitnessObjectives;

    // 정렬
    private MemberSortType memberSortType;

}
