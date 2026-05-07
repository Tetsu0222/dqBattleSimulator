package com.example.rpg2.battle.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.example.rpg2.action.EnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.process.BadStatusBefore;

import lombok.Data;

@Data
public class BattleSupportEnemy {
	
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
	private List<String> mesageList = new ArrayList<>();
	
	//グループ攻撃用のセット
	private List<String> enemyNameList;
	private List<String> allyNameList;
	
	private List<Integer> targetList;
	
	Random random = new Random();
	
	
	public BattleSupportEnemy( Battle battle ) {
		this.partyMap 		= battle.getPartyMap();
		this.monsterDataMap = battle.getMonsterDataMap();
		this.targetMap 		= battle.getTargetMap();
		this.targetSetAlly  = battle.getTargetSetAlly();
		this.targetSetEnemy = battle.getTargetSetEnemy();
		this.enemyNameList  = battle.getEnemyNameList();
		this.allyNameList 	= battle.getAllyNameList();
		this.targetList 	= new ArrayList<Integer>( targetSetAlly );
	}
	
	
	public void enemyAction( Integer key , BattleSupportStatus battleSupportStatus ) {
		
        //行動対象のモンスターのデータを生成
		MonsterData monsterData = monsterDataMap.get( key );
			
		//行動不能系の状態異常の所持数をチェック
		BadStatusBefore badStatusBefor = new BadStatusBefore();
		Integer juds = badStatusBefor.execution( monsterData );
			
		if( badStatusBefor.getMessage() != null ) {
			this.mesageList.add( badStatusBefor.getMessage() );
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
			battleSupportStatus.badStatusAfter( monsterData , key );
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
    			this.singleAttack( enemyAction );
    				
    		//全体攻撃を処理
    		}else if( enemyAction.getRange().equals( "whole" )){
    			this.wholeAttack( enemyAction );

	    	//ミス系
	    	}else if( enemyAction.getPattern().equals( "miss" )){
	    		enemyAction.noAction();
	    		mesageList.add( monsterData.getName() + "の攻撃!!" );
	    		mesageList.add( "しかし、攻撃は外れてしまった…" );
	    			
	    	//死亡時
	    	}else{
	    		enemyAction.noAction();
	    	}
		}
	}
	
	
	//敵の単体攻撃を処理するメソッド
	public void singleAttack( EnemyAction enemyAction ) {
		
		//単体攻撃を処理
		AllyData allyData = enemyAction.attackSkillSingle( partyMap , targetList );
		
		//行動開始のメッセージを追加
		this.mesageList.add( enemyAction.getStartMessage() );
		
		//ダメージがあれば表示に追加
		if( enemyAction.getBattleMessage() != null ) {
			this.mesageList.add( enemyAction.getBattleMessage() );
		}
		
		//状態異常があれば表示に追加
		if( enemyAction.getBuffMessage() != null ) {
			this.mesageList.add( enemyAction.getBuffMessage() );
		}
		
		//攻撃で味方が倒れた場合の処理の処理結果を反映
		if( allyData.getSurvival() == 0 ) {
			this.targetSetAlly.remove( enemyAction.getTargetId() );
			this.targetList.remove( enemyAction.getTargetId() );
			this.targetMap.put( enemyAction.getTargetId() , new Target( enemyAction.getTargetId() ) );
			this.partyMap.put( enemyAction.getTargetId() , allyData );
			this.mesageList.add( enemyAction.getDedMessage() );
			
		//行動の処理結果を反映
		}else{
			this.partyMap.put( enemyAction.getTargetId() , allyData );
		}
	}
	
	
	//敵の全体攻撃を処理するメソッド
	public void wholeAttack( EnemyAction enemyAction ) {
		
		//行動開始のメッセージを表示に追加
		this.mesageList.add( enemyAction.getStartMessage() );
		
		//味方全体へ処理を繰り返す。
		for( int j = 0 ; j < targetList.size() ; j++ ) {
			
			int targetId = targetList.get( j );
			AllyData allyData = enemyAction.attackSkillWhole( partyMap , targetId );
			
			//ダメージがあれば表示に追加
			if( enemyAction.getBattleMessage() != null ) {
				this.mesageList.add( enemyAction.getBattleMessage() );
			}
			
			//状態異常があれば表示に追加
			if( enemyAction.getBuffMessage() != null ) {
				this.mesageList.add( enemyAction.getBuffMessage() );
			}
			
			//攻撃結果で味方が倒れた場合の処理とその結果の格納
			if( allyData.getSurvival() == 0 ) {
				this.targetSetAlly.remove( enemyAction.getTargetId() );
				this.targetList.remove( enemyAction.getTargetId() );
				this.targetMap.put( enemyAction.getTargetId() , new Target( enemyAction.getTargetId() ) );
				this.partyMap.put( enemyAction.getTargetId() , allyData );
				if( enemyAction.getDedMessage() != null ) {
					this.mesageList.add( enemyAction.getDedMessage() );
				}
			
			//処理結果を格納
			}else{
				this.partyMap.put( enemyAction.getTargetId() , allyData );
			}
		}
	}

}
