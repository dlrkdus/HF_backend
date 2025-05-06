package com.hf.healthfriend.domain.search.controller;


import com.hf.healthfriend.domain.search.dto.SearchResponse;
import com.hf.healthfriend.domain.search.service.SearchService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hf")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "통합 검색 목록 조회",
            responses = {
            @ApiResponse(responseCode = "200", description = "통합 검색 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "통합 검색 목록 조회 실패")
    })
    @GetMapping("/search")
    @CrossOrigin
    public ResponseEntity<ApiBasicResponse<SearchResponse>> getSearchList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam int size,
            @RequestParam(required = false) String cd1,
            @RequestParam(required = false) String cd2,
            @RequestParam(required = false) String cd3,
            @RequestParam(required = false) List<String> fitnessLevels,
            @RequestParam(required = false) List<String> companionStyles,
            @RequestParam(required = false) List<String> fitnessEagernesses,
            @RequestParam(required = false) List<String> fitnessKinds,
            @RequestParam(required = false) List<String> fitnessObjectives,
            @RequestParam(required = false) String memberSortType,
            @RequestParam @Nullable String keyword,
            @Nullable Long memberId) {

        return ResponseEntity.ok(ApiBasicResponse.of(searchService.search(page, size, cd1,cd2,cd3,fitnessLevels,companionStyles,fitnessEagernesses,fitnessKinds,fitnessObjectives,memberSortType, keyword, memberId),
                HttpStatus.OK));
    }

    @Operation(summary = "최근 검색 목록 삭제", responses = {
            @ApiResponse(responseCode = "200", description = "최근 검색 목록 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "최근 검색 목록 삭제 실패")
    })
    @DeleteMapping("/search/recentDelete")
    public ResponseEntity<ApiBasicResponse<Void>> deleteRecentSearchList(Long memberId) {
        searchService.deleteRecentSearch(memberId);
        return ResponseEntity.ok(ApiBasicResponse.of(HttpStatus.OK));
    }


}
