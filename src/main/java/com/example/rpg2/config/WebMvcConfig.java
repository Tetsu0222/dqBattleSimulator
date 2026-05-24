package com.example.rpg2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @NonNull
    private final BattleStateGuardInterceptor battleStateGuardInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(battleStateGuardInterceptor)
                .addPathPatterns(
                        "/defense/**",
                        "/magic/**",
                        "/skill/**",
                        "/target/magic/**",
                        "/target/skill/**",
                        "/target/attack/**"
                );
    }
}
