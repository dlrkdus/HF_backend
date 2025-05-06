package com.hf.healthfriend.domain.matching.dto.response;

import com.hf.healthfriend.domain.member.constant.*;
import lombok.Builder;

@Builder
public record ProfileOfMemberInMatchingResponseDto(
        Long memberId,
        String nickname,
        String profileImageUrl,
        FitnessLevel fitnessLevel,
        CompanionStyle companionStyle,
        FitnessEagerness fitnessEagerness,
        FitnessKind fitnessKind,
        FitnessObjective fitnessObjective,
        String cd1,
        String cd2,
        String cd3,
        Long matchedCount
) {
}
