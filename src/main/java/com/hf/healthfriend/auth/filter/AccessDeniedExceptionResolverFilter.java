package com.hf.healthfriend.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.global.exception.ErrorCode;
import com.hf.healthfriend.global.spec.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * AuthorizationFilter에 EventPublisher를 설정하거나 하는 식으로
 * AccessDeniedException을 제대로 처리할 수 없을 것 같아서 아예 이 Filter에서
 * AccessDeniedException을 처리
 *
 * @author PGD
 * @see org.springframework.security.web.access.intercept.AuthorizationFilter
 * @see org.springframework.security.access.AccessDeniedException
 */
@Slf4j
@RequiredArgsConstructor
public class AccessDeniedExceptionResolverFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            // TODO: AccessDeniedException 종류를 여러 개
            writeErrorResponse(response);
            throw e;
        } catch (ServletException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause instanceof AccessDeniedException) {
                writeErrorResponse(response);
            } else {
                throw e;
            }
        }
    }

    private void writeErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse responseBody = ApiErrorResponse.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .statusCodeSeries(HttpStatus.FORBIDDEN.series().value())
                .errorName(ErrorCode.UNAUTHORIZED.name())
                .errorCode(ErrorCode.UNAUTHORIZED.code())
                .message(ErrorCode.UNAUTHORIZED.message())
                .build();
        try (BufferedWriter bw = new BufferedWriter(response.getWriter())) {
            bw.write(this.objectMapper.writeValueAsString(responseBody));
        }
    }
}
