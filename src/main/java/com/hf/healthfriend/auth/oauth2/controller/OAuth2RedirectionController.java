package com.hf.healthfriend.auth.oauth2.controller;

import com.hf.healthfriend.auth.constant.CookieConstants;
import com.hf.healthfriend.auth.oauth2.constant.AuthServer;
import com.hf.healthfriend.auth.oauth2.dto.propertyeditor.AuthServerEditor;
import com.hf.healthfriend.auth.oauth2.dto.response.GrantedTokenInfo;
import com.hf.healthfriend.auth.oauth2.tokensupport.OAuth2TokenSupport;
import com.hf.healthfriend.domain.member.service.MemberService;
import com.hf.healthfriend.global.util.HttpCookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 Authorization Server에 등록된 Redirect URI Endpoint를 정의한 Controller.
 * 인가코드가 Query Parameter로 전해지면 Authorization Server에 Access Token, Refresh Token, Email 정보를
 * 받아와 요청이 들어온 클라이언트에 전달(Response)한다.
 *
 * @author PGD
 */
@Slf4j
@RestController
@RequestMapping("/oauth/code")
public class OAuth2RedirectionController {
    private static final String REDIRECTION_PATH = "/agreement";

    private final Map<AuthServer, OAuth2TokenSupport> tokenSupportByName;
    private final HttpCookieUtils cookieUtils;
    private final MemberService memberService;
    private final String clientOrigin;
    private final String backendOrigin;

    public OAuth2RedirectionController(List<OAuth2TokenSupport> oAuth2TokenSupports,
                                       HttpCookieUtils cookieUtils,
                                       MemberService memberService,
                                       @Value("${client.origin}") String clientOrigin,
                                       @Value("${backend.origin}") String backendOrigin) {
        this.tokenSupportByName = new HashMap<>();
        for (OAuth2TokenSupport tokenSupport : oAuth2TokenSupports) {
            for (AuthServer authServer : AuthServer.values()) {
                if (tokenSupport.supports(authServer)) {
                    this.tokenSupportByName.put(authServer, tokenSupport);
                }
            }
        }

        this.cookieUtils = cookieUtils;
        this.memberService = memberService;
        this.clientOrigin = clientOrigin;
        this.backendOrigin = backendOrigin;
    }

    @InitBinder
    public void registerBinder(WebDataBinder binder) {
        binder.registerCustomEditor(AuthServer.class, AuthServerEditor.getInstance());
    }

    @Operation(summary = "카카오 로그인 Redirect URI")
    @ApiResponses({
            @ApiResponse(
                    description = "카카오 로그인 성공 및 Access Token, Refresh Token, email 반환",
                    responseCode = "308",
                    headers = {
                            @Header(
                                    name = "Location",
                                    description = "카카오 로그인 성공 후 리다이렉션할 페이지\n/register/exercise-style로 리다이렉션"
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = """
                                            발급받은 토큰 정보를 Cookie에 담아서 반환
                                            쿠키 정보
                                            - refresh_token: Refresh Token을 담은 HTTP-only, Secure, Same-site: None 쿠키
                                            - access_token: Access Token을 담은 Secure, Same-site: None 쿠키
                                            - email: 카카오 Authorization Server로부터 가져온 email을 담은 Secure, Same-site: None 쿠키
                                            - name: 카카오 Authorization Server로부터 가져온 회원의 이름, Secure, Same-site: None 쿠키
                                            name은 URI 인코딩이 되어 있기 때문에 디코딩해야 함 ("%EB%B0%95%EA%B2%BD... 형식")
                                            - is_new_member: 신규 회원일 경우 true, 그렇지 않으면 false, Secure, Same-site: None 쿠키
                                            """
                            )
                    }
            ),
            @ApiResponse(
                    description = "유효하지 않은 인가코드로 인한 카카오 로그인 실패 - 로그인 페이지로 리다이렉션",
                    responseCode = "307",
                    headers = @Header(
                            name = "Location",
                            description = """
                                    카카오 로그인 실패로 인해 리다이렉션될 로그인 페이지
                                    /login 으로 이동
                                    Query Parameters:
                                      - error-code: 102
                                      - error-name: INVALID_CODE,
                                      - message: 유효하지 않은 인가코드입니다
                                    """
                    )
            )
    })
    @GetMapping("/kakao")
    public ResponseEntity<Void> get(@RequestParam("code") String code) {
        log.info("Kakao Login has been requested");
        return doTheSameThing(AuthServer.KAKAO, code, this.backendOrigin + "/oauth/code/kakao");
    }

