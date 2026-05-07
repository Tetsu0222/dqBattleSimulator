package com.example.rpg2.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Status;

import lombok.Data;


@Data
public class BadStatusAfter {
	
	private String message;
	private String recoveryMessage;
	private String resultMessage;
	private String dedMessage;
	
	private Set<Integer> targetSetAlly;
	private Map<Integer,Target> targetMap;
	private Set<Integer> targetSetEnemy;
	
	
	//コンストラクタ
	public BadStatusAfter( Set<Integer> targetSetAlly , Map<Integer,Target> targetMap , Set<Integer> targetSetEnemy ) {
		this.targetSetAlly = targetSetAlly;
		this.targetMap     = targetMap;
		this.targetSetEnemy = targetSetEnemy;
	}

	
	//味方側の処理
	public Map<Integer,AllyData> execution( Map<Integer,AllyData> partyMap , AllyData allyData , Integer key ) {
		
		//ダメージ系の状態異常の処理
		List<Integer> damageList = new ArrayList<>();
		allyData.getStatusSet().stream()
		.forEach( s -> damageList.add( s.actionStatusAfter() ));
		
		//状態異常のメッセージ
		allyData.getStatusSet().stream()
		.filter( s -> !s.statusMessageAfter().equals( "no" ) )
		.map( s -> s.statusMessageAfter() )
		.forEach( s -> message = s );
		
		//自然治癒判定
		Set<Status> statusSet = allyData.getStatusSet().stream()
		.filter( s -> s.countDown() > 0 )
		.collect( Collectors.toSet() );
		
		//自然治癒メッセージをセット
		allyData.getStatusSet().stream()
		.filter( s -> s.getCount() == 0 )
		.filter( s -> !s.recoverymessage().equals( "no" ) )
		.map( s -> s.recoverymessage() )
		.forEach( s -> recoveryMessage = s );
		
		//状態異常がすべて完治した場合は、正常状態へ戻す。
		if( statusSet.size() == 0 ) {
			statusSet.add( new Normal() );
		}
		
		//聖なる守りの効果を元に戻す。
		if( statusSet.stream().filter( s -> s.getName().equals( "聖なる守り" )).count() == 0 ) {
			allyData.setSurvival( 1 );
		}
		
		//状態異常の処理結果を格納
		allyData.setStatusSet( statusSet );
		
		//状態異常によるダメージを累計
		Integer result = damageList.stream().collect( Collectors.summingInt( s -> s ) );
		
		//ダメージ計算処理
		Integer HP = allyData.getCurrentHp() - result;
		
		//結果を反映
		Funeral funeral = new Funeral( targetSetAlly , targetMap , targetSetEnemy );
		allyData = funeral.execution( allyData , HP , key );
		
		if( result > 0 ) {
			this.resultMessage = allyData.getName() + "は状態異常により" + result + "のダメージを受けた!!";
		}
		
		//状態異常ダメージで死亡した場合は、そのメッセージを追加
		if( funeral.getResultMessage() != null ) {
			this.dedMessage = funeral.getResultMessage();
		}
		
		this.targetSetAlly = funeral.getTargetSetAlly();
		this.targetMap     = funeral.getTargetMap();
		
		//結果を格納
		partyMap.put( key , allyData );
		
		return partyMap;
	}
	
	
	//敵側の処理
	public Map<Integer,MonsterData> execution( Map<Integer,MonsterData> monsterDataMap , MonsterData monsterData , Integer key ) {
		
		//ダメージ系の状態異常の処理
		List<Integer> damageList = new ArrayList<>();
		monsterData.getStatusSet().stream()
		.forEach( s -> damageList.add( s.actionStatusAfter() ));
		
		//状態異常のメッセージ
		monsterData.getStatusSet().stream()
		.filter( s -> !s.statusMessageAfter().equals( "no" ) )
		.map( s -> s.statusMessageAfter() )
		.forEach( s -> message = s );
		
		//自然治癒判定
		Set<Status> statusSet = monsterData.getStatusSet().stream()
		.filter( s -> s.countDown() > 0 )
		.collect( Collectors.toSet() );
		
		//自然治癒メッセージをセット
		monsterData.getStatusSet().stream()
		.filter( s -> s.getCount() == 0 )
		.filter( s -> !s.recoverymessage().equals( "no" ) )
		.map( s -> s.recoverymessage() )
		.forEach( s -> recoveryMessage = s );
		
		//状態異常がすべて完治した場合は、正常状態へ戻す。
		if( statusSet.size() == 0 ) {
			statusSet.add( new Normal() );
		}
		
		//聖なる守りの効果を元に戻す。
		if( statusSet.stream().filter( s -> s.getName().equals( "聖なる守り" )).count() == 0 ) {
			monsterData.setSurvival( 1 );
		}
		
		//状態異常の処理結果を格納
		monsterData.setStatusSet( statusSet );
		
		
		//状態異常によるダメージを累計
		Integer result = damageList.stream().collect( Collectors.summingInt( s -> s ) );
		
		//ダメージ計算処理
		Integer HP = monsterData.getCurrentHp() - result;
		
		//結果を反映
		Funeral funeral = new Funeral( targetSetAlly , targetMap , targetSetEnemy );
		monsterData = funeral.execution( monsterData , HP , key );
		
		if( result > 0 ) {
			this.resultMessage = monsterData.getName() + "は状態異常により" + result + "のダメージを受けた!!";
		}
		
		//状態異常ダメージで死亡した場合は、そのメッセージを追加
		if( funeral.getResultMessage() != null ) {
			this.dedMessage = funeral.getResultMessage();
		}
		
		this.targetSetEnemy = funeral.getTargetSetEnemy();

		//結果を格納
		monsterDataMap.put( key , monsterData );
		
		return monsterDataMap;
	}
	
	
}
