package com.jd.pay.base.engine;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @description: Swagger配置文件
 *
 * @author: liuX
 * @time: 2020/5/30 10:32
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    @Bean
    public Docket customDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jd.pay"))
                .paths(urlFilter())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(serviceName + "服务接口文档")
                .description(serviceName + "服务接口文档")
                .version("v1.0.0")
                .contact(new Contact("优品支付中台项目", "swagger-ui.html", ""))
                .build();
    }

    private Predicate<String> urlFilter() {
        return Predicates.not(Predicates.or(PathSelectors.regex("/error.*"),
                PathSelectors.regex("/v1/api.*")));
    }


}
