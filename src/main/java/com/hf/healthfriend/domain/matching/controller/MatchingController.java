package com.hf.healthfriend.domain.matching.controller;

import com.hf.healthfriend.domain.matching.constant.MatchingFetchType;
import com.hf.healthfriend.domain.matching.constant.MatchingStatusCondition;
import com.hf.healthfriend.domain.matching.controller.schema.PaginatedMatchingListResponseSchema;
import com.hf.healthfriend.domain.matching.dto.response.MatchingListResponseDto;
import com.hf.healthfriend.domain.matching.dto.response.PageResponseDto;
import com.hf.healthfriend.domain.matching.service.MatchingService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import com.hf.healthfriend.global.spec.BasicErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hf")
@RequiredArgsConstructor
public class MatchingController {
    private static final String MATCHING_LIST_DEFAULT_PAGE_SIZE = "4";

    private final MatchingService matchingService;

    @Validated
    @GetMapping("/members/{memberId}/matchings")
    @Operation(
            summary = "마이페이지의 매칭 목록 조회",
            description = "특정 사용자의 매칭 목록, 이력을 조회하는 API 엔드포인트",
            parameters = {
                    @Parameter(name = "memberId", required = true, in = ParameterIn.PATH, description = "조회하고자 하는 회원의 ID"),
                    @Parameter(
                            name = "matchingFetchType",
                            description = "내가 매칭 신청을 건 매칭을 가져올 것인지, 다른 사람이 매칭 신청을 건 매칭을 가져올 것인지",
                            examples = {
                                    @ExampleObject(summary = "조건 상관 없음", value = "ALL"),
                                    @ExampleObject(summary = "내가 신청한 매칭 가져오기", value = "WHAT_I_REQUESTED"),
                                    @ExampleObject(summary = "다른 사람한테 신청받은 매칭 가져오기", value = "WHAT_I_RECEIVED")
                            }
                    ),
                    @Parameter(
                            name = "matchingStatusCondition",
                            description = "매칭 상태에 따른 검색 조건",
                            examples = {
                                    @ExampleObject(summary = "조건 없음", value = "ALL"),
                                    @ExampleObject(summary = "진행 중인 매칭 가져오기", value = "IN_PROGRESS"),
                                    @ExampleObject(summary = "종료된 매칭 가져오기", value = "FINISHED"),
                                    @ExampleObject(summary = "중단된 매칭 가져오기", value = "HALTED")
                            }
                    ),
                    @Parameter(name = "page", description = "페이지. 1부터 시작. 1보다 작아서는 안 됨"),
                    @Parameter(name = "page", description = "한 페이지의 크기. 1보다 작아서는 안 됨")
            },
            responses = {
                    @ApiResponse(
                            description = "조회 성공",
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = PaginatedMatchingListResponseSchema.class),
                                    examples = @ExampleObject("""
                                            {
                                                "statusCode": 200,
                                                "statusCodeSeries": 2,
                                                "content": {
                                                    "page": 1,
                                                    "pageSize": 4,
                                                    "totalElementCount": 320,
                                                    "totalPageCount": 80,
                                                    "content": [
                                                        {
                                                            "matchingId": 100,
                                                            "meetingPlace": "스포애니",
                                                            "meetingPlaceAddress": "서울시 영등포구 당산역",
                                                            "matchingStatus": "ACCEPTED",
                                                            "meetingTime": "2024-11-22T05:27:21.754Z",
                                                            "finishTime": null,
                                                            "opponentInfo": {
                                                                "memberId": 1000,
                                                                "nickname": "새싹",
                                                                "profileImageUrl": null,
                                                                "fitnessLevel": "BEGINNER",
                                                                "companionStyle": "SMALL",
                                                                "fitnessEagerness": "EAGER",
                                                                "fitnessKind": "HIGH_STRESS",
                                                                "fitnessObjective": "BULK_UP",
                                                                "cd1": "01",
                                                                "cd2": "110",
                                                                "cd3": "220",
                                                                "matchedCount": 35
                                                            }
                                                        },
                                                        {
                                                            "matchingId": 101,
                                                            "meetingPlace": "스포애니",
                                                            "meetingPlaceAddress": "서울시 강남구",
                                                            "matchingStatus": "REJECTED",
                                                            "meetingTime": "2024-11-18T05:27:21.754Z",
                                                            "finishTime": null,
                                                            "opponentInfo": {
                                                                "memberId": 1001,
                                                                "nickname": "김새싹",
                                                                "profileImageUrl": null,
                                                                "fitnessLevel": "BEGINNER",
                                                                "companionStyle": "SMALL",
                                                                "fitnessEagerness": "EAGER",
                                                                "fitnessKind": "HIGH_STRESS",
                                                                "fitnessObjective": "BULK_UP",
                                                                "cd1": "11",
                                                                "cd2": "110",
                                                                "cd3": "220",
                                                                "matchedCount": 8
                                                            }
                                                        },
                                                        {
                                                            "matchingId": 102,
                                                            "meetingPlace": "스포애니",
                                                            "meetingPlaceAddress": "서울시 양쳔구",
                                                            "matchingStatus": "PENDING",
                                                            "meetingTime": "2024-11-08T05:27:21.754Z",
                                                            "finishTime": null,
                                                            "opponentInfo": {
                                                                "memberId": 1002,
                                                                "nickname": "고수1",
                                                                "profileImageUrl": null,
                                                                "fitnessLevel": "ADVANCED",
                                                                "companionStyle": "SMALL",
                                                                "fitnessEagerness": "EAGER",
                                                                "fitnessKind": "HIGH_STRESS",
                                                                "fitnessObjective": "BULK_UP",
                                                                "cd1": "01",
                                                                "cd2": "110",
                                                                "cd3": "220",
                                                                "matchedCount": 26
                                                            }
                                                        },
                                                        {
                                                            "matchingId": 103,
                                                            "meetingPlace": "스포애니",
                                                            "meetingPlaceAddress": "서울시 영등포구 당산역",
                                                            "matchingStatus": "FINISHED",
                                                            "meetingTime": "2024-11-04T05:27:21.754Z",
                                                            "finishTime": "2024-11-05T05:27:21.754Z",
                                                            "opponentInfo": {
                                                                "memberId": 1000,
                                                                "nickname": "새싹",
                                                                "profileImageUrl": null,
                                                                "fitnessLevel": "BEGINNER",
                                                                "companionStyle": "SMALL",
                                                                "fitnessEagerness": "EAGER",
                                                                "fitnessKind": "HIGH_STRESS",
                                                                "fitnessObjective": "BULK_UP",
                                                                "cd1": "01",
                                                                "cd2": "110",
                                                                "cd3": "220",
                                                                "matchedCount": 35
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            description = "회원을 찾을 수 없음",
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = BasicErrorResponse.class),
                                    examples = @ExampleObject("""
                                            {
                                                "statusCode": 40401,
                                                "statusCodeSeries": 4,
                                                "errorCode": "MB001",
                                                "errorName": "MEMBER_NOT_FOUND"
                                            }
                                            """)
                            )
                    )
            }
    )
    public ResponseEntity<ApiBasicResponse<PageResponseDto<MatchingListResponseDto>>> getMatchingList(
            @PathVariable("memberId") Long memberId,
            @RequestParam(value = "matchingFetchType", defaultValue = "ALL") MatchingFetchType fetchType,
            @RequestParam(value = "matchingStatusCondition", defaultValue = "ALL") MatchingStatusCondition statusCondition,
            @RequestParam(value = "page", defaultValue = "1") @Min(value = 1) int page,
            @RequestParam(value = "pageSize", defaultValue = MATCHING_LIST_DEFAULT_PAGE_SIZE) @Min(value = 1) int pageSize) {
        PageResponseDto<MatchingListResponseDto> result = this.matchingService.searchMatchingListOfMember(memberId,
                fetchType, statusCondition, page, pageSize);
        return ResponseEntity.ok(
                ApiBasicResponse.of(result, HttpStatus.OK)
        );
    }
}
