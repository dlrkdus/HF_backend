package com.hf.healthfriend.global.util;

import org.springframework.http.ResponseCookie;

/**
 * HTTP 통신에서 사용하는 Cookie를 생성하는 Utils interface
 * 로컬 개발 환경에서는 HTTP-only, secure 옵션을 안 붙여도 되지만
 * 배포 환경에서는 보안을 위해 붙여야 한다.
 * 환경에 따라 다른 Cookie를 사용하기 위해 정의
 *
 * @author PGD
 */
public interface HttpCookieUtils {

    /**
     * ResponseEntity에 담을 ResponseCookie를 생성하는 메소드.
     * HTTP-only 쿠키를 생성한다.
     * @param name Cookie key
     * @param value Cookie value
     * @return 클라이언트에 저장할 ResponseCookie 정보. HTTP-only 쿠키가 아니다.
     */
    ResponseCookie buildHttpOnlyResponseCookie(String name, String value);

    /**
     * ResponseEntity에 담을 ResponseCookie를 생성하는 메소드.
     * HTTP-only가 아닌 쿠키를 생성한다.
     * @param name Cookie key
     * @param value Cookie value
     * @return 클라이언트에 저장할 ResponseCookie 정보. HTTP-only 쿠키가 아니다.
     */
    ResponseCookie buildJavaScriptAccessibleResponseCookie(String name, String value);

    ResponseCookie buildCookieInvalidator(String name);
}
