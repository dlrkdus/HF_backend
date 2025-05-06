package com.hf.healthfriend.auth.filter;

import com.hf.healthfriend.auth.authentication.NoAuthAuthentication;
import com.hf.healthfriend.auth.oauth2.tokensupport.OAuth2TokenSupport;
import com.hf.healthfriend.domain.member.constant.Role;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Profile("no-auth")
@RequiredArgsConstructor
public class JsonParserFilter extends OncePerRequestFilter {
    private final List<OAuth2TokenSupport> tokenSupports;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            log.info("No authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        if (authorization.startsWith("Bearer ")) {
            authorization = authorization.substring("Bearer ".length());
        }

        String email = null;
        for (OAuth2TokenSupport tokenSupport : tokenSupports) {
            try {
                email = tokenSupport.requestEmail(authorization);
            } catch (Exception e) {
                log.trace("{} not works for {}", tokenSupport.getClass(), authorization, e);
            }
        }

        if (email == null) {
            log.info("No token support for Authorization: {}", authorization);
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Member> memberOp = this.memberRepository.findNotDeletedMemberByEmail(email);
        if (memberOp.isEmpty()) {
            log.info("No such member of email: {}", email);
            filterChain.doFilter(request, response);
            return;
        }

        Member member = memberOp.get();

        Authentication authentication = new NoAuthAuthentication(String.valueOf(member.getId()), new SimpleGrantedAuthority(Role.ROLE_MEMBER.name()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
