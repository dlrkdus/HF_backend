package com.hf.healthfriend.domain.wish.service;

import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.wish.dto.request.WishRequestDto;
import com.hf.healthfriend.domain.wish.dto.response.WishedListResponse;
import com.hf.healthfriend.domain.wish.dto.response.WisherListResponse;
import com.hf.healthfriend.domain.wish.entity.Wish;
import com.hf.healthfriend.domain.wish.exception.WishException;
import com.hf.healthfriend.domain.wish.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WishServiceTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private WishService wishService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("찜 성공")
    void testSaveWish_Success() {
        long wisherId = 1L;
        long wishedId = 2L;
        Member wisher = new Member(wisherId);
        Member wished = new Member(wishedId);

        // 중복 찜 여부 체크 (찜이 존재하지 않는다고 가정)
        when(wishRepository.existsByWishedIdAndWisherIdAndIsDeletedFalse(wishedId, wisherId)).thenReturn(false);

        // Member 조회 시 정상적으로 객체 반환하도록 설정
        when(memberRepository.findById(wisherId)).thenReturn(Optional.of(wisher));
        when(memberRepository.findById(wishedId)).thenReturn(Optional.of(wished));

        // 찜 저장 시 반환 객체 설정
        Wish savedWish = new Wish(wisher, wished);
        when(wishRepository.save(any(Wish.class))).thenReturn(savedWish);

        WishRequestDto wishRequestDto = new WishRequestDto(wisherId, wishedId);
        Long wishId = wishService.save(wishRequestDto);

        verify(wishRepository, times(1)).save(any(Wish.class));
    }

    @Test
    @DisplayName("이미 존재하는 찜이면 예외")
    void testSaveWish_AlreadyExists() {
        long wisherId = 1L;
        long wishedId = 2L;

        when(wishRepository.existsByWishedIdAndWisherIdAndIsDeletedFalse(wishedId, wisherId)).thenReturn(true);

        WishRequestDto wishRequestDto = new WishRequestDto(1L,2L);

        WishException exception = assertThrows(WishException.class, () -> {
            wishService.save(wishRequestDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(wishRepository, never()).save(any(Wish.class));
    }

    @Test
    @DisplayName("멤버가 존재하지 않을 때")
    void testSaveWish_MemberNotFound() {
        long wisherId = 1L;
        long wishedId = 2L;

        when(wishRepository.existsByWishedIdAndWisherIdAndIsDeletedFalse(wishedId, wisherId)).thenReturn(false);
        when(memberRepository.existsById(wisherId)).thenReturn(false);

        WishRequestDto wishRequestDto = new WishRequestDto(1L,2L);

        WishException exception = assertThrows(WishException.class, () -> {
            wishService.save(wishRequestDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(wishRepository, never()).save(any(Wish.class));
    }

    @Test
    @DisplayName("Soft Delete 테스트")
    void testDeleteWish_Success() {
        Member wisher = Member.builder().id(1L).profileImageUrl(null).nickname(null).wishedCount(0L).build();
        Member wished = Member.builder().id(2L).profileImageUrl(null).nickname(null).wishedCount(0L).build();
        WishRequestDto wishRequestDto = new WishRequestDto(1L,2L);

        Wish wish = new Wish(wisher, wished);

        when(wishRepository.findByWisherIdAndWishedIdAndIsDeletedFalse(1L,2L)).thenReturn(Optional.of(wish));

        wishService.delete(wishRequestDto);

        verify(wishRepository, times(1)).findByWisherIdAndWishedIdAndIsDeletedFalse(1L,2L);
        assertTrue(wish.isDeleted());
    }

    @Test
    @DisplayName("존재하지 않는 찜을 삭제하려 할 때")
    void testDeleteWish_NotFound() {
        WishRequestDto wishRequestDto = new WishRequestDto(1L,2L);

        when(wishRepository.findByWisherIdAndWishedIdAndIsDeletedFalse(1L,2L)).thenReturn(Optional.empty());

        WishException exception = assertThrows(WishException.class, () -> {
            wishService.delete(wishRequestDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(wishRepository, times(1)).findByWisherIdAndWishedIdAndIsDeletedFalse(1L,2L);
    }

    @Test
    @DisplayName("찜 목록 조회")
    void testGetWishedList() {
        long memberId = 1L;
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        Member wisher = new Member(1L);
        Member wished = new Member(2L);
        Wish wish = new Wish(wisher, wished);
        List<Wish> wishes = List.of(wish);

        when(wishRepository.findAllByWisherIdAndIsDeletedFalse(memberId, pageable)).thenReturn(wishes);

        List<WishedListResponse> wishedList = wishService.getWishedList(page, size, memberId);

        assertEquals(1, wishedList.size());
        verify(wishRepository, times(1)).findAllByWisherIdAndIsDeletedFalse(memberId, pageable);
    }

    @Test
    @DisplayName("특정인 찜 목록 조회")
    void testGetWisherList() {
        long memberId = 1L;
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        Member wisher = new Member(1L);
        Member wished = new Member(2L);
        Wish wish = new Wish(wisher,wished);
        List<Wish> wishes = List.of(wish);

        when(wishRepository.findAllByWishedIdAndIsDeletedFalse(memberId, pageable)).thenReturn(wishes);

        List<WisherListResponse> wisherList = wishService.getWisherList(page, size, memberId);

        assertEquals(1, wisherList.size());
        verify(wishRepository, times(1)).findAllByWishedIdAndIsDeletedFalse(memberId, pageable);
    }
}