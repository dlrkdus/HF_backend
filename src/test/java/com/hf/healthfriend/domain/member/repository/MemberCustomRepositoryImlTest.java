package com.hf.healthfriend.domain.member.repository;

import com.hf.healthfriend.domain.member.constant.*;
import com.hf.healthfriend.domain.member.dto.request.MembersSearchRequest;
import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.dto.ProfileQueryResultDto;
import com.hf.healthfriend.domain.member.repository.querydsl.MemberCustomRepositoryImpl;
import com.hf.healthfriend.domain.wish.entity.Wish;
import com.hf.healthfriend.domain.wish.repository.WishRepository;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ActiveProfiles("test")
@Import(TestConfig.class)
@DataJpaTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemberCustomRepositoryImlTest {

    @Autowired
    private MemberCustomRepositoryImpl memberCustomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private TestEntityManager entityManager;

    private MembersSearchRequest request;

    @BeforeEach
    public void setUp(){
        request = MembersSearchRequest.builder()
                .companionStyles(List.of(String.valueOf(CompanionStyle.GROUP)))
                .fitnessKinds(List.of(String.valueOf(FitnessKind.FUNCTIONAL)))
                .fitnessObjectives(List.of(String.valueOf(FitnessObjective.RUNNING)))
                .fitnessEagernesses(null)
                .memberSortType(MemberSortType.MATCHING_COUNT)
                .build();
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Member member = Member.builder()
                    .loginId("virtualUser"+i)
                    .role(Role.ROLE_MEMBER)
                    .name("Virtual User"+ i)
                    .email("virtual@user.com"+i)
                    .password("password")
                    .nickname("VirtualNickname"+(char) ('A'+i-1))
                    .cd1("01")
                    .cd2("001")
                    .cd3("003")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .introduction("This is a virtual member"+ i)
                    .fitnessLevel(FitnessLevel.BEGINNER)
                    .companionStyle(CompanionStyle.GROUP)
                    .fitnessEagerness(FitnessEagerness.EAGER)
                    .fitnessObjective(FitnessObjective.BULK_UP)
                    .fitnessKind(FitnessKind.HIGH_STRESS)
                    .build();
            memberRepository.save(member);
            members.add(member);
        }
        for(int i = 0; i<=18; i++){
            Wish wish = new Wish(members.get(i),members.get(i+1));
            wishRepository.save(wish);
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("filtering test")
    public void filteringTest() {
        // Given
        BooleanBuilder builder = memberCustomRepository.filter(null,request);
        // When
        Predicate predicate = builder.getValue();
        // Then
        assertThat(predicate.toString()).contains("str(member1.companionStyle) = GROUP");
        assertThat(predicate.toString()).contains("str(member1.fitnessKind) = FUNCTIONAL");
        assertThat(predicate.toString()).contains("str(member1.fitnessObjective) = RUNNING");
        assertThat(predicate.toString()).doesNotContain("str(member1.fitnessEagerness) = EAGER");
    }

    @Test
    @DisplayName("profile search test")
    public void profileSearchTest() {
        //Given
        String keyword = "VirtualNicknameB";
        Pageable pageable = PageRequest.of(0, 10);
        MembersSearchRequest new_request = MembersSearchRequest.builder()
                .build();

        //When
        List<MemberListResponse> searchedProfileList = memberCustomRepository.searchMembers(keyword,new_request, pageable);

        //Then
        assertEquals(1, searchedProfileList.size());
        assertEquals("This is a virtual member2",searchedProfileList.get(0).getIntroduction());
    }

    @DisplayName("findByMemberId - success")
    @Test
    void findByMemberId_success() {
        // Given
        Member sampleMember = SampleEntityGenerator.generateSampleMember("sample@gmail.com", "samplenickname");
        this.memberRepository.save(sampleMember);
        this.entityManager.flush();
        this.entityManager.clear();

        // When
        Optional<Member> memberOp = this.memberCustomRepository.findByMemberId(sampleMember.getId());

        // Then
        assertThat(memberOp).isNotEmpty();

        Member member = memberOp.get();

        System.out.println("findMember ID=" + member.getId());
        System.out.println("findMember Email=" + member.getEmail());
        System.out.println("findMember Nickname=" + member.getNickname());

        assertThat(member.getId()).isEqualTo(sampleMember.getId());
        assertThat(member.getEmail()).isEqualTo(sampleMember.getEmail());
        assertThat(member.getNickname()).isEqualTo(sampleMember.getNickname());
    }

    @Test
    @DisplayName("findProfileByMemberId() - 스펙, 리뷰가 없는 회원 조회 시 쿼리 성공해야 함")
    void findProfileByMemberId_noReviewOrSpecs_success() {
        // Given
        Member sampleMember = SampleEntityGenerator.generateSampleMember("sample1@gmail.com", "sample1");
        this.memberRepository.save(sampleMember);

        // When
        Optional<ProfileQueryResultDto> result = this.memberCustomRepository.findProfileByMemberId(sampleMember.getId());

        // Then
        assertThat(result).isNotEmpty();

        ProfileQueryResultDto profileQueryResultDto = result.get();

        log.info("result={}", profileQueryResultDto);

        assertThat(profileQueryResultDto.specs()).isEmpty();
        assertThat(profileQueryResultDto.averageReviewScore()).isZero();
    }
}
