package com.example.rpg2.battle.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.example.rpg2.action.ConfusionActions;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.process.BadStatusAfter;

import lombok.Data;

@Data
public class BattleSupportStatus {
	
	//プレイアブルメンバーを管理
	private Map<Integer,AllyData> partyMap;
	
	//エネミーメンバーを管理
	private Map<Integer,MonsterData> monsterDataMap;
	
	//プレイアブルメンバーの行動選択を管理
	private Map<Integer,Target> targetMap;
	
	//味方の数とキーを管理、キャラの特定に使用
	private Set<Integer> targetSetAlly;
	
	//敵の数とキーを管理、同上
	private Set<Integer> targetSetEnemy;
	
	//表示するログを管理
	private List<String> mesageList;
	
	//グループ攻撃用のセット
	private List<String> enemyNameList;
	private List<String> allyNameList;
	
	Random random = new Random();
	
	
	public BattleSupportStatus( Battle battle ) {
		this.partyMap = battle.getPartyMap();
		this.monsterDataMap = battle.getMonsterDataMap();
		this.targetMap = battle.getTargetMap();
		this.targetSetAlly = battle.getTargetSetAlly();
		this.targetSetEnemy = battle.getTargetSetEnemy();
		this.enemyNameList = battle.getEnemyNameList();
		this.allyNameList = battle.getAllyNameList();
	}
	
	
	//味方側のダメージ系の状態異常処理（行動終了後に処理する状態異常のメソッド）
	public void badStatusAfter( AllyData allyData , Integer key ) {
		
		this.mesageList = new ArrayList<>();
		
		BadStatusAfter badStatusAfter = new BadStatusAfter( targetSetAlly , targetMap , targetSetEnemy );
		this.partyMap = badStatusAfter.execution( partyMap , allyData , key );
		this.targetSetAlly = badStatusAfter.getTargetSetAlly();
		this.targetMap     = badStatusAfter.getTargetMap();
		
		//自然治癒メッセージを追加
		if( badStatusAfter.getRecoveryMessage() != null ) {
			this.mesageList.add( badStatusAfter.getRecoveryMessage() );
		}
		
		//状態異常のメッセージを追加
		if( badStatusAfter.getResultMessage() != null ) {
			this.mesageList.add( badStatusAfter.getResultMessage() );
		}
		
		//状態異常のダメージで死亡した場合のメッセージを追加
		if( badStatusAfter.getDedMessage() != null ) {
			this.mesageList.add( badStatusAfter.getDedMessage() );
		}
	}
	
	
	//敵側のダメージ系の状態異常処理（行動終了後に処理する状態異常のメソッド）
	public void badStatusAfter( MonsterData monsterData , Integer key ) {
		
		this.mesageList = new ArrayList<>();
		
		BadStatusAfter badStatusAfter = new BadStatusAfter( targetSetAlly , targetMap , targetSetEnemy );
		this.monsterDataMap = badStatusAfter.execution( monsterDataMap , monsterData , key );
		this.targetSetEnemy = badStatusAfter.getTargetSetEnemy();
		
		//自然治癒メッセージを追加
		if( badStatusAfter.getRecoveryMessage() != null ) {
			this.mesageList.add( badStatusAfter.getRecoveryMessage() );
		}
		
		//状態異常のメッセージを追加
		if( badStatusAfter.getResultMessage() != null ) {
			this.mesageList.add( badStatusAfter.getResultMessage() );
		}
		
		//状態異常のダメージで死亡した場合のメッセージを追加
		if( badStatusAfter.getDedMessage() != null ) {
			this.mesageList.add( badStatusAfter.getDedMessage() );
		}
		
	}
	
	
	//混乱中の行動処理
	public void confusion( AllyData allyData ) {
		
		this.mesageList = new ArrayList<>();
		
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
			
			this.mesageList.add( ConfusionActions.message );
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
			
			this.mesageList.add( ConfusionActions.message );
			monsterDataMap.put( targetList.get( index ) , monsterData );
		}
	}

}
