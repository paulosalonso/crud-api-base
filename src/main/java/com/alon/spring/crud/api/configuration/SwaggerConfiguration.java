package com.alon.spring.crud.api.configuration;

import com.alon.spring.crud.api.documentation.PageableDocumentation;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableWebMvc
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Autowired(required = false)
    private SwaggerCustomization swaggerCustomization;

    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
        		.ignoredParameterTypes(ServletWebRequest.class)
				.select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(Predicates.and(
                            Predicates.not(PathSelectors.ant("/error")), // Removes basic-error-controller
                            PathSelectors.any()))
				.build()
                .directModelSubstitute(Pageable.class, PageableDocumentation.class);

        if (swaggerCustomization != null)
            swaggerCustomization.configure(docket);

        return docket;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    public interface SwaggerCustomization {
        void configure(Docket docket);
    }
    
}