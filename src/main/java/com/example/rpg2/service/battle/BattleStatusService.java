package com.example.rpg2.service.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.rpg2.action.ConfusionActions;
import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.util.battle.BadStatusAfter;

@Service
public class BattleStatusService {


	//味方側のダメージ系の状態異常処理（行動終了後に処理する状態異常のメソッド）
	public void badStatusAfter( AllyData allyData , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		BadStatusAfter badStatusAfter = new BadStatusAfter( battleState.getTargetSetAlly() , battleState.getTargetMap() , battleState.getTargetSetEnemy() );
		badStatusAfter.execution( battleRecord.partyMap() , allyData , key );

		//Funeral 内部で別オブジェクトに差し替わる可能性があるため、再取得して BattleState に反映
		battleState.setTargetSetAlly( badStatusAfter.getTargetSetAlly() );
		battleState.setTargetMap( badStatusAfter.getTargetMap() );

		//自然治癒メッセージを追加
		if( badStatusAfter.getRecoveryMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getRecoveryMessage() );
		}

		//状態異常のメッセージを追加
		if( badStatusAfter.getResultMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getResultMessage() );
		}

		//状態異常のダメージで死亡した場合のメッセージを追加
		if( badStatusAfter.getDedMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getDedMessage() );
		}
	}


	//敵側のダメージ系の状態異常処理（行動終了後に処理する状態異常のメソッド）
	public void badStatusAfter( MonsterData monsterData , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		BadStatusAfter badStatusAfter = new BadStatusAfter( battleState.getTargetSetAlly() , battleState.getTargetMap() , battleState.getTargetSetEnemy() );
		badStatusAfter.execution( battleRecord.monsterDataMap() , monsterData , key );

		battleState.setTargetSetEnemy( badStatusAfter.getTargetSetEnemy() );

		//自然治癒メッセージを追加
		if( badStatusAfter.getRecoveryMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getRecoveryMessage() );
		}

		//状態異常のメッセージを追加
		if( badStatusAfter.getResultMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getResultMessage() );
		}

		//状態異常のダメージで死亡した場合のメッセージを追加
		if( badStatusAfter.getDedMessage() != null ) {
			battleState.getMesageList().add( badStatusAfter.getDedMessage() );
		}
	}


	//混乱中の行動処理
	public void confusion( AllyData allyData , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData>    partyMap       = battleRecord.partyMap();
		Map<Integer,MonsterData> monsterDataMap = battleRecord.monsterDataMap();
		Set<Integer>             targetSetAlly  = battleState.getTargetSetAlly();
		Set<Integer>             targetSetEnemy = battleState.getTargetSetEnemy();

		Random random = new Random();
		int target = random.nextInt( 2 );

		//味方をターゲットとした混乱行動
		if( target == 0 ) {
			List<Integer> targetList = new ArrayList<Integer>( targetSetAlly );
			int index = random.nextInt( targetList.size() );
			AllyData targetAllyData = partyMap.get( targetList.get( index ) );
			targetAllyData = ConfusionActions.action( allyData , targetAllyData , random );

			if( targetAllyData.getCurrentHp() == 0 ) {

				//敵リストから対象を削除
				targetSetAlly.remove( targetList.get( index ) );
			}

			battleState.getMesageList().add( ConfusionActions.message );
			partyMap.put( targetList.get( index ) , targetAllyData );

		//敵をターゲットとした混乱行動
		}else{
			List<Integer> targetList = new ArrayList<Integer>( targetSetEnemy );
			int index = random.nextInt( targetList.size() );
			MonsterData monsterData = monsterDataMap.get( targetList.get( index ) );
			monsterData = ConfusionActions.action( allyData , monsterData , random );

			if( monsterData.getCurrentHp() == 0 ) {

				//敵リストから対象を削除
				targetSetEnemy.remove( targetList.get( index ) );
			}

			battleState.getMesageList().add( ConfusionActions.message );
			monsterDataMap.put( targetList.get( index ) , monsterData );
		}
	}
}
