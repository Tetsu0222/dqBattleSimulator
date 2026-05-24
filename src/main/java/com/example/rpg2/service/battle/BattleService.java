package com.example.rpg2.service.battle;

import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.example.rpg2.domain.BattleResultType;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.util.battle.TurnQueue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final BattleProgressService battleProgressService;

    public void initializeBattle(BattleRecord battleRecord , BattleState  battleState ) {

        if(battleRecord == null || battleState == null) throw new IllegalArgumentException("BattleRecord and BattleState cannot be null");

        // 前回までのログを消去
        battleState.getMesageList().clear();

        // 各キャラクターの行動順を規定
        battleProgressService.turn(battleRecord, battleState);

        // ターンの最初に発動する効果を処理
        battleProgressService.startSkill(battleRecord, battleState);

        // 各キャラクターの座標を素早さが高い順（降順）でソートしたリストを取得
        List<Entry<Integer, Integer>> turnList = battleState.getTurnList();

        // 素早さで順でソートされたリストから、各キャラクターの座標だけ抽出してキューへ格納
        // このキューを用いて具体的な戦闘処理を実施する。
        battleState.setTurnQueue(TurnQueue.getTurnQueue(turnList));
    }

    public boolean judgeTurnEnd( BattleState  battleState ) {

        if(battleState == null) throw new IllegalArgumentException("BattleState cannot be null");

        // 前回までのログを消去
        battleState.getMesageList().clear();

        // キューを取得
        Queue<Integer> turnqueue = battleState.getTurnQueue();

        if (turnqueue.peek() == null) return true;
        return false;
    }

    public boolean judgePossible(BattleRecord battleRecord , BattleState  battleState ) {

          if(battleRecord == null || battleState == null) throw new IllegalArgumentException("BattleRecord and BattleState cannot be null");

        Integer actionObj = battleState.getTurnQueue().poll();
        boolean isPossible = battleProgressService.turnAction(battleRecord, battleState, actionObj);
        return isPossible;
    }

    public void startBattleSetting(BattleRecord battleRecord , BattleState  battleState ) {

        if(battleRecord == null || battleState == null) throw new IllegalArgumentException("BattleRecord and BattleState cannot be null");

        // judgePossible で確定した行動者を参照（再度キューを消費しない）
        Integer actionObj = battleState.getCurrentActor();
        battleProgressService.startBattle(actionObj, battleRecord, battleState);
    }

    public BattleResultType judgeBattleResult(BattleState battleState) {

        if(battleState == null) throw new IllegalArgumentException("BattleState cannot be null");

        if (battleState.getTargetSetAlly().size() == 0)  return BattleResultType.LOSE;
        if (battleState.getTargetSetEnemy().size() == 0) return BattleResultType.WIN;
        return BattleResultType.CONTINUE;
    }

    public void turnEnd(BattleRecord battleRecord , BattleState  battleState ) {

        if(battleRecord == null || battleState == null) throw new IllegalArgumentException("BattleRecord and BattleState cannot be null");

        // ターン終了時に発動する処理
        battleProgressService.endSkill(battleRecord, battleState);
        battleState.setTurnCount(battleState.getTurnCount() + 1);
    }
}
