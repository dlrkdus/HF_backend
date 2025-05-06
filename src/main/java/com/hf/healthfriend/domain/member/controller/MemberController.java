package com.hf.healthfriend.domain.member.controller;

import com.hf.healthfriend.auth.constant.CookieConstants;
import com.hf.healthfriend.domain.member.controller.schema.ProfileResponseSchema;
import com.hf.healthfriend.domain.member.dto.MemberDto;
import com.hf.healthfriend.domain.member.dto.request.MemberCreationRequestDto;
import com.hf.healthfriend.domain.member.dto.request.MemberUpdateRequestDto;
import com.hf.healthfriend.domain.member.dto.response.MemberCreationResponseDto;
import com.hf.healthfriend.domain.member.dto.response.MemberListResponse;
import com.hf.healthfriend.domain.member.dto.response.MemberSearchResponse;
import com.hf.healthfriend.domain.member.dto.response.MemberUpdateResponseDto;
import com.hf.healthfriend.domain.member.dto.response.ProfileResponseDto;
import com.hf.healthfriend.domain.member.service.MemberService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import com.hf.healthfriend.global.spec.ApiErrorResponse;
import com.hf.healthfriend.global.spec.schema.BooleanTypeSchema;
import com.hf.healthfriend.global.spec.schema.MemberCreationResponseSchema;
import com.hf.healthfriend.global.spec.schema.MemberResponseSchema;
import com.hf.healthfriend.global.util.HttpCookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hf/members")
public class MemberController {

