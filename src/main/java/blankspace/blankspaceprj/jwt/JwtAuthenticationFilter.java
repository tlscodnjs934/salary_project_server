package blankspace.blankspaceprj.jwt;

import blankspace.blankspaceprj.dto.MemberVO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebFilter
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter( JwtTokenProvider jwtTokenProvider ) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {

        final String authorizationHeader = request.getHeader("Bearar");

        if (authorizationHeader != null) {
            //String tokenStr = JwtHeaderUtil.getAccessToken(request);
            //AuthToken token = tokenProvider.convertAuthToken(tokenStr);

            logger.info("헤더값: " + request.getHeader("Bearar"));
            if (jwtTokenProvider.validateToken(authorizationHeader)) {
                logger.info("헤더 토큰 인증 성공");

                Authentication authentication = jwtTokenProvider.getAuthentication(authorizationHeader);
                // 해당 스프링 시큐리티 유저를 시큐리티 건텍스트에 저장, 즉 디비를 거치지 않음
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }

        filterChain.doFilter(request, response);

    }


}