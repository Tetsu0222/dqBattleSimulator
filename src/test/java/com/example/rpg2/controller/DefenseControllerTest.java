package com.example.rpg2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.domain.BattleState;
import com.example.rpg2.service.battle.BattleManagementService;

@ExtendWith(MockitoExtension.class) // JUnit5でMockitoを使うための拡張機能を有効化するアノテーション
class DefenseControllerTest {

    @Mock // @ExtendWith(MockitoExtension.class)で自動的に初期化
    private BattleManagementService battleManagementService;

    // 定数
    private final String BattleScreen    = "battle";
    private final String BattleStateKey  = "battleState";

    @InjectMocks // 依存性を自動注入
    private DefenseController defenseController;
    private MockHttpSession session;

    @BeforeEach // 各テストメソッドの実行前に必ず呼ばれる初期化処理
    void setUp() {
        session = new MockHttpSession();
    }

    // --- defense() のテスト ---------------------------------------------------
    @Test
    void defense_ビュー名にbattleが設定される() {
        Integer myKey = 3;
        ModelAndView mv = defenseController.defense(myKey , new ModelAndView() , session);
        assertThat(mv.getViewName()).isEqualTo(BattleScreen);
    }

    @Test
    void defense_サービスへmyKeyとBattleStateが渡される() {
        // BeforeEachでセッションが初期化されるため、インスタンス化
        BattleState state = new BattleState();
        session.setAttribute(BattleStateKey, state);

        defenseController.defense(3, new ModelAndView(), session);

        // Mockitoのverify()はモックに対するメソッド呼び出しを監視する。
        // コントローラが正しい引数でサービスを呼んだか検証
        // verify はモックに対してしか使えない。
        verify(battleManagementService).selectionDefense(3, state);
    }

    @Test
    void defense_セッションにbattleStateが再格納される(){
        BattleState state = new BattleState();
        session.setAttribute(BattleStateKey, state);

        defenseController.defense(3, new ModelAndView(), session);

        // 同じインスタンス同時か比較している。
        assertThat(session.getAttribute(BattleStateKey)).isSameAs(state);
    }
}
