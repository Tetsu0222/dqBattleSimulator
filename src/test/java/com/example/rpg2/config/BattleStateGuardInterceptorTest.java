package com.example.rpg2.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.example.rpg2.domain.BattleState;

class BattleStateGuardInterceptorTest {

    private BattleStateGuardInterceptor interceptor;
    @NonNull private MockHttpServletRequest  request  = new MockHttpServletRequest();
    @NonNull private MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        interceptor = new BattleStateGuardInterceptor();
        request     = new MockHttpServletRequest();
        response    = new MockHttpServletResponse();
    }

    private boolean callPreHandle() throws Exception {
        return interceptor.preHandle(request, response, new Object());
    }

    // --- preHandle() のテスト ------------------------------------------------

    @Test
    void preHandle_battleStateがセッションにあれば処理を継続する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("battleState", new BattleState());
        request.setSession(session);

        boolean result = callPreHandle();

        assertThat(result).isTrue();
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void preHandle_battleStateがセッションになければルートへリダイレクトする() throws Exception {
        request.setSession(new MockHttpSession()); // 空のセッション

        boolean result = callPreHandle();

        assertThat(result).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void preHandle_セッション自体が存在しなければルートへリダイレクトする() throws Exception {
        // request.setSession() を呼ばず、getSession(false) が null を返す状態にする

        boolean result = callPreHandle();

        assertThat(result).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}
