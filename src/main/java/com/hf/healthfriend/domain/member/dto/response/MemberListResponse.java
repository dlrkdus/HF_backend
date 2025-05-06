package com.hf.healthfriend.domain.member.dto.response;

import com.hf.healthfriend.domain.member.constant.FitnessLevel;
import com.hf.healthfriend.domain.member.domain.Tier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MemberListResponse {
    private Long memberId;
    private String profileImageUrl;
    private String introduction;
    private String nickname;
    private Long wishCount;
    private Double reviewScore;
    //private Long level;
    private Long matchedCount;
    private String fitnessLevel;
    private String companionStyle;
    private String fitnessEagerness;
    private String fitnessKind;
    private String fitnessObjective;
    private Tier tier;

    public MemberListResponse(Long memberId,
                              String profileImageUrl,
                              String introduction,
                              String nickname,
                              Long wishCount,
                              Double reviewScore,
                              Long matchedCount,
                              String fitnessLevel,
                              String companionStyle,
                              String fitnessEagerness,
                              String fitnessKind,
                              String fitnessObjective) {
        this.memberId = memberId;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.nickname = nickname;
        this.wishCount = wishCount;
        this.reviewScore = reviewScore;
        this.matchedCount = matchedCount;
        this.fitnessLevel = fitnessLevel;
        this.companionStyle = companionStyle;
        this.fitnessEagerness = fitnessEagerness;
        this.fitnessKind = fitnessKind;
        this.fitnessObjective = fitnessObjective;
        this.tier = Tier.create(FitnessLevel.valueOf(fitnessLevel), matchedCount);
    }
}
