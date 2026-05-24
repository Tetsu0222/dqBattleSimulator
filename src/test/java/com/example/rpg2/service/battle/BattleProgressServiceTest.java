package com.example.rpg2.service.battle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.domain.Target;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.status.Confusion;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BattleProgressServiceTest {

    @Mock private BattleAttackService   battleAttackService;
    @Mock private BattleRecoveryService battleRecoveryService;
    @Mock private BattleStatusService   battleStatusService;
    @Mock private BattleEnemyService    battleEnemyService;

    @InjectMocks
    private BattleProgressService battleProgressService;

    private BattleRecord battleRecord;
    private BattleState  battleState;
    private Map<Integer, AllyData>     partyMap;
    private Map<Integer, MonsterData>  monsterDataMap;
    private Map<Integer, Target>       targetMap;

    @BeforeEach
    void setUp() {
        partyMap       = new HashMap<>();
        monsterDataMap = new HashMap<>();
        targetMap      = new HashMap<>();

        battleRecord = new BattleRecord(partyMap, monsterDataMap, new ArrayList<>());
        battleState  = new BattleState();
        battleState.setTargetMap(targetMap);
        battleState.setTargetSetAlly(new TreeSet<>());
        battleState.setTargetSetEnemy(new TreeSet<>());
    }

    //------------------------------------------------------
    //味方を生成して partyMap に登録するヘルパー
    //AllyData はリポジトリ依存のコンストラクタしか持たないため Mockito で生成
    //------------------------------------------------------
    private AllyData mockAlly(int allyId, int survival, Set<Status> statusSet) {
        AllyData ally = org.mockito.Mockito.mock(AllyData.class);
        lenient().when(ally.getAllyId()).thenReturn(allyId);
        lenient().when(ally.getName()).thenReturn("味方" + allyId);
        lenient().when(ally.getSurvival()).thenReturn(survival);
        lenient().when(ally.getStatusSet()).thenReturn(statusSet);
        lenient().when(ally.getCurrentMp()).thenReturn(10);
        return ally;
    }

    private MonsterData mockMonster(int enemyId, int survival) {
        MonsterData m = org.mockito.Mockito.mock(MonsterData.class);
        lenient().when(m.getEnemyId()).thenReturn(enemyId);
        lenient().when(m.getName()).thenReturn("敵" + enemyId);
        lenient().when(m.getSurvival()).thenReturn(survival);
        return m;
    }

    private Target target(String category, int selectionId) {
        Target t = new Target(1);
        t.setCategory(category);
        t.setSelectionId(selectionId);
        return t;
    }


    //==========================================================
    // turn()
    //==========================================================
    @Nested
    @DisplayName("turn(): 行動順リストを BattleState に格納する")
    class TurnTest {

        @Test
        void turn_BattleStateのturnListに非nullのリストが設定される() {
            AllyData ally = mockAlly(0, 1, new HashSet<>());
            when(ally.getCurrentSPE()).thenReturn(10);
            partyMap.put(0, ally);
            battleState.getTargetSetAlly().add(0);

            battleProgressService.turn(battleRecord, battleState);

            assertThat(battleState.getTurnList()).isNotNull();
            assertThat(battleState.getTurnList()).hasSize(1);
        }
    }


    //==========================================================
    // startBattle(): 味方側の各 movementPattern
    //==========================================================
    @Nested
    @DisplayName("startBattle(): 味方の行動分岐")
    class StartBattleAllyTest {

        @Test
        void startBattle_attackパターンでnormalAttackが呼ばれる() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);

            Target t = target("attack", 99);
            Skill skill = new Skill();
            skill.setMp(0);
            Magic magic = new Magic();
            magic.setMp(0);
            t.setExecutionSkill(skill);
            t.setExecutionMagic(magic);
            targetMap.put(key, t);

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleAttackService).normalAttack(eq(99), eq(key), eq(magic), eq(skill),
                    eq(ally), eq(battleRecord), eq(battleState));
        }

        @Test
        void startBattle_targetallyパターンでmagicOrSkillRecoveryが呼ばれる() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);

            Target t = target("targetally", 2);
            targetMap.put(key, t);

            when(battleRecoveryService.magicOrSkillRecovery(any(), any(), any(),
                    any(), any(), any(), any())).thenReturn(false);

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleRecoveryService).magicOrSkillRecovery(eq(ally), any(), any(),
                    eq(2), eq(key), eq(battleRecord), eq(battleState));
        }

        @Test
        void startBattle_targetenemyパターンでmagicOrSkillAttackが呼ばれる() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);

            Target t = target("targetenemy", 5);
            targetMap.put(key, t);

            when(battleAttackService.magicOrSkillAttack(any(), any(), any(),
                    any(), any(), any(), any())).thenReturn(false);

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleAttackService).magicOrSkillAttack(eq(ally), any(), any(),
                    eq(5), eq(key), eq(battleRecord), eq(battleState));
        }

        @Test
        void startBattle_defenseパターンで防御メッセージが追加される() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);

            targetMap.put(key, target("defense", 0));

            battleProgressService.startBattle(key, battleRecord, battleState);

            assertThat(battleState.getMesageList()).contains("味方" + key + "は防御している");
        }

        @Test
        void startBattle_混乱状態の味方はconfusionが実行される() {
            int key = 1;
            Set<Status> statusSet = new HashSet<>();
            statusSet.add(new Confusion());
            AllyData ally = mockAlly(key, 1, statusSet);
            partyMap.put(key, ally);

            // 元の movementPattern は attack（混乱で上書きされることを検証）
            targetMap.put(key, target("attack", 99));

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleStatusService).confusion(ally, battleRecord, battleState);
            verify(battleAttackService, never()).normalAttack(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void startBattle_死亡している味方は行動処理がスキップされる() {
            int key = 1;
            AllyData ally = mockAlly(key, 0, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);
            targetMap.put(key, target("attack", 99));

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleAttackService, never()).normalAttack(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void startBattle_行動終了後にbadStatusAfterが呼ばれる() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            partyMap.put(key, ally);
            targetMap.put(key, target("defense", 0));

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleStatusService).badStatusAfter(eq(ally), eq(key),
                    eq(battleRecord), eq(battleState));
        }
    }


    //==========================================================
    // startBattle(): 敵側
    //==========================================================
    @Nested
    @DisplayName("startBattle(): 敵側の処理")
    class StartBattleEnemyTest {

        @Test
        void startBattle_敵の行動でenemyActionと敵badStatusAfterが呼ばれる() {
            int key = 10;
            MonsterData monster = mockMonster(key, 1);
            monsterDataMap.put(key, monster);

            battleProgressService.startBattle(key, battleRecord, battleState);

            verify(battleEnemyService).enemyAction(eq(key), eq(battleRecord), eq(battleState));
            verify(battleStatusService).badStatusAfter(eq(monster), eq(key),
                    eq(battleRecord), eq(battleState));
        }
    }


    //==========================================================
    // startSkill() / endSkill()
    //==========================================================
    @Nested
    @DisplayName("startSkill() / endSkill(): ターン開始・終了処理")
    class StartEndSkillTest {

        @Test
        void startSkill_スタートスキルなしの場合でも例外なく完了する() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>(Set.of(new Normal())));
            // turnStartSkillSet を null にしてスキル発動分岐を回避
            when(ally.getTurnStartSkillSet()).thenReturn(null);
            partyMap.put(key, ally);
            battleState.getTargetSetAlly().add(key);

            Target t = target("attack", 0);
            t.setSkillName("通常攻撃");
            targetMap.put(key, t);

            battleProgressService.startSkill(battleRecord, battleState);
            // 例外なく終わればOK
        }

        @Test
        void startSkill_防御選択時はchoiceDefenseが反映される() {
            int key = 1;
            Set<Status> statusSet = new HashSet<>(Set.of(new Normal()));
            AllyData ally = mockAlly(key, 1, statusSet);
            when(ally.getTurnStartSkillSet()).thenReturn(null);
            partyMap.put(key, ally);
            battleState.getTargetSetAlly().add(key);

            Target t = target("defense", 0);
            t.setSkillName("防御");
            targetMap.put(key, t);

            battleProgressService.startSkill(battleRecord, battleState);

            // 防御スキルが statusSet に追加され、partyMap に再格納されている
            assertThat(partyMap.get(key)).isSameAs(ally);
        }

        @Test
        void endSkill_エンドスキルなしでもcancelDefenseが実行され例外なく完了する() {
            int key = 1;
            Set<Status> statusSet = new HashSet<>(Set.of(new Normal()));
            AllyData ally = mockAlly(key, 1, statusSet);
            when(ally.getTurnEndSkillSet()).thenReturn(null);
            partyMap.put(key, ally);
            battleState.getTargetSetAlly().add(key);

            battleProgressService.endSkill(battleRecord, battleState);

            // setStatusSet が呼ばれている（CancelDefense によるリセット）
            verify(ally, times(1)).setStatusSet(any());
        }
    }


    //==========================================================
    // turnAction(): null防御 と isPossible への委譲
    //==========================================================
    @Nested
    @DisplayName("turnAction(): null防御 と継続判定")
    class TurnActionTest {

        @Test
        void turnAction_battleRecordがnullならfalseを返す() {
            boolean result = battleProgressService.turnAction(null, battleState, 1);
            assertThat(result).isFalse();
        }

        @Test
        void turnAction_actionObjがnullならfalseを返す() {
            battleState.setTurnQueue(new LinkedList<>());
            boolean result = battleProgressService.turnAction(battleRecord, battleState, null);
            assertThat(result).isFalse();
        }

        @Test
        void turnAction_turnQueueがnullならfalseを返す() {
            battleState.setTurnQueue(null);
            boolean result = battleProgressService.turnAction(battleRecord, battleState, 1);
            assertThat(result).isFalse();
        }

        @Test
        void turnAction_生存している味方ならtrueを返しcurrentActorが設定される() {
            int key = 1;
            AllyData ally = mockAlly(key, 1, new HashSet<>());
            partyMap.put(key, ally);
            battleState.setTurnQueue(new LinkedList<>());

            boolean result = battleProgressService.turnAction(battleRecord, battleState, key);

            assertThat(result).isTrue();
            assertThat(battleState.getCurrentActor()).isEqualTo(key);
        }

        @Test
        void turnAction_生存している敵ならtrueを返しcurrentActorが設定される() {
            int key = 10;
            MonsterData monster = mockMonster(key, 1);
            monsterDataMap.put(key, monster);
            battleState.setTurnQueue(new LinkedList<>());

            boolean result = battleProgressService.turnAction(battleRecord, battleState, key);

            assertThat(result).isTrue();
            assertThat(battleState.getCurrentActor()).isEqualTo(key);
        }

        @Test
        void turnAction_死亡味方かつturnQueue空ならfalseを返す() {
            int key = 1;
            AllyData ally = mockAlly(key, 0, new HashSet<>());
            partyMap.put(key, ally);
            battleState.setTurnQueue(new LinkedList<>());

            boolean result = battleProgressService.turnAction(battleRecord, battleState, key);

            assertThat(result).isFalse();
        }

        @Test
        void turnAction_死亡味方でも次の生存対象がいればtrueを返す() {
            int deadKey  = 1;
            int aliveKey = 2;
            AllyData dead  = mockAlly(deadKey,  0, new HashSet<>());
            AllyData alive = mockAlly(aliveKey, 1, new HashSet<>());
            partyMap.put(deadKey,  dead);
            partyMap.put(aliveKey, alive);

            Queue<Integer> q = new LinkedList<>();
            q.add(aliveKey);
            battleState.setTurnQueue(q);

            boolean result = battleProgressService.turnAction(battleRecord, battleState, deadKey);

            assertThat(result).isTrue();
            assertThat(battleState.getCurrentActor()).isEqualTo(aliveKey);
        }

        @Test
        void turnAction_死亡敵でも次の生存敵がいればtrueを返す() {
            int deadKey  = 10;
            int aliveKey = 11;
            MonsterData dead  = mockMonster(deadKey,  0);
            MonsterData alive = mockMonster(aliveKey, 1);
            monsterDataMap.put(deadKey,  dead);
            monsterDataMap.put(aliveKey, alive);

            Queue<Integer> q = new LinkedList<>();
            q.add(aliveKey);
            battleState.setTurnQueue(q);

            boolean result = battleProgressService.turnAction(battleRecord, battleState, deadKey);

            assertThat(result).isTrue();
            assertThat(battleState.getCurrentActor()).isEqualTo(aliveKey);
        }

        @Test
        void turnAction_partyにもmonsterにも存在しないキーならfalseを返す() {
            int key = 999;
            battleState.setTurnQueue(new LinkedList<>());

            boolean result = battleProgressService.turnAction(battleRecord, battleState, key);

            assertThat(result).isFalse();
        }
    }
}
