package blankspace.blankspaceprj.jwt;

import blankspace.blankspaceprj.dto.MemberVO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //private final AuthTokenProvider tokenProvider;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JwtTokenProvider tokenProvider;

    @Value("${jwt.secret.key}")
    String jwtSecretKey;

    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {

        final String authorizationHeader = request.getHeader("Bearar");

        logger.info("헤더값: " + request.getHeader("Bearar"));
        if (authorizationHeader != null) {
            //String tokenStr = JwtHeaderUtil.getAccessToken(request);
            //AuthToken token = tokenProvider.convertAuthToken(tokenStr);

            if (validateToken(authorizationHeader)) {
                logger.info("헤더 토큰 인증 성공");

                Authentication authentication = tokenProvider.getAuthentication(authorizationHeader);
                // 해당 스프링 시큐리티 유저를 시큐리티 건텍스트에 저장, 즉 디비를 거치지 않음
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
//
            filterChain.doFilter(request, response);
        }

    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}