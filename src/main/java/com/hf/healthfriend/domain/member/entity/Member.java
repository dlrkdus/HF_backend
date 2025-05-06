package com.hf.healthfriend.domain.member.entity;

import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;
import com.hf.healthfriend.domain.matching.entity.Matching;
import com.hf.healthfriend.domain.member.constant.*;
import com.hf.healthfriend.domain.member.domain.Tier;
import com.hf.healthfriend.domain.member.repository.dto.MemberUpdateDto;
import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.review.entity.Review;
import com.hf.healthfriend.domain.spec.entity.Spec;
import com.hf.healthfriend.domain.wish.entity.Wish;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Setter // TODO: Setter를 없애고 엔티티 수정 코드는 udpate~ 메소드로 대체해야 함
@Getter
@ToString
@Table(name = "members", indexes = {
        @Index(name = "member_fitness_level_idx", columnList = "fitnessLevel")
})
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_MEMBER;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password = null;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationTime = LocalDateTime.now();

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_url")
    private String profileImageUrl;

    @Column(name = "cd1", nullable = false)
    private String cd1;

    @Column(name = "cd2", nullable = false)
    private String cd2;

    @Column(name = "cd3", nullable = false)
    private String cd3;

    @Column(name = "birth_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "introduction", nullable = false)
    private String introduction;

    @Column(name = "fitness_level")
    @Enumerated(EnumType.STRING)
    private FitnessLevel fitnessLevel;

    @Column(name = "companion_style")
    @Enumerated(EnumType.STRING)
    private CompanionStyle companionStyle;

    @Column(name = "fitness_eagerness")
    @Enumerated(EnumType.STRING)
    private FitnessEagerness fitnessEagerness;

    @Column(name = "fitness_objective")
    @Enumerated(EnumType.STRING)
    private FitnessObjective fitnessObjective;

    @Column(name = "fitness_kind")
    @Enumerated(EnumType.STRING)
    private FitnessKind fitnessKind;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Spec> specs = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchingRequests = new ArrayList<>();

    @OneToMany(mappedBy = "targetMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchingsReceived = new ArrayList<>();

    @OneToMany(mappedBy = "wished", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wish> wishedList = new ArrayList<>();

    @OneToMany(mappedBy = "wisher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wish> wisherList = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewsWrote = new ArrayList<>();

    @OneToMany(mappedBy = "reviewee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewsReceived = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipation> chatParticipations = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Column(name = "review_score")
    private Double reviewScore = 0.0;

    @Column(name = "matched_count")
    private Long matchedCount = 0L;

    @Column(name = "wished_count")
    private Long wishedCount = 0L;

    public Member(long memberId) {
        this.id = memberId;
    }

    public Member(String loginId) {
        this.loginId = loginId;
        this.email = loginId;
    }

    public Member(String loginId, String password) {
        this.loginId = loginId;
        this.email = loginId;
        this.password = password;
    }

    public Member(String loginId, String email, String password) {
        this.loginId = loginId;
        this.email = email;
        this.password = password;
    }

    public void addSpec(Spec spec) {
        this.specs.add(spec);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(this.role.name());
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.id);
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void undelete() {
        this.isDeleted = false;
    }

    public void incrementMatchedCount() {
        this.matchedCount++;
    }

    public void incrementWishedCount() {
        this.wishedCount++;
    }

    public void decrementWishedCount() {
        this.wishedCount--;
    }

    public Tier getTier() {
        return Tier.create(this.fitnessLevel, this.matchedCount);
    }

    public void update(MemberUpdateDto updateDto) {
        if (updateDto.getNickname() != null) {
            this.nickname = updateDto.getNickname();
        }
        if (updateDto.getProfileImageUrl() != null) {
            this.profileImageUrl = updateDto.getProfileImageUrl();
        }
        if (updateDto.getCd1() != null) {
            this.cd1 = updateDto.getCd1();
        }
        if (updateDto.getCd2() != null) {
            this.cd2 = updateDto.getCd2();
        }
        if (updateDto.getCd3() != null) {
            this.cd3 = updateDto.getCd3();
        }
        if (updateDto.getIntroduction() != null) {
            this.introduction = updateDto.getIntroduction();
        }
        if (updateDto.getFitnessLevel() != null) {
            this.fitnessLevel = updateDto.getFitnessLevel();
        }
        if (updateDto.getCompanionStyle() != null) {
            this.companionStyle = updateDto.getCompanionStyle();
        }
        if (updateDto.getFitnessEagerness() != null) {
            this.fitnessEagerness = updateDto.getFitnessEagerness();
        }
        if (updateDto.getFitnessObjective() != null) {
            this.fitnessObjective = updateDto.getFitnessObjective();
        }
        if (updateDto.getFitnessKind() != null) {
            this.fitnessKind = updateDto.getFitnessKind();
        }
    }

    public void addChatParticipation(ChatParticipation chatParticipation) {
        this.chatParticipations.add(chatParticipation);
    }
}
