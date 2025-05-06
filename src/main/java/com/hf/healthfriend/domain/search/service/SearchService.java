package com.hf.healthfriend.domain.search.service;

import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.member.dto.response.MemberSearchResponse;
import com.hf.healthfriend.domain.member.service.MemberService;
import com.hf.healthfriend.domain.post.dto.response.PostListObject;
import com.hf.healthfriend.domain.post.dto.response.PostSearchResponse;
import com.hf.healthfriend.domain.post.repository.PostRepository;
import com.hf.healthfriend.domain.post.service.PostService;
import com.hf.healthfriend.domain.search.dto.SearchResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final MemberService memberService;
    private final PostService postService;
    private final PostRepository postRepository;
    private final RedissonClient redissonClient;

    public SearchResponse search(int page, int size,
                                 String cd1, String cd2, String cd3,
                                 List<String> fitnessLevels,List<String> companionStyles,List<String> fitnessEagernesses,
                                 List<String> fitnessKinds,List<String> fitnessObjectives,
                                 String memberSortType,String keyword, Long memberId) {

        PostSearchResponse postSearchResponse =  postService.getList(page, size, null, null, keyword);
        MemberSearchResponse memberSearchResponse = memberService.searchMembers(cd1,cd2,cd3,fitnessLevels,companionStyles,fitnessEagernesses,fitnessKinds,fitnessObjectives,memberSortType,keyword,page,size);
        Long postListSize = postRepository.getSearchedPostListSize(null,null,keyword);
        Long memberListSize = memberService.getSearchedMembersSize(cd1,cd2,cd3,fitnessLevels,companionStyles,fitnessEagernesses,fitnessKinds,fitnessObjectives,memberSortType,keyword,page,size);
        List<String> recentSearchList = getRecentSearchAndSave(memberId,keyword);

        return SearchResponse.builder()
                .postList(postSearchResponse.postList())
                .postListSize(postListSize)
                .profileList(memberSearchResponse.memberList())
                .profileListSize(memberListSize)
                .recentSearchList(recentSearchList)
                .build();
    }

    public void saveRecentSearchKeyword(Long memberId, String keyword) {
        RList<String> recentSearchList = redissonClient.getList("recent_search_keywords:"+memberId);
        recentSearchList.remove(keyword); //중복 저장 방지
        recentSearchList.add(0,keyword);
        if (recentSearchList.size() > 6) {
            recentSearchList.remove(6);
        }
        recentSearchList.expire(Duration.ofDays(3));
    }

    public List<String> getRecentSearchKeywords(Long memberId) {
        RList<String> recentSearchKeywords = redissonClient.getList("recent_search_keywords:"+memberId);
        if (recentSearchKeywords == null) {
            return new ArrayList<>();  // null 이면 빈 리스트 반환
        }
        return new ArrayList<>(recentSearchKeywords.readAll());
    }

    public void deleteRecentSearch(Long memberId) {
        RList<String> myList = redissonClient.getList("recent_search_keywords:"+memberId);
        myList.clear();
    }

    public List<String> getRecentSearchAndSave(Long memberId, String keyword) {
        List<String> recentSearchList = new ArrayList<>();
        if(memberId!=null){
            recentSearchList = getRecentSearchKeywords(memberId);
            if (keyword != null) {
                saveRecentSearchKeyword(memberId, keyword);
            }
        }
        return recentSearchList;
    }
}
