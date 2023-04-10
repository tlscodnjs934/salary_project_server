package blankspace.blankspaceprj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("blankspace.blankspaceprj"))
                .paths(PathSelectors.any())
                .build();
    }

    // 화면에 보여질 설정값들
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("끌린더 API 목록")
                .description("끌린더에서 호출가능한 컨트롤러 목록입니다.")
                .version("1.0")
                .build();
    }


}