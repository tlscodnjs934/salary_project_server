package blankspace.blankspaceprj.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.springframework.util.StringUtils.split;

@Component
public class JwtTokenProvider {
    private final long TOKEN_VALID_MILISECOND = 1000L * 60 * 60 * 10; // 10시간

    @Value("${jwt.secret.key}")
    private String secretKey;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserUserDetailsService userDetailsService;

    public JwtTokenProvider(//@Qualifier("UserUserDetailsService")
                            UserUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // Jwt 토큰 생성
    public String createToken(String userPk) {
        logger.info("createToken secretKey : " + secretKey);
        Claims claims = Jwts.claims().setSubject(userPk);
        //claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now)   // 토큰 발행 일자
                .setExpiration(new Date(now.getTime() + TOKEN_VALID_MILISECOND)) // 만료 기간
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret 값
                .compact(); // Token 생성
    }

    // 인증 성공시 SecurityContextHolder에 저장할 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        logger.info("this.getUserPk(token) token : " + secretKey);
        logger.info("getAuthentication jwtSecretKey : " + secretKey);
        logger.info("getAuthentication jwtSecretKey getBytes : " + token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Jwt Token에서 User PK 추출
    public String getUserPk(String token) {
        logger.info("getUserPk secretKey : " + secretKey);
        return Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        return req.getHeader("Bearer");
    }

    public boolean validateToken(String token) {
        try {
            logger.info("token : "+token);
            logger.info("validateToken : "+secretKey);

            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다." + e);
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다." + e);
        }
        return false;
    }

}