package com.example.rpg2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.domain.EnemyBuildResult;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.domain.PartyBuildResult;
import com.example.rpg2.dto.request.BattleStartRequest;
import com.example.rpg2.dto.response.AllySummary;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.dto.response.MonsterSummary;
import com.example.rpg2.service.battle.CreateCharacterSet;
import com.example.rpg2.repository.AllyRepository;
import com.example.rpg2.repository.MonsterRepository;
import com.example.rpg2.service.battle.BattleManagementService;

@ExtendWith(MockitoExtension.class)
class StartControllerTest {

    @Mock
    private CreateCharacterSet createCharacterSet;

    @Mock
    private AllyRepository allyRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private BattleManagementService battleManagementService;

    @InjectMocks
    private StartController startController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    // --- Index() のテスト ---------------------------------------------------

    @Test
    void Index_ビュー名にindexが設定される() {
        when(allyRepository.findAllProjectedBy()).thenReturn(List.of());
        when(monsterRepository.findAllProjectedBy()).thenReturn(List.of());

        ModelAndView mv = startController.Index(new ModelAndView(), session);

        assertThat(mv.getViewName()).isEqualTo("index");
    }

    @Test
    void Index_味方と敵の選択肢がモデルに格納される() {
        AllySummary ally = stubAlly(1, "勇者");
        MonsterSummary monster = stubMonster(101, "スライム");
        when(allyRepository.findAllProjectedBy()).thenReturn(List.of(ally));
        when(monsterRepository.findAllProjectedBy()).thenReturn(List.of(monster));

        ModelAndView mv = startController.Index(new ModelAndView(), session);

        assertThat(mv.getModel().get("allyList")).isEqualTo(List.of(ally));
        assertThat(mv.getModel().get("enemyList")).isEqualTo(List.of(monster));
    }

    @Test
    void Index_セッションが無効化される() {
        when(allyRepository.findAllProjectedBy()).thenReturn(List.of());
        when(monsterRepository.findAllProjectedBy()).thenReturn(List.of());
        session.setAttribute("dummy", "value");

        startController.Index(new ModelAndView(), session);

        assertThat(session.isInvalid()).isTrue();
    }

    // --- battle() のテスト --------------------------------------------------

    @Test
    void battle_ビュー名にbattleが設定される() {
        BattleStartRequest request = new BattleStartRequest(1, null, null, null, 101, null, null, null);
        stubBattleBuild();

        ModelAndView mv = startController.battle(request, new ModelAndView(), session);

        assertThat(mv.getViewName()).isEqualTo("battle");
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void battle_リクエストの味方IDがCreateCharacterSetへ渡される() {
        BattleStartRequest request = new BattleStartRequest(1, 2, null, 4, 101, null, null, null);
        stubBattleBuild();

        startController.battle(request, new ModelAndView(), session);

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(createCharacterSet).createPartySet(captor.capture());
        assertThat(captor.getValue()).containsExactly(1, 2, 4); // null は除外される
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void battle_リクエストの敵IDがCreateCharacterSetへ渡される() {
        BattleStartRequest request = new BattleStartRequest(1, null, null, null, 101, 102, null, 104);
        stubBattleBuild();

        startController.battle(request, new ModelAndView(), session);

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(createCharacterSet).createEnemySet(captor.capture());
        assertThat(captor.getValue()).containsExactly(101, 102, 104); // null は除外される
    }

    @Test
    void battle_BattleRecordとBattleStateがセッションへ格納される() {
        BattleStartRequest request = new BattleStartRequest(1, null, null, null, 101, null, null, null);
        BattleRecord record = stubBattleBuild().record();
        BattleState state = stubBattleBuild().state();
        // stubBattleBuild() の2回呼び出しで戻り値を固定するため再設定
        when(battleManagementService.createBattleRecord(any(), any(), anyList())).thenReturn(record);
        when(battleManagementService.createBattleState(any(), anyList())).thenReturn(state);

        startController.battle(request, new ModelAndView(), session);

        assertThat(session.getAttribute("battleRecord")).isSameAs(record);
        assertThat(session.getAttribute("battleState")).isSameAs(state);
    }

    @Test
    void battle_BattleManagementServiceへPartyBuildResultとEnemyBuildResultの中身が渡される() {
        BattleStartRequest request = new BattleStartRequest(1, null, null, null, 101, null, null, null);
        Stubs stubs = stubBattleBuild();

        startController.battle(request, new ModelAndView(), session);

        verify(battleManagementService).createBattleRecord(
                stubs.partyResult().partySet(),
                stubs.enemyResult().monsterDataSet(),
                stubs.partyResult().nameList());
        verify(battleManagementService).createBattleState(
                stubs.record(),
                stubs.enemyResult().nameListEnemy());
    }

    // --- ヘルパー -----------------------------------------------------------

    private record Stubs(PartyBuildResult partyResult,
                         EnemyBuildResult enemyResult,
                         BattleRecord record,
                         BattleState state) {}

    private Stubs stubBattleBuild() {
        PartyBuildResult partyResult = new PartyBuildResult(Set.of(), List.of("勇者"));
        EnemyBuildResult enemyResult = new EnemyBuildResult(Set.of(), List.of("スライム"));
        BattleRecord record = new BattleRecord(Map.<Integer, AllyData>of(),
                                               Map.<Integer, MonsterData>of(),
                                               List.of("勇者"));
        BattleState state = new BattleState();

        when(createCharacterSet.createPartySet(anyList())).thenReturn(partyResult);
        when(createCharacterSet.createEnemySet(anyList())).thenReturn(enemyResult);
        when(battleManagementService.createBattleRecord(any(), any(), anyList())).thenReturn(record);
        when(battleManagementService.createBattleState(any(), anyList())).thenReturn(state);

        return new Stubs(partyResult, enemyResult, record, state);
    }

    private AllySummary stubAlly(Integer id, String name) {
        return new AllySummary() {
            @Override public Integer getId()   { return id; }
            @Override public String  getName() { return name; }
        };
    }

    private MonsterSummary stubMonster(Integer id, String name) {
        return new MonsterSummary() {
            @Override public Integer getId()   { return id; }
            @Override public String  getName() { return name; }
        };
    }
}
