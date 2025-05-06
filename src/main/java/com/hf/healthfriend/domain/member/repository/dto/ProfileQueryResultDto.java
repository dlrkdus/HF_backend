package com.hf.healthfriend.domain.member.repository.dto;

import com.hf.healthfriend.domain.spec.dto.SpecDto;

import java.util.List;

public record ProfileQueryResultDto(
        Long memberId,
        String introduction,
        List<SpecDto> specs,
        Double averageReviewScore,
        long matchingCount,
        long wishedCount
) {
}
