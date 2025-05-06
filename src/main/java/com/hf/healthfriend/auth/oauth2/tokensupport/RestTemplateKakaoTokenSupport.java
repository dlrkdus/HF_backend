package com.hf.healthfriend.auth.oauth2.tokensupport;

import com.hf.healthfriend.auth.exception.InvalidCodeException;
import com.hf.healthfriend.auth.oauth2.constant.AuthServer;
import com.hf.healthfriend.auth.oauth2.dto.response.GrantedTokenInfo;
import com.hf.healthfriend.auth.oauth2.dto.response.TokenValidationInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 추후 WebClient를 사용하든가, 다른 방식을 사용하는 객체로 대체할 예정
 */
@Slf4j
@Component
@Profile("!no-auth")
public class RestTemplateKakaoTokenSupport implements KakaoOAuth2TokenSupport {
    private static final String KAKAO_TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_INFO_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_ACCESS_TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String TOKEN_REQUEST_HEADER_KEY_GRANT_TYPE = "grant_type";
    private static final String TOKEN_REQUEST_HEADER_VALUE_GRANT_TYPE = "authorization_code";
    private static final String TOKEN_REQUEST_HEADER_KEY_CLIENT_ID = "client_id";
    private static final String TOKEN_REQUEST_HEADER_KEY_REDIRECT_URI = "redirect_uri";
    private static final String TOKEN_REQUEST_HEADER_KEY_CODE = "code";

    private final RestTemplate restTemplate;
    private final String kakaoClientId;

    public RestTemplateKakaoTokenSupport(RestTemplate restTemplate,
                                         @Value("${secret.oauth2.kakao.client-id}") String kakaoClientId) {
        this.restTemplate = restTemplate;
        this.kakaoClientId = kakaoClientId;
    }

    @Override
    public GrantedTokenInfo grantToken(String code, String redirectUri) throws InvalidCodeException {
        log.debug("redirectUri={}", redirectUri);
        LocalDateTime recordNow = LocalDateTime.now();

        ResponseEntity<String> responseEntity;
        try {
            responseEntity =
                    this.restTemplate.exchange(buildTokenRequestEntity(code, redirectUri), String.class);
        } catch (Exception e) {
            throw new InvalidCodeException("유효하지 않은 인가 코드: " + code, e);
        }

        JSONObject responseBody =
                new JSONObject(responseEntity.getBody());
        log.trace("Response\n{}", responseBody);

        String accessToken = responseBody.getString("access_token");

        JSONObject kakaoUserInfoAsJson = fetchKakaoUserInfoAsJson(accessToken);

        return new GrantedTokenInfo(
                accessToken,
                responseBody.getString("refresh_token"),
                recordNow.plus(responseBody.getInt("expires_in"), ChronoUnit.SECONDS),
                kakaoUserInfoAsJson.getString("email"),
                kakaoUserInfoAsJson.getString("name"),
                AuthServer.KAKAO
        );
    }

    private RequestEntity<MultiValueMap<String, String>> buildTokenRequestEntity(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(TOKEN_REQUEST_HEADER_KEY_GRANT_TYPE, TOKEN_REQUEST_HEADER_VALUE_GRANT_TYPE);
        params.add(TOKEN_REQUEST_HEADER_KEY_CLIENT_ID, this.kakaoClientId);
        params.add(TOKEN_REQUEST_HEADER_KEY_REDIRECT_URI, redirectUri);
        params.add(TOKEN_REQUEST_HEADER_KEY_CODE, code);

        return RequestEntity.post(KAKAO_TOKEN_REQUEST_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(params);
    }

    @Override
    public String requestEmail(String accessToken) {
        JSONObject jsonObject = fetchKakaoUserInfoAsJson(accessToken);
        return jsonObject.getString("email");
    }

    private JSONObject fetchKakaoUserInfoAsJson(String accessToken) {
        RequestEntity<String> requestEntity = RequestEntity.post(KAKAO_INFO_REQUEST_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body("property_keys=[\"kakao_account.email\",\"kakao_account.name\"]");
        ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
        return new JSONObject(responseEntity.getBody()).getJSONObject("kakao_account");
    }

    @Override
    public TokenValidationInfo validateToken(String token) throws RuntimeException {
        return new TokenValidationInfo(
                requestEmail(token)
        );
    }

    @Override
    public String refreshToken(String refreshToken) throws RuntimeException {
        MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
        bodyParams.add("grant_type", "refresh_token");
        bodyParams.add("client_id", this.kakaoClientId);
        bodyParams.add("refresh_token", refreshToken);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity.post(KAKAO_TOKEN_REQUEST_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(bodyParams);

        JSONObject responseBody = new JSONObject(this.restTemplate.exchange(requestEntity, String.class).getBody());

        if (log.isTraceEnabled()) {
            log.trace("Response Body:\n{}", responseBody);
        }

        return responseBody.getString("access_token");
    }

    @Override
    public boolean supports(AuthServer authServer) {
        return authServer == AuthServer.KAKAO;
    }
}
