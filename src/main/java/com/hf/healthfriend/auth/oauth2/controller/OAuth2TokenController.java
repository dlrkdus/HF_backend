package com.hf.healthfriend.auth.oauth2.controller;

import com.hf.healthfriend.auth.constant.CookieConstants;
import com.hf.healthfriend.auth.exception.InvalidRefreshTokenException;
import com.hf.healthfriend.auth.oauth2.dto.request.WrappingRefreshTokenWithCookieRequestDto;
import com.hf.healthfriend.auth.oauth2.dto.response.TokenRefreshResponseDto;
import com.hf.healthfriend.auth.oauth2.tokensupport.OAuth2TokenSupport;
import com.hf.healthfriend.domain.member.dto.MemberDto;
import com.hf.healthfriend.domain.member.service.MemberService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import com.hf.healthfriend.global.spec.ApiErrorResponse;
import com.hf.healthfriend.global.spec.schema.MemberResponseSchema;
import com.hf.healthfriend.global.spec.schema.TokenRefreshResponseSchema;
import com.hf.healthfriend.global.util.HttpCookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/oauth/token")
@RequiredArgsConstructor
public class OAuth2TokenController {
    private final List<OAuth2TokenSupport> tokenSupports;
    private final HttpCookieUtils httpCookieUtils;
    private final MemberService memberService;

    @Deprecated
    @PostMapping("/refresh-token/wrap-with-cookie")
    public ResponseEntity<ApiBasicResponse<Void>> wrapWithCookie(@RequestBody WrappingRefreshTokenWithCookieRequestDto dto) {
        var refreshToken = dto.getRefreshToken();
        var responseCookie =
                this.httpCookieUtils.buildHttpOnlyResponseCookie(CookieConstants.COOKIE_NAME_REFRESH_TOKEN.getString(), refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ApiBasicResponse.of(HttpStatus.OK, "Refresh Token에 쿠키 저장 완료"));
    }

    @Operation(
            summary = "Refresh Token을 통한 Access Token 재발급",
            description = "HTTP-only Cookie에 담겨 있는 Refresh Token을 통해 Access Token을 재발급받는다."
    )
    @ApiResponses({
            @ApiResponse(
                    description = "Access Token 재발급 성공",
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = TokenRefreshResponseSchema.class),
                            examples = {
                                    @ExampleObject("""
                                            {
                                                "statusCode": 200,
                                                "statusCodeSeries": 2,
                                                "message": null,
                                                "content": {
                                                    "accessToken": "fawoigh42ihsioahoidasasodifh..."
                                                }
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    description = "유효하지 않은 Refresh Token으로 인한 Access Token 재발급 실패",
                    responseCode = "401",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "statusCode": 401,
                                        "statusCodeSeries": 4,
                                        "errorCode": 103,
                                        "errorName": "INVALID_REFRESH_TOKEN",
                                        "message": "유효하지 않은 Refresh Token입니다"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/refresh")
    public ResponseEntity<ApiBasicResponse<TokenRefreshResponseDto>> refreshToken(
            @CookieValue("refresh_token") String refreshToken) {
        String regrantedAccessToken = null;
        for (OAuth2TokenSupport tokenSupport : tokenSupports) {
            try {
                regrantedAccessToken = tokenSupport.refreshToken(refreshToken);
            } catch (Exception e) { // TODO: 세밀한 예외 처리
                log.warn("{}가 처리할 수 없는 Refresh Token", tokenSupport.getClass());
            }
        }

        if (regrantedAccessToken == null) {
            throw new InvalidRefreshTokenException("유효하지 않은 Refresh Token");
        }

        return ResponseEntity.ok(ApiBasicResponse.of(new TokenRefreshResponseDto(regrantedAccessToken), HttpStatus.OK));
    }

    @GetMapping("/me")
    @Operation(
            summary = "현재 로그인한 회원의 정보 가져오기",
            description = "Access Token을 통해 현재 로그인한 회원 정보 가져옴"
    )
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
                                        "statusCode": 404,
                                        "statusCodeSeries": 4,
                                        "errorCode": 200,
                                        "errorName": "MEMBER_OF_THE_MEMBER_ID_NOT_FOUND",
                                        "message": "memberId에 해당하는 회원이 없습니다"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiBasicResponse<MemberDto>> whoAmI() {
        long id;
        try {
            id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("Member Not allowed", e);
        }
        if (log.isTraceEnabled()) {
            log.trace("Logged In OAuth 2.0 Member={}", id);
        }
        MemberDto memberDto = this.memberService.findMember(id);
        return ResponseEntity.ok(
                ApiBasicResponse.of(
                        memberDto,
                        HttpStatus.OK,
                        "당신이 누군지 알겠습니다"
                )
        );
    }

    @DeleteMapping("/refresh-token")
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 저장하고 있는 쿠키를 삭제함으로써 로그아웃 + 그외 로그인 시 생성된 쿠키 삭제",
            responses = {
                    @ApiResponse(
                            headers = {
                                    @Header(
                                            name = HttpHeaders.SET_COOKIE,
                                            description = "Refresh Token을 담고 있는 쿠키를 삭제하기 위한 헤더"
                                    ),
                                    @Header(
                                            name = HttpHeaders.SET_COOKIE,
                                            description = "Access Token을 담고 있는 쿠키를 삭제하기 위한 헤더"
                                    ),
                                    @Header(
                                            name = HttpHeaders.SET_COOKIE,
                                            description = "email을 담고 있는 쿠키를 삭제하기 위한 헤더"
                                    ),
                                    @Header(
                                            name = HttpHeaders.SET_COOKIE,
                                            description = "is_new_member을 담고 있는 쿠키를 삭제하기 위한 헤더"
                                    ),
                                    @Header(
                                            name = HttpHeaders.SET_COOKIE,
                                            description = "name을 담고 있는 쿠키를 삭제하기 위한 헤더"
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<Void> deleteRefreshToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, this.httpCookieUtils.buildCookieInvalidator(CookieConstants.COOKIE_NAME_REFRESH_TOKEN.getString()).toString());
        headers.add(HttpHeaders.SET_COOKIE, this.httpCookieUtils.buildCookieInvalidator(CookieConstants.COOKIE_NAME_ACCESS_TOKEN.getString()).toString());
        headers.add(HttpHeaders.SET_COOKIE, this.httpCookieUtils.buildCookieInvalidator(CookieConstants.COOKIE_NAME_EMAIL.getString()).toString());
        headers.add(HttpHeaders.SET_COOKIE, this.httpCookieUtils.buildCookieInvalidator(CookieConstants.COOKIE_NAME_IS_NEW_MEMBER.getString()).toString());
        headers.add(HttpHeaders.SET_COOKIE, this.httpCookieUtils.buildCookieInvalidator(CookieConstants.COOKIE_NAME_NAME.getString()).toString());
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }
}
