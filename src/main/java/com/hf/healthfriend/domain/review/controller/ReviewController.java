package com.hf.healthfriend.domain.review.controller;

import com.hf.healthfriend.domain.review.controller.schema.RevieweeResponseSchema;
import com.hf.healthfriend.domain.review.dto.request.ReviewCreationRequestDto;
import com.hf.healthfriend.domain.review.dto.response.RevieweeResponseDto;
import com.hf.healthfriend.domain.review.service.ReviewService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import com.hf.healthfriend.global.spec.BasicErrorResponse;
import com.hf.healthfriend.global.spec.schema.LongTypeSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hf")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @Operation(
            summary = "리뷰 생성",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = ReviewCreationRequestDto.class),
                            examples = @ExampleObject("""
                                    {
                                        "matchingId": 1,
                                        "reviewerId": 1,
                                        "revieweeId": 2,
                                        "score": 4,
                                        "evaluations": [
                                            {
                                                "evaluationType": "GOOD",
                                                "evaluationDetailId": 2
                                            },
                                            {
                                                "evaluationType": "GOOD",
                                                "evaluationDetailId": 3
                                            }
                                        ]
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "리뷰 생성 성공",
                            responseCode = "201",
                            content = @Content(
                                    schema = @Schema(implementation = LongTypeSchema.class),
                                    examples = @ExampleObject(
                                            name = "리뷰 생성 성공 시 Response Body",
                                            value = """
                                                    {
                                                      "statusCode": 201,
                                                      "statusCodeSeries": 2,
                                                      "message": "리뷰 생성 성공",
                                                      "content": 1000
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            description = "없는 회원 혹은 없는 매칭에 대해 리뷰를 남길 경우 실패",
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = BasicErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "매칭이 존재하지 않음",
                                                    value = """
                                                            {
                                                                "statusCode": 40401,
                                                                "statusCodeSeries": 4,
                                                                "errorCode": "MAT002",
                                                                "errorName": "MATCHING_NOT_FOUND".
                                                                "detail": {
                                                                    "matchingId": 10000
                                                                }
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "회원이 존재하지 않음",
                                                    value = """
                                                            {
                                                                "statusCode": 40401,
                                                                "statusCodeSeries": 4,
                                                                "errorCode": "MB001",
                                                                "errorName": "MEMBER_NOT_FOUND",
                                                                "detail": {
                                                                    "reviewerId": 10000
                                                                }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<ApiBasicResponse<Long>> addReview(@RequestBody @Valid ReviewCreationRequestDto dto) {
        Long generatedReviewId = this.reviewService.addReview(dto);
        return new ResponseEntity<>(
                ApiBasicResponse.of(generatedReviewId, HttpStatus.CREATED, "리뷰 생성 성공"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/members/{memberId}/written-reviews")
    @Operation(
            summary = "특정 회원에 대한 리뷰 조회",
            parameters = @Parameter(
                    name = "회원 ID",
                    description = "리뷰를 조회하려는 대상 회원의 ID",
                    examples = @ExampleObject("10000")
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 리뷰 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = RevieweeResponseSchema.class),
                                    examples = @ExampleObject("""
                                            {
                                                "statusCode": 200,
                                                "statusCodeSeries": 2,
                                                "message": "회원 리뷰 조회 성공",
                                                "content": {
                                                    "memberId": 10000,
                                                    "averageScore": 3.5,
                                                    "good": {
                                                        "totalCountPerEvaluationType": 71,
                                                        "reviewDetailsPerEvaluationType": [
                                                            {
                                                                "reviewDetailId": 3,
                                                                "reviewDetailCount": 36
                                                            },
                                                            {
                                                                "reviewDetailId": 1,
                                                                "reviewDetailCount": 23
                                                            },
                                                            {
                                                                "reviewDetailId": 2,
                                                                "reviewDetailCount": 12
                                                            }
                                                        ]
                                                    },
                                                    "notGood": {
                                                        "totalCountPerEvaluationType": 15,
                                                        "reviewDetailsPerEvaluationType": [
                                                            {
                                                                "reviewDetailId": 2,
                                                                "reviewDetailCount": 10
                                                            },
                                                            {
                                                                "reviewDetailId": 1,
                                                                "reviewDetailCount": 5
                                                            }
                                                        ]
                                                    },
                                                    "reviewScore": 3.5
                                                }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            description = "없는 회원에 대한 리뷰를 조회할 경우",
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = BasicErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "회원이 존재하지 않음",
                                                    value = """
                                                            {
                                                                "statusCode": 40401,
                                                                "statusCodeSeries": 4,
                                                                "errorCode": "MB001",
                                                                "errorName": "MEMBER_NOT_FOUND",
                                                                "detail": {
                                                                    "reviewerId": 10000
                                                                }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<ApiBasicResponse<RevieweeResponseDto>> getReviewsWrittenForMember(@PathVariable("memberId") Long memberId) {
        RevieweeResponseDto revieweeInfo = this.reviewService.getRevieweeInfo(memberId);
        return ResponseEntity.ok(
                ApiBasicResponse.of(revieweeInfo, HttpStatus.OK, "회원 리뷰 조회 성공")
        );
    }
}
