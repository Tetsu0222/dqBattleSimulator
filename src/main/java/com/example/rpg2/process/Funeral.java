package com.example.rpg2.process;

import java.util.Map;
import java.util.Set;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.status.Dead;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class Funeral {
	
	public String resultMessage;
	private Set<Integer> targetSetAlly;
	private Map<Integer,Target> targetMap;
	private Set<Integer> targetSetEnemy;
	
	
	//味方側の死亡処理
	public Funeral( Set<Integer> targetSetAlly , Map<Integer,Target> targetMap , Set<Integer> targetSetEnemy ) {
		this.targetSetAlly = targetSetAlly;
		this.targetMap     = targetMap;
		this.targetSetEnemy = targetSetEnemy;
	}
	
	//味方側の死亡判定と処理
	public AllyData execution( AllyData allyData , Integer HP , Integer key ) {
		
		//引数のオブジェクトが死亡している場合は、その処理を実行
		if( HP <= 0 ) {
			
			//死亡ステータスへ変更
			Set<Status> statusSet = allyData.getStatusSet();
			allyData.setCurrentHp( 0 );
			allyData.setSurvival( 0 );
			statusSet.clear();
			statusSet.add( new Dead() );
			allyData.setStatusSet( statusSet );
			
			//メッセージに追加
			resultMessage =  allyData.getName() + "は死んでしまった…";
			
			//各種座標リストを変更
			targetSetAlly.remove( key );
			targetMap.put( key , new Target( key ) );
		
		//死亡していない場合は、ダメージ計算された後のHPをそのまま設定
		}else{
			allyData.setCurrentHp( HP );
		}
		
		return allyData;
	}
	
	
	//敵側の死亡判定と処理
	public MonsterData execution( MonsterData monsterData , Integer HP , Integer key ) {
		
		//引数のオブジェクトが死亡している場合は、その処理を実行
		if( HP <= 0 ) {
			
			//死亡ステータスへ変更
			Set<Status> statusSet = monsterData.getStatusSet();
			monsterData.setCurrentHp( 0 );
			monsterData.setSurvival( 0 );
			statusSet.clear();
			statusSet.add( new Dead() );
			monsterData.setStatusSet( statusSet );
			
			//メッセージに追加
			resultMessage =  monsterData.getName() + "を倒した!!";
			
			//各種座標リストを変更
			targetSetEnemy.remove( key );
		
		//死亡していない場合は、ダメージ計算された後のHPをそのまま設定
		}else{
			monsterData.setCurrentHp( HP );
		}
		
		return monsterData;
	}
	
	
	//味方側の死亡処理
	public static AllyData execution( AllyData allyData ) {
		
			//死亡ステータスへ変更
			Set<Status> statusSet = allyData.getStatusSet();
			allyData.setCurrentHp( 0 );
			allyData.setSurvival( 0 );
			statusSet.clear();
			statusSet.add( new Dead() );
			allyData.setStatusSet( statusSet );
		
		return allyData;
	}
	
	
	//敵側の死亡処理
	public static MonsterData execution( MonsterData monsterData ) {
		
			//死亡ステータスへ変更
			Set<Status> statusSet = monsterData.getStatusSet();
			monsterData.setCurrentHp( 0 );
			monsterData.setSurvival( 0 );
			statusSet.clear();
			statusSet.add( new Dead() );
			monsterData.setStatusSet( statusSet );
		
		return monsterData;
	}
	
	

}
