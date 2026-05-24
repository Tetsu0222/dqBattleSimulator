package com.example.rpg2.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.dto.response.AllySummary;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.dto.request.BattleStartRequest;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.domain.EnemyBuildResult;
import com.example.rpg2.dto.response.MonsterSummary;
import com.example.rpg2.domain.PartyBuildResult;
import com.example.rpg2.service.battle.CreateCharacterSet;
import com.example.rpg2.repository.AllyRepository;
import com.example.rpg2.repository.MonsterRepository;
import com.example.rpg2.service.battle.BattleManagementService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StartController {

    private final CreateCharacterSet createCharacterSet;
    private final AllyRepository allyRepository;
    private final MonsterRepository monsterRepository;
    private final BattleManagementService battleManagementService;

    // 定数
    private final String TopMenu      = "index";
    private final String BattleScreen = "battle";
    private final String BattleRecordKey = "battleRecord";
    private final String BattleStateKey  = "battleState";
    private final String PartyMember  = "allyList";
    private final String EnemyMember  = "enemyList";

    // TOP画面に対応
    @GetMapping("/")
    public ModelAndView Index(ModelAndView mv, HttpSession session) {

        mv.setViewName(TopMenu);

        // プレイアブルキャラクターとエネミーキャラクターの選択肢を提示（id と name のみ取得）
        List<AllySummary>    allyList    = allyRepository.findAllProjectedBy();
        List<MonsterSummary> monsterList = monsterRepository.findAllProjectedBy();

        mv.addObject(PartyMember, allyList);
        mv.addObject(EnemyMember, monsterList);

        session.invalidate();
        return mv;
    }

    // バトルへ遷移
    @GetMapping("/battle")
    public ModelAndView battle(BattleStartRequest request,ModelAndView mv,HttpSession session) {

        mv.setViewName(BattleScreen);

        // 選択に応じたプレイアブルキャラクターのIdを格納
        List<Integer> repositoryIdList = request.partyIds();

        // 生成プレイアブルキャラクターを格納するセットを生成
        PartyBuildResult partyBuildResult = createCharacterSet.createPartySet(repositoryIdList);

        // 選択に応じたエネミーキャラクターのIdを格納
        List<Integer> repositoryEnemyIdList = request.enemyIds();

        // 生成したエネミーキャラクターを格納するセットを生成
        EnemyBuildResult enemyResult = createCharacterSet.createEnemySet(repositoryEnemyIdList);

        // 不変な戦闘構成と可変な戦闘進行状態をそれぞれ生成
        BattleRecord battleRecord = battleManagementService.createBattleRecord(
                partyBuildResult.partySet(), enemyResult.monsterDataSet(), partyBuildResult.nameList());
        BattleState battleState = battleManagementService.createBattleState(battleRecord, enemyResult.nameListEnemy());

        // 戦闘画面用のデータをセッションスコープに保存
        session.setAttribute(BattleRecordKey, battleRecord);
        session.setAttribute(BattleStateKey,  battleState);
        return mv;
    }
}
