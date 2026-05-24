package com.example.rpg2.service.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.rpg2.action.EnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.dto.BattleRecord;
import com.example.rpg2.dto.BattleState;
import com.example.rpg2.process.BadStatusBefore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BattleEnemyService {

	private final BattleStatusService battleStatusService;


	public void enemyAction( Integer key , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,MonsterData> monsterDataMap = battleRecord.monsterDataMap();
		Set<Integer>             targetSetEnemy = battleState.getTargetSetEnemy();
		Set<Integer>             targetSetAlly  = battleState.getTargetSetAlly();

		//enemyAction の引数で使う味方座標リスト（攻撃で倒れたメンバーをここから外していく）
		List<Integer> targetList = new ArrayList<Integer>( targetSetAlly );

		//行動対象のモンスターのデータを生成
		MonsterData monsterData = monsterDataMap.get( key );

		//行動不能系の状態異常の所持数をチェック
		BadStatusBefore badStatusBefor = new BadStatusBefore();
		Integer juds = badStatusBefor.execution( monsterData );

		if( badStatusBefor.getMessage() != null ) {
			battleState.getMesageList().add( badStatusBefor.getMessage() );
		}

		//モンスターの行動回数を設定
		List<Integer> actionsList = monsterData.getActionsList();
		int actions = actionsList.get( 0 );

		//行動回数がランダムの場合の処理
		if( actionsList.size() > 1 ) {
			Random random = new Random();
			int index = random.nextInt( actionsList.size() );
			actions = actionsList.get( index );
		}

		//行動不能と判定された状態異常が1つ以上あれば処理中断
		if( juds > 0 ) {
			battleStatusService.badStatusAfter( monsterData , key , battleRecord , battleState );
			actions = 0;
		}

		//ターン中に死亡している場合は、処理を中断(カウンターなどを想定)
		if( monsterData.getSurvival() == 0 ) {
			actions = 0;
		}

		//複数行動に対応
		for( int a = 0 ; a < actions ; a++ ) {

			EnemyAction enemyAction = new EnemyAction();

			//モンスターの行動を決定
    		enemyAction.decision( monsterData );

    		//ターン中に死亡してた場合は、行動処理を上書き(カウンターや反射ダメージなどを想定）
    		if( monsterData.getSurvival() == 0 ) {
    			enemyAction.setRange( "death" );
    			enemyAction.setPattern( "death" );
    			break;
    		}

            //ターン中に敵か味方のいずれかが全滅している場合は、行動を終了させる。
            if( targetSetEnemy.size() == 0 || targetSetAlly.size() == 0 ) {
            	break;
            }

            //---スイッチ文に修正予定---
    		//単体攻撃処理
    		if( enemyAction.getRange().equals( "single" )){
    			this.singleAttack( enemyAction , targetList , battleRecord , battleState );

    		//全体攻撃を処理
    		}else if( enemyAction.getRange().equals( "whole" )){
    			this.wholeAttack( enemyAction , targetList , battleRecord , battleState );

	    	//ミス系
	    	}else if( enemyAction.getPattern().equals( "miss" )){
	    		enemyAction.noAction();
	    		battleState.getMesageList().add( monsterData.getName() + "の攻撃!!" );
	    		battleState.getMesageList().add( "しかし、攻撃は外れてしまった…" );

	    	//死亡時
	    	}else{
	    		enemyAction.noAction();
	    	}
		}
	}


	//敵の単体攻撃を処理するメソッド
	public void singleAttack( EnemyAction enemyAction , List<Integer> targetList , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData> partyMap = battleRecord.partyMap();

		//単体攻撃を処理
		AllyData allyData = enemyAction.attackSkillSingle( partyMap , targetList );

		//行動開始のメッセージを追加
		battleState.getMesageList().add( enemyAction.getStartMessage() );

		//ダメージがあれば表示に追加
		if( enemyAction.getBattleMessage() != null ) {
			battleState.getMesageList().add( enemyAction.getBattleMessage() );
		}

		//状態異常があれば表示に追加
		if( enemyAction.getBuffMessage() != null ) {
			battleState.getMesageList().add( enemyAction.getBuffMessage() );
		}

		//攻撃で味方が倒れた場合の処理の処理結果を反映
		if( allyData.getSurvival() == 0 ) {
			battleState.getTargetSetAlly().remove( enemyAction.getTargetId() );
			targetList.remove( enemyAction.getTargetId() );
			battleState.getTargetMap().put( enemyAction.getTargetId() , new Target( enemyAction.getTargetId() ) );
			partyMap.put( enemyAction.getTargetId() , allyData );
			battleState.getMesageList().add( enemyAction.getDedMessage() );

		//行動の処理結果を反映
		}else{
			partyMap.put( enemyAction.getTargetId() , allyData );
		}
	}


	//敵の全体攻撃を処理するメソッド
	public void wholeAttack( EnemyAction enemyAction , List<Integer> targetList , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData> partyMap = battleRecord.partyMap();

		//行動開始のメッセージを表示に追加
		battleState.getMesageList().add( enemyAction.getStartMessage() );

		//味方全体へ処理を繰り返す。
		for( int j = 0 ; j < targetList.size() ; j++ ) {

			int targetId = targetList.get( j );
			AllyData allyData = enemyAction.attackSkillWhole( partyMap , targetId );

			//ダメージがあれば表示に追加
			if( enemyAction.getBattleMessage() != null ) {
				battleState.getMesageList().add( enemyAction.getBattleMessage() );
			}

			//状態異常があれば表示に追加
			if( enemyAction.getBuffMessage() != null ) {
				battleState.getMesageList().add( enemyAction.getBuffMessage() );
			}

			//攻撃結果で味方が倒れた場合の処理とその結果の格納
			if( allyData.getSurvival() == 0 ) {
				battleState.getTargetSetAlly().remove( enemyAction.getTargetId() );
				targetList.remove( enemyAction.getTargetId() );
				battleState.getTargetMap().put( enemyAction.getTargetId() , new Target( enemyAction.getTargetId() ) );
				partyMap.put( enemyAction.getTargetId() , allyData );
				if( enemyAction.getDedMessage() != null ) {
					battleState.getMesageList().add( enemyAction.getDedMessage() );
				}

			//処理結果を格納
			}else{
				partyMap.put( enemyAction.getTargetId() , allyData );
			}
		}
	}
}
