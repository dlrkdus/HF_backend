package com.hf.healthfriend.domain.wish.service;

import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import com.hf.healthfriend.domain.wish.dto.request.WishRequestDto;
import com.hf.healthfriend.domain.wish.dto.response.WishedListResponse;
import com.hf.healthfriend.domain.wish.dto.response.WisherListResponse;
import com.hf.healthfriend.domain.wish.entity.Wish;
import com.hf.healthfriend.domain.wish.exception.WishErrorCode;
import com.hf.healthfriend.domain.wish.exception.WishException;
import com.hf.healthfriend.domain.wish.repository.WishRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;

    public Long save(WishRequestDto wishRequestDto) {

        Long wisherId = wishRequestDto.getWisherId();
        Long wishedId = wishRequestDto.getWishedId();

        if (wishRepository.existsByWishedIdAndWisherIdAndIsDeletedFalse(wishedId,wisherId))
            throw new WishException(WishErrorCode.DUPLICATE_WISH,HttpStatus.BAD_REQUEST,
                    "wishedId: "+wishedId+", wisherId: "+wisherId);

        boolean wisherExist = memberRepository.existsById(wisherId);
        boolean wishedExist = memberRepository.existsById(wishedId);
        if( wisherExist && wishedExist){
            Wish wish = new Wish(
                    new Member(wisherId),
                    new Member(wishedId));
            wishRepository.save(wish);
            memberRepository.findById(wishedId).get().incrementWishedCount();
            return wish.getWishId();
        }else if(wisherExist){
            throw new WishException(WishErrorCode.MEMBER_NOT_FOUND, HttpStatus.BAD_REQUEST,
                    "wishedId not found: "+wishedId);
        }else if(wishedExist){
            throw new WishException(WishErrorCode.MEMBER_NOT_FOUND, HttpStatus.BAD_REQUEST,
                    "wisherId not found: "+wisherId);
        }else{
            throw new WishException(WishErrorCode.MEMBER_NOT_FOUND, HttpStatus.BAD_REQUEST,
                    "wisherId not found: "+wisherId+" wishedId not found: "+wishedId);
        }
    }

    public void delete(WishRequestDto wishRequestDto){
        long wisherId = wishRequestDto.getWisherId();
        long wishedId = wishRequestDto.getWishedId();

        Wish wish = wishRepository.findByWisherIdAndWishedIdAndIsDeletedFalse(wisherId,wishedId)
                .orElseThrow(() -> new WishException(WishErrorCode.WISH_NOT_FOUND, HttpStatus.BAD_REQUEST,
                        "wisherId: "+wisherId+", wishedId: "+wishedId));
        wish.delete();
        Member member = wish.getWished();
        member.decrementWishedCount();
    }

    public List<WishedListResponse> getWishedList(int page, int size, Long memberId){
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Wish> wishedList = wishRepository.findAllByWisherIdAndIsDeletedFalse(memberId, pageable);
        return getWishedResponses(wishedList);
    }

    public List<WisherListResponse> getWisherList(int page, int size, Long memberId){
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Wish> wisherList = wishRepository.findAllByWishedIdAndIsDeletedFalse(memberId, pageable);
        return getWisherResponses(wisherList);
    }

    @NotNull
    private List<WishedListResponse> getWishedResponses(List<Wish> wishedList) {
        List<WishedListResponse> wishedListResponseList = new ArrayList<>();
        for(Wish wish : wishedList){
            WishedListResponse wishedListResponse = WishedListResponse.builder()
                    .wishedId(wish.getWished().getId())
                    .imageUrl(wish.getWished().getProfileImageUrl())
                    .wishedNickname(wish.getWished().getNickname())
                    .build();
            wishedListResponseList.add(wishedListResponse);
        }
        return wishedListResponseList;
    }

    @NotNull
    private List<WisherListResponse> getWisherResponses(List<Wish> wishedList) {
        List<WisherListResponse> wisherListResponseList = new ArrayList<>();
        for(Wish wish : wishedList){
            WisherListResponse wisherListResponse = WisherListResponse.builder()
                    .wisherId(wish.getWisher().getId())
                    .imageUrl(wish.getWisher().getProfileImageUrl())
                    .wisherNickname(wish.getWisher().getNickname())
                    .build();
            wisherListResponseList.add(wisherListResponse);
        }
        return wisherListResponseList;
    }


    @Description("찜 눌렀는지 확인 기능")
    public Boolean isWished(Long wishedId, Long wisherId) {
        return wishRepository.existsByWishedIdAndWisherIdAndIsDeletedFalse(wishedId,wisherId);
    }
}
