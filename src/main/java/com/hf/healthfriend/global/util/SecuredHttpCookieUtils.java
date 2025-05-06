package com.hf.healthfriend.global.util;

import com.hf.healthfriend.auth.constant.CookieConstants;
import org.springframework.http.ResponseCookie;

public class SecuredHttpCookieUtils implements HttpCookieUtils {
    private final String domain;

    public SecuredHttpCookieUtils(String domain) {
        this.domain = domain;
    }

    @Override
    public ResponseCookie buildHttpOnlyResponseCookie(String name, String value) {
        return buildCookie(name, value, true);
    }

    @Override
    public ResponseCookie buildJavaScriptAccessibleResponseCookie(String name, String value) {
        return buildCookie(name, value, false);
    }

    private ResponseCookie buildCookie(String name, String value, boolean httpOnly) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .maxAge(CookieConstants.COOKIE_DURATION_IN_SECONDS.getInt())
                .domain(this.domain)
                .path("/")
                .sameSite("None")
                .secure(true) // localhost 환경에서는 secure 쿠키도 전달됨
                .build();
    }

    @Override
    public ResponseCookie buildCookieInvalidator(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .maxAge(0)
                .domain(this.domain)
                .path("/")
                .sameSite("None")
                .secure(true) // localhost 환경에서는 secure 쿠키도 전달됨
                .build();
    }
}
