package com.example.rpg2.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.service.battle.BattleManagementService;

@ExtendWith(MockitoExtension.class) // JUnit5でMockitoを使うための拡張機能を有効化するアノテーション
class DefenseControllerTest {

    @Mock // @ExtendWith(MockitoExtension.class)で自動的に初期化
    private BattleManagementService battleManagementService;

    @InjectMocks // 依存性を自動注入
    private DefenseController defenseController;
    private MockHttpSession session;

    @BeforeEach // 各テストメソッドの実行前に必ず呼ばれる初期化処理
    void setUp() {
        session = new MockHttpSession();
    }

    // --- defense() の観点 ---------------------------------------------------
    /*
    1. ビュー名の検証
    defense_ビュー名にbattleが設定される。
    戻り値 ModelAndView の getViewName() が "battle" であること。

    2. サービス呼び出しの検証（最重要）
    defense_BattleManagementServiceへmyKeyとBattleStateが渡される。
    battleManagementService.selectionDefense(myKey, battleState) が正しい引数で1回呼ばれること。
    ArgumentCaptor か verify(...).selectionDefense(eq(3), same(state)) で確認。
    defense_PathVariableの値がそのままサービスに渡される。
    @PathVariable int myKey が変換ロスなくサービス層に届くか（例: 0, 1, Integer.MAX_VALUE などの境界値）。

    3. セッション操作の検証
    defense_セッションのbattleStateが取得されサービスに渡される。
    事前に session.setAttribute("battleState", state) した BattleState インスタンスが、サービス呼び出しの第2引数として 同一参照 (isSameAs) で渡されること。
    defense_セッションにbattleStateが再格納される。
    処理後 session.getAttribute("battleState") が元のインスタンスと同一であること（setAttribute が呼ばれているかの確認）。

    4. 異常系・エッジケース
    defense_セッションにbattleStateが存在しない場合_nullがサービスに渡される。
    session が空のとき、現状コードは null チェックせずサービスを呼ぶ。
    これを「仕様」として固定するか、防御的コードを追加すべきか判断する観点。
     → 現状の挙動を確定させるテストにするなら、verify で第2引数が null で呼ばれることを確認。

    5. 呼び出し回数の検証
    defense_サービスは1回だけ呼ばれる。
    verify(battleManagementService, times(1)).selectionDefense(...) で重複呼び出しがないこと。
    */

    // --- defense() のテスト ---------------------------------------------------
    @Test
    void defense_ビュー名にbattleが設定される() {
        Integer myKey = 3;
        ModelAndView mv = defenseController.defense(myKey , new ModelAndView() , session);
        assertThat(mv.getViewName()).isEqualTo("battle");
    }
}
