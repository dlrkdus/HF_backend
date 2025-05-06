package com.hf.healthfriend.domain.member.repository;

import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.querydsl.MemberCustomRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {

    @Query("""
            SELECT m
            FROM Member m
            WHERE m.id = :memberId AND m.isDeleted = false
            """)
    Optional<Member> findNotDeletedMemberById(Long memberId);

    @Query("""
            SELECT m
            FROM Member m
            WHERE m.email = :email AND m.isDeleted = false
            """)
    Optional<Member> findNotDeletedMemberByEmail(String email);

    Optional<Member> findByLoginId(String loginId);

    @Query(value = """
            SELECT m
            FROM Member m
            WHERE m.loginId = :loginId AND m.isDeleted = false
            """)
    Optional<Member> findNotDeletedMemberByLoginId(String loginId);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByLoginIdAndIsDeletedFalse(String loginId);

    boolean existsByNicknameAndIsDeletedFalse(String nickname);

    @Transactional
    @Modifying // 조회가 아닌 변경성 작업에는 해당 어노테이션을 붙여줘야 함
    @Query(value = "UPDATE members m SET m.review_score=:reviewScore WHERE m.member_id=:memberId ", nativeQuery = true)
    void updateMemberReviewScore(@Param("memberId") long memberId, @Param("reviewScore") double reviewScore);

    @Modifying
    @Query(value = "UPDATE members m SET m.wished_count = m.wished_count + 1 WHERE m.member_id = :memberId", nativeQuery = true)
    void incrementWishedCountByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query(value = """
            UPDATE members m SET m.wished_count = m.wished_count - 1
            WHERE m.member_id = :memberId
                AND m.wished_count > 0""", nativeQuery = true)
    void decrementWishedCountByMemberId(@Param("memberId") Long memberId);

    Long findMemberIdByLoginIdAndIsDeletedFalse(String loginId);
}
