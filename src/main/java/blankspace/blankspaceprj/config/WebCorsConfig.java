package blankspace.blankspaceprj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedOrigins("https://nid.naver.com/oauth2.0")
                .allowedOrigins("https://kauth.kakao.com/oauth")
                .allowedOrigins("https://kapi.kakao.com")
                .allowedOrigins("https://accounts.google.com")
                .allowedOrigins("https://www.googleapis.com")
                .allowedMethods("OPTIONS", "GET","POST","PUT","DELETE");

    }
}
