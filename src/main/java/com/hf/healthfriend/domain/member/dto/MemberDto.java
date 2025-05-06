package com.hf.healthfriend.domain.member.dto;

import com.hf.healthfriend.domain.member.constant.*;
import com.hf.healthfriend.domain.member.domain.Tier;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.global.util.mapping.BeanMapping;
import com.hf.healthfriend.global.util.mapping.MappingAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@ToString
@BeanMapping(Member.class)
public class MemberDto {

    @MappingAttribute(target = "id")
    private Long memberId;

    @Schema(description = "로그인할 때 사용되는 ID. 소셜 로그인일 경우, 소셜 로그인 email")
    private String loginId;

    private Role role;
    private String name;
    private String email;
    private LocalDateTime creationTime;
    private String nickname;
    private String profileImageUrl;
    private String cd1;
    private String cd2;
    private String cd3;
    private LocalDate birthDate;
    private Gender gender;
    private String introduction;
    private FitnessLevel fitnessLevel;
    private CompanionStyle companionStyle;
    private FitnessEagerness fitnessEagerness;
    private FitnessObjective fitnessObjective;
    private FitnessKind fitnessKind;
    private Long matchedCount;
    private Tier tier;
    private Boolean isWished;

    public static MemberDto of(Member member, String profileImageUrl, Boolean isWished) {
        return MemberDto.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .role(member.getRole())
                .name(member.getName())
                .email(member.getEmail())
                .creationTime(member.getCreationTime())
                .nickname(member.getNickname())
                .profileImageUrl(profileImageUrl)
                .cd1(member.getCd1())
                .cd2(member.getCd2())
                .cd3(member.getCd3())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .introduction(member.getIntroduction())
                .fitnessLevel(member.getFitnessLevel())
                .companionStyle(member.getCompanionStyle())
                .fitnessEagerness(member.getFitnessEagerness())
                .fitnessObjective(member.getFitnessObjective())
                .fitnessKind(member.getFitnessKind())
                .matchedCount(member.getMatchedCount())
                .tier(member.getTier())
                .isWished(isWished)
                .build();
    }
}
