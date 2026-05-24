package com.example.rpg2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.domain.BattleState;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.service.battle.BattleManagementService;
import com.example.rpg2.service.battle.CreateCharacterSet;

@ExtendWith(MockitoExtension.class)
class AttackControllerTest {

    @Mock
    private BattleManagementService battleManagementService;

    @Mock
    private CreateCharacterSet createCharacterSet;

    // 定数
    private final String BattleScreen    = "battle";
    private final String BattleRecordKey = "battleRecord";
    private final String BattleStateKey  = "battleState";
    private final String ScreenMode      = "mode";
    private final String BeforeTurn      = "log";
    private final String NormalAttack    = "attackTargetMonster";

    @InjectMocks // 依存性を自動注入
    private AttackController attackController;
    private MockHttpSession session;

    @BeforeEach // 各テストメソッドの実行前に必ず呼ばれる初期化処理
    void setUp() {
        session = new MockHttpSession();
    }

    // --- attack() のテスト ---------------------------------------------------
    @Test
    void attack_ビュー名にbattleが設定される() {
        int myKey = 3;
        ModelAndView mv = attackController.attack(myKey , new ModelAndView() , session);
        assertThat(mv.getViewName()).isEqualTo(BattleScreen);
    }

    @Test
    void attack_ビュー名にmyKeyが設定される() {
        int myKey = 3;
        ModelAndView mv = attackController.attack(myKey , new ModelAndView() , session);
        assertThat(mv.getModel().get("myKey")).isEqualTo(myKey);
    }

    @Test
    void attack_攻撃モードがセッションに記録される(){
        attackController.attack(3, new ModelAndView(), session);
        assertThat(session.getAttribute(ScreenMode)).isEqualTo(NormalAttack);
    }

    // --- attackTargetMonster() のテスト ---------------------------------------------------
    @Test
    void attackTargetMonster_ビュー名にbattleが設定される() {
        int myKey = 3;
        int targetKey = 3;
        ModelAndView mv = attackController.attackTargetMonster(myKey , targetKey , new ModelAndView() , session);
        assertThat(mv.getViewName()).isEqualTo(BattleScreen);
    }

    @Test
    void attackTargetMonster_サービスへ各KeyとBattleRecordとBattleStateが渡される() {
        int myKey = 3;
        int targetKey = 3;
        BattleRecord battleRecord = new BattleRecord(Map.of(), Map.of(), List.of());
        BattleState battleState = new BattleState();
        session.setAttribute(BattleRecordKey, battleRecord);
        session.setAttribute(BattleStateKey, battleState);

        attackController.attackTargetMonster(myKey, targetKey, new ModelAndView(), session);

        verify(battleManagementService).selectionAttack(myKey, targetKey, battleRecord, battleState);
    }

    @Test
    void attackTargetMonster_セッションにbattleRecordとbattleStateが再格納される(){
        int myKey = 3;
        int targetKey = 3;
        BattleState battleState = new BattleState();
        session.setAttribute(BattleStateKey, battleState);
        attackController.attackTargetMonster(myKey, targetKey, new ModelAndView(), session);

        assertThat(session.getAttribute(BattleStateKey)).isSameAs(battleState);
    }

    @Test
    void attackTargetMonster_ログモードがセッションに記録される(){
        int myKey = 3;
        int targetKey = 3;
        BattleState battleState = new BattleState();
        session.setAttribute(BattleStateKey, battleState);

        attackController.attackTargetMonster(myKey, targetKey, new ModelAndView(), session);
        assertThat(session.getAttribute(ScreenMode)).isEqualTo(BeforeTurn);
    }
}
