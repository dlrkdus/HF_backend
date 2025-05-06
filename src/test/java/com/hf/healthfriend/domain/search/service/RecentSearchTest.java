package com.hf.healthfriend.domain.search.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.hf.healthfriend.domain.post.repository.PostRepository;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
public class RecentSearchTest {
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private PostRepository postRepository;

    @Mock
    private RList<String> mockRList;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(null, null, postRepository,redissonClient);
    }

    @Test
    @DisplayName("멤버 아이디로 최근 검색어 저장되는지 확인")
    void testSaveRecentSearchKeyword() {
        // Given
        Long memberId = 1L;
        String keyword = "운동";
        String redisKey = "recent_search_keywords:" + memberId;

        Mockito.when(redissonClient.<String>getList(redisKey)).thenReturn(mockRList);

        // When
        searchService.saveRecentSearchKeyword(memberId, keyword);

        // Then
        Mockito.verify(mockRList).remove(keyword); // 중복 방지 확인
        Mockito.verify(mockRList).add(0, keyword); // 검색어 추가 확인
        Mockito.verify(mockRList).expire(Duration.ofDays(3)); // 만료 시간 설정 확인
    }

    @Test
    @DisplayName("멤버 아이디로 최근 검색 목록 받아오는지 확인")
    void testGetRecentSearchKeywords() {
        // Given
        Long memberId = 1L;
        String redisKey = "recent_search_keywords:" + memberId;
        List<String> expectedList = List.of("운동", "다이어트");

        Mockito.when(redissonClient.<String>getList(redisKey)).thenReturn(mockRList);
        Mockito.when(mockRList.readAll()).thenReturn(expectedList);

        System.out.println(redissonClient.<String>getList(redisKey).readAll());


        // When
        List<String> recentSearches = searchService.getRecentSearchKeywords(memberId);

        // Then
        assertThat(recentSearches).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("멤버 아이디로 최근 검색 목록 삭제되는지 확인")
    void testDeleteRecentSearch() {
        // Given
        Long memberId = 1L;
        String redisKey = "recent_search_keywords:" + memberId;

        Mockito.when(redissonClient.<String>getList(redisKey)).thenReturn(mockRList);

        // When
        searchService.deleteRecentSearch(memberId);

        // Then
        Mockito.verify(mockRList).clear(); // 목록이 비워졌는지 확인
    }
}
