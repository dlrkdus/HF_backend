package com.hf.healthfriend.domain.search.service;

import com.hf.healthfriend.domain.member.service.MemberService;
import com.hf.healthfriend.domain.post.service.PostService;
import com.hf.healthfriend.domain.search.constant.SearchCategory;
import com.hf.healthfriend.domain.search.dto.SearchResponse;
import com.hf.healthfriend.testutil.RedisTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {SearchService.class, RedisTestConfig.class})
@ActiveProfiles("test")
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private PostService postService;

    @Autowired
    @Qualifier("testRedissonClient")
    private RedissonClient redissonClient;

    private final Long memberId = 1L;

    @BeforeEach
    void setUp() {
        // Test setup 작업 필요시 이곳에 추가 가능
        RList<String> list = redissonClient.getList("recent_search_keywords:"+memberId);
        list.clear();
        System.out.println("Redis 데이터 초기화 완료");
    }

    @Test
    void testSaveRecentSearchKeyword() {
        // Given
        String keyword = "testKeyword";

        // When
        searchService.saveRecentSearchKeyword(memberId, keyword);

        // Then
        List<String> recentSearches = searchService.getRecentSearchKeywords(memberId);
        assertNotNull(recentSearches);
        assertTrue(recentSearches.contains(keyword));
    }

    @Test
    void testSaveRecentSearchKeywordWithOverflow() {
        // Given
        String[] keywords = {"keyword1", "keyword2", "keyword3", "keyword4", "keyword5", "keyword6", "keyword7"};

        // When
        for (String keyword : keywords) {
            searchService.saveRecentSearchKeyword(memberId, keyword);
        }

        // Then
        List<String> recentSearches = searchService.getRecentSearchKeywords(memberId);
        assertNotNull(recentSearches);
        assertEquals(6, recentSearches.size());
        assertEquals("keyword7", recentSearches.get(0));  // 최신 검색어가 맨 앞
        assertFalse(recentSearches.contains("keyword1"));  // 첫 번째 입력된 검색어는 삭제되어야 함
    }

    @Test
    void testGetRecentSearchKeywords() {
        // Given
        String keyword = "sampleKeyword";
        searchService.saveRecentSearchKeyword(memberId, keyword);

        // When
        List<String> recentSearches = searchService.getRecentSearchKeywords(memberId);
        System.out.println(recentSearches);

        // Then
        assertNotNull(recentSearches);
        assertEquals(1, recentSearches.size());
        assertEquals(keyword, recentSearches.get(0));
    }

//    @Test
//    void testSearchWithRecentKeywords() {
//        // Given
//        String keyword = "sampleKeyword";
//
//        // When
//        SearchResponse response = searchService.search(1, 10, SearchCategory.POST, keyword, memberId);
//
//        // Then
//        assertNotNull(response);
//        List<String> recentSearches = searchService.getRecentSearchKeywords(memberId);
//        assertTrue(recentSearches.contains(keyword));
//    }
}