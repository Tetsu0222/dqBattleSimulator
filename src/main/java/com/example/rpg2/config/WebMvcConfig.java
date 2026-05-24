package com.example.rpg2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

// アプリ起動時：WebMvcConfig が「/defense/** など6つのURLに BattleStateGuardInterceptor を貼り付けて」とSpringに依頼する
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer { // WebMvcConfigurer → Spring MVCの設定をカスタマイズするための入り口

    @NonNull
    private final BattleStateGuardInterceptor battleStateGuardInterceptor;

    @Override // インターセプター（割り込み処理）をオーバーライドして登録
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 「このインターセプターを、これらのURLパターンに適用する」と具体的に指示
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
