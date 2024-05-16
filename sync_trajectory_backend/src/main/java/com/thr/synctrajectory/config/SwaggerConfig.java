package com.thr.synctrajectory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 自定义 Swagger 接口文档的配置
 *
 * @author thr
 */
@Configuration  // 配置类
@EnableSwagger2  // 开启 swagger2 的自动配置
@Profile({"dev"})  // 仅在 dev 环境下才会被加载
public class SwaggerConfig {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 标注控制器位置
                .apis(RequestHandlerSelectors.basePackage("com.thr.synctrajectory.controller"))
                .paths(PathSelectors.any())  // 选择路径
                .build()
                .pathMapping("/api");
    }

    // 基本信息的配置，信息会在api文档上显示
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("同步轨迹项目-接口文档") // 标题
                .description("帮助志同道合的人们找到交汇点，一起探索技术之旅") // 描述)
                .version("1.0")
                .build();
    }
}