    @Operation(summary = "구글 로그인 Redirect URI")
    @ApiResponses({
            @ApiResponse(
                    description = "구글 로그인 성공 및 Access Token, Refresh Token, email 반환",
                    responseCode = "308",
                    headers = {
                            @Header(
                                    name = "Location",
                                    description = "구글 로그인 성공 후 리다이렉션할 페이지\n/register/exercise-style 로 이동"
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = """
                                            발급받은 토큰 정보를 Cookie에 담아서 반환
                                            쿠키 정보
                                            - refresh_token: Refresh Token을 담은 HTTP-only, Secure, Same-site: None 쿠키
                                            - access_token: Access Token을 담은 Secure, Same-site: None 쿠키
                                            - email: 구글 Authorization Server로부터 가져온 email을 담은 Secure, Same-site: None 쿠키
                                            - name: 구글 Authorization Server로부터 가져온 회원의 이름, Secure, Same-site: None 쿠키
                                            name은 URI 인코딩이 되어 있기 때문에 디코딩해야 함 ("%EB%B0%95%EA%B2%BD... 형식")
                                            - is_new_member: 신규 회원일 경우 true, 그렇지 않으면 false, Secure, Same-site: None 쿠키
                                            """
                            )
                    }
            ),
            @ApiResponse(
                    description = "유효하지 않은 인가코드로 인한 구글 로그인 실패",
                    responseCode = "307",
                    headers = @Header(
                            name = "Location",
                            description = """
                                    구글 로그인 실패로 인해 리다이렉션될 로그인 페이지
                                    /login 으로 이동
                                    Query Parameters:
                                      - error-code: 102
                                      - error-name: INVALID_CODE,
                                      - message: 유효하지 않은 인가코드입니다
                                    """
                    )
            )
    })
    @GetMapping("/google")
    public ResponseEntity<Void> getGoogle(@RequestParam("code") String code) {
        log.info("Google Login has been requested");
        return doTheSameThing(AuthServer.GOOGLE, code, this.backendOrigin + "/oauth/code/google");
    }

    private ResponseEntity<Void> doTheSameThing(AuthServer authServer, String code, String redirectUri) {
        OAuth2TokenSupport tokenSupport = this.tokenSupportByName.get(authServer);

        // 너무 간단한 로직이므로 Service 객체를 따로 정의하지 않고 여기서 했다.
        GrantedTokenInfo grantedTokenInfo = tokenSupport.grantToken(code, redirectUri);

        boolean memberExists = this.memberService.isMemberOfEmailExists(grantedTokenInfo.getEmail());

        ResponseCookie refreshTokenCookie =
                this.cookieUtils.buildHttpOnlyResponseCookie(CookieConstants.COOKIE_NAME_REFRESH_TOKEN.getString(),
                        grantedTokenInfo.getRefreshToken());
        ResponseCookie accessTokenCookie =
                this.cookieUtils.buildJavaScriptAccessibleResponseCookie(CookieConstants.COOKIE_NAME_ACCESS_TOKEN.getString(),
                        grantedTokenInfo.getAccessToken());
        ResponseCookie emailCookie =
                this.cookieUtils.buildJavaScriptAccessibleResponseCookie(CookieConstants.COOKIE_NAME_EMAIL.getString(),
                        grantedTokenInfo.getEmail());
        ResponseCookie nameCookie =
                this.cookieUtils.buildJavaScriptAccessibleResponseCookie(CookieConstants.COOKIE_NAME_NAME.getString(),
                        URLEncoder.encode(grantedTokenInfo.getName(), StandardCharsets.UTF_8));
        ResponseCookie newMemberCookie =
                this.cookieUtils.buildJavaScriptAccessibleResponseCookie(CookieConstants.COOKIE_NAME_IS_NEW_MEMBER.getString(),
                        String.valueOf(!memberExists));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, emailCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, nameCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, newMemberCookie.toString());
        headers.add(HttpHeaders.LOCATION, this.clientOrigin + REDIRECTION_PATH);

        return new ResponseEntity<>(
                headers,
                HttpStatus.PERMANENT_REDIRECT
        );
    }
}
