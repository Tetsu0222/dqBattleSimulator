package com.example.rpg2.config;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// リクエスト時：対象URLにアクセスが来ると、Springが自動的に BattleStateGuardInterceptor.preHandle() を呼び出す
@Component
public class BattleStateGuardInterceptor implements HandlerInterceptor {

    private static final String BATTLE_STATE_KEY = "battleState";
    private static final String REDIRECT_PATH    = "/";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(BATTLE_STATE_KEY) == null) {
            response.sendRedirect(request.getContextPath() + REDIRECT_PATH);
            return false;
        }
        return true;
    }
}