    private final MemberService memberService;
    private final HttpCookieUtils httpCookieUtils;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "회원 생성",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MemberCreationRequestDto.class),
                            examples = @ExampleObject("""
                                    {
                                        "id": "new@gmail.com",
                                        "name": "김샘플",
                                        "nickname": "새로운인간",
                                        "profileImageFileExtension": "jpg",
                                        "birthDate": "1997-09-16",
                                        "gender": "MALE",
                                        "cd1": "01",
                                        "cd2": "110",
                                        "cd3": "112",
                                        "introduction": "안녕하세요",
                                        "fitnessLevel": "BEGINNER",
                                        "companionStyle": "GROUP",
                                        "fitnessEagerness": "EAGER",
                                        "fitnessObjective": "BULK_UP",
                                        "fitnessKind": "FUNCTIONAL",
                                        "specs": [
                                            {
                                                "startDate": "2017-11-23",
                                                "endDate": "2018-11-12",
                                                "isCurrent": false,
                                                "title": "경력1",
                                                "description": "종료된 경력"
                                            },
                                            {
                                                "startDate": "2024-08-24",
                                                "endDate": null,
                                                "isCurrent": true,
                                                "title": "경력2",
                                                "description": "현재 진행 중인 경력"
                                            },
                                            {
                                                "startDate": "2020-11-23",
                                                "endDate": null,
                                                "isCurrent": false,
                                                "title": "수상1",
                                                "description": ""
                                            }
                                        ]
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    description = "회원 생성 성공",
                    responseCode = "201",
                    headers = {
                            @Header(name = "Location", description = "생성된 회원의 리소스 경로"),
                            @Header(name = HttpHeaders.SET_COOKIE, description = "is_new_member 쿠키를 false로 설정")
                    },
                    content = @Content(
                            schema = @Schema(implementation = MemberCreationResponseSchema.class),
                            examples = @ExampleObject("""
                                    {
                                        "statusCode": 201,
                                        "statusCodeSeries": 2,
                                        "message": null,
                                        "content": {
                                            "memberId": 1000,
                                            "loginId": "new@gmail.com",
                                            "email": "new@gmail.com",
                                            "role": "ROLE_MEMBER",
                                            "creationTime": "2024-10-29T15:08:06.022Z",
                                            "nickname": "새로운인간",
                                            "birthDate": "1997-09-16",
                                            "gender": "MALE",
                                            "introduction": "안녕하세요",
                                            "profileImageUploadUrl": "http://hf-aws-bucket.s3.aws.amazon.com?accessKey=acc#!$",
                                            "fitnessLevel": "ADVANCED",
                                            "companionStyle": "SMALL",
                                            "fitnessEagerness": "EAGER",
                                            "fitnessObjective": "BULK_UP",
                                            "fitnessKind": "HIGH_STRESS",
                                            "specIds": [
                                                10000,
                                                10001,
                                                10002
                                            ]
                                        }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    description = "회원 중복 등록 에러 / Error Code: 201",
                    responseCode = "400",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "statusCode": 40003,
                                        "statusCodeSeries": 4,
                                        "errorCode": "MB004",
                                        "errorName": "MEMBER_ALREADY_EXISTS",
                                        "message": "이미 존재하는 회원입니다"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    description = "현재 로그인한 회원이 자신과 다른 이름의 회원을 생성하려고 할 때 / Error Code: 101",
                    responseCode = "403",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "statusCode": 403,
                                        "statusCodeSeries": 4,
                                        "errorCode": 101,
                                        "errorName": "UNAUTHORIZED",
                                        "message": "허용되지 않은 접근입니다"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiBasicResponse<MemberCreationResponseDto>> createMember(
            @RequestBody @Valid MemberCreationRequestDto requestBody) throws URISyntaxException {
        log.info("Request Body:\n{}", requestBody);

        MemberCreationResponseDto result = this.memberService.createMember(requestBody);
        ResponseCookie existsMember =
                this.httpCookieUtils.buildJavaScriptAccessibleResponseCookie(CookieConstants.COOKIE_NAME_IS_NEW_MEMBER.getString(),
                        String.valueOf(false));
        return ResponseEntity.created(new URI("/hr/members/" + result.getMemberId()))
                .header(HttpHeaders.SET_COOKIE, existsMember.toString())
                .body(ApiBasicResponse.of(result, HttpStatus.CREATED));
    }

    @GetMapping(value = "/{memberId}", produces = "application/json")
    @Operation(summary = "회원 찾기")
    @ApiResponses({
            @ApiResponse(
                    description = "회원 찾기 성공",
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = MemberResponseSchema.class))
            ),
            @ApiResponse(
                    description = "없는 회원 검색 / Error Code: 200",
                    responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
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
    })
    public ResponseEntity<ApiBasicResponse<MemberDto>> findMember(@PathVariable("memberId") Long memberId) {
        log.info("Find member of id={}", memberId);
        return ResponseEntity.ok(ApiBasicResponse.of(this.memberService.findMember(memberId), HttpStatus.OK));
    }

    @PatchMapping(value = "/{memberId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
    @Operation(
            summary = "회원 업데이트",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MemberUpdateRequestDto.class),
                            examples = @ExampleObject("""
                                    {
                                        "nickname": "new-nickname",
                                        "cd1": "10",
                                        "cd2": "111",
                                        "cd3": "123",
                                        "introduction": "수정된 소개",
                                        "fitnessLevel": "EAGER",
                                        "companionStyle": "GROUP",
                                        "fitnessEagerness": "EAGER",
                                        "fitnessObjective": "BULK_UP",
                                        "fitnessKind": "FUNCTIONAL",
                                        "profileImageFileExtension": null,
                                        "specUpdate": [
                                            {
                                                "specUpdateType": "INSERT",
                                                "spec": {
                                                    "startDate": "2020-10-21",
                                                    "endDate": "2022-10-21",
                                                    "title": "새 스펙",
                                                    "description": "스펙",
                                                    "isCurrent": false
                                                }
                                            },
                                            {
                                                "specUpdateType": "UPDATE",
                                                "specId": 100,
                                                "spec": {
                                                    "startDate": "2020-10-21",
                                                    "endDate": "2022-10-21",
                                                    "title": "스포애니 전문 트레이너",
                                                    "description": "원래는 에이블짐 트레이너였음",
                                                    "isCurrent": false
                                                }
                                            },
                                            {
                                                "specUpdateType": "DELETE",
                                                "specId": 101
                                            }
                                        ]
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    description = "회원 업데이트 성공",
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = MemberUpdateResponseDto.class))
            ),
            @ApiResponse(
                    description = "없는 회원 검색 / Error Code: 200",
                    responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
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
    })
    public ResponseEntity<ApiBasicResponse<MemberUpdateResponseDto>> updateMember(@PathVariable("memberId") Long memberId,
                                                                                  @RequestBody @Valid MemberUpdateRequestDto dto) {
        MemberUpdateResponseDto resultDto = this.memberService.updateMember(memberId, dto);
        return ResponseEntity.ok(ApiBasicResponse.of(resultDto, HttpStatus.OK));
    }

    @GetMapping("/{memberId}/profile")
    @Operation(
            summary = "매칭을 위한 회원 프로필 조회",
            parameters = @Parameter(
                    name = "조회하고자 하는 회원의 프로필",
                    required = true,
                    examples = @ExampleObject("20000"),
                    in = ParameterIn.PATH
            ),
            responses = {
                    @ApiResponse(
                            description = "경력은 startDate 기준 내림차순, 리뷰는 reviewDetailCount 기준 내림차순",
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = ProfileResponseSchema.class),
                                    examples = @ExampleObject("""
                                            {
                                                "statusCode": 200,
                                                "statusCodeSeries": 2,
                                                "message": "회원 프로필 받아오기 성공",
                                                "content": {
                                                    "memberId": "20000",
                                                    "introduction": "안녕하세요!",
                                                    "specs": [
                                                        {
                                                            "specId": 1002,
                                                            "startDate": "2022-03",
                                                            "endDate": null,
                                                            "isCurrent": true,
                                                            "title": "스포애니 전문 트레이너",
                                                            "description": "스포애니에서 현재까지 근무 중"
                                                        },
                                                        {
                                                            "specId": 1001,
                                                            "startDate": "2021-04",
                                                            "endDate": null,
                                                            "isCurrent": false,
                                                            "title": "보디빌딩 대회 국방부장관상",
                                                            "description": "수상 이력입니다. (endDate null, isCurrent false일 경우 수상 이력)"
                                                        },
                                                        {
                                                            "specId": 1000,
                                                            "startDate": "2019-03",
                                                            "endDate": "2020-12",
                                                            "isCurrent": false,
                                                            "title": "15 전투비행단 체력단련실 지박령",
                                                            "description": ""
                                                        }
                                                    ],
                                                    "reviews": {
                                                        "good": [
                                                            {
                                                                "reviewDetailId": 1,
                                                                "reviewDetailCount": 12
                                                            },
                                                            {
                                                                "reviewDetailId": 2,
                                                                "reviewDetailCount": 9
                                                            }
                                                        ],
                                                        "notGood": [
                                                            {
                                                                "reviewDetailId": 3,
                                                                "reviewDetailCount": 8
                                                            },
                                                            {
                                                                "reviewDetailId": 1,
                                                                "reviewDetailCount": 5
                                                            }
                                                        ]
                                                    },
                                                    "averageReviewScore": 3.5,
                                                    "matchingCount": 34,
                                                    "reviewCount": 34,
                                                    "wishedCount": 25
                                                }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            description = "찾고자 하는 회원이 존재하지 않음",
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
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
    public ResponseEntity<ApiBasicResponse<ProfileResponseDto>> getProfileOfMember(@PathVariable("memberId") Long memberId) {
        ProfileResponseDto result = this.memberService.getProfileOfMember(memberId);
        return ResponseEntity.ok(
                ApiBasicResponse.of(
                        result,
                        HttpStatus.OK,
                        "회원 프로필 받아오기 성공"
                )
        );
    }

    @Operation(summary = "프로필 검색 목록 조회", responses = {
            @ApiResponse(responseCode = "200", description = "프로필 검색 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "프로필 검색 목록 조회 실패")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiBasicResponse<MemberSearchResponse>> getSearchedMembers(@RequestParam(value = "page", defaultValue = "1") int page,
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
                                                                                     @RequestParam @Nullable String keyword) {
        return ResponseEntity.ok(ApiBasicResponse.of(this.memberService.searchMembers(cd1,cd2,cd3,fitnessLevels,companionStyles,fitnessEagernesses,fitnessKinds,fitnessObjectives,memberSortType,keyword,page,size), HttpStatus.OK));
    }

    @GetMapping("/is-duplicate-nickname")
    @Operation(
            summary = "닉네임 중복 체크",
            description = "회원 닉네임 중복 확인. 중복된 닉네임일 경우 true, 중복되지 않은 닉네임일 경우 false가 \"content\"에 " +
                    "담겨서 반환됨",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = BooleanTypeSchema.class),
                            examples = {
                                    @ExampleObject(
                                            summary = "닉네임 중복일 경우 true 반환",
                                            value = """
                                                    {
                                                        "statusCode": 200,
                                                        "statusCodeSeries": 2,
                                                        "content": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            summary = "닉네임이 중복되지 않을 경우 false 반환",
                                            value = """
                                                    {
                                                        "statusCode": 200,
                                                        "statusCodeSeries": 2,
                                                        "content": false
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<ApiBasicResponse<Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(ApiBasicResponse.of(this.memberService.checkDuplicateOfNickname(nickname), HttpStatus.OK));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "회원 탈퇴", responses = {
            @ApiResponse(description = "회원 탈퇴 성공", responseCode = "200"),
            @ApiResponse(description = "해당 회원이 없음", responseCode = "404")
    })
    public ResponseEntity<ApiBasicResponse<Void>> deleteMember(@PathVariable("memberId") Long memberId) {
        this.memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiBasicResponse.of(HttpStatus.OK, "회원 탈퇴 성공"));
    }
}
