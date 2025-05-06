package com.hf.healthfriend.domain.member.repository.querydsl;

import com.hf.healthfriend.domain.member.dto.request.MembersSearchRequest;
import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.dto.MemberUpdateDto;
import com.hf.healthfriend.domain.member.repository.dto.ProfileQueryResultDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberCustomRepository {

    Optional<Member> findByMemberId(Long memberId);

    List<MemberListResponse> searchMembers(String keyword, MembersSearchRequest request, Pageable pageable);

    Long getSearchedMembersSize(String keyword, MembersSearchRequest request, Pageable pageable);

    Member update(Long memberId, MemberUpdateDto updateDto);

    Optional<ProfileQueryResultDto> findProfileByMemberId(Long memberId);

    Long getTotalPageSize(int size);
}
