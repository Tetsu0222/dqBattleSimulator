package com.example.rpg2.process;

import java.util.List;
import java.util.stream.Collectors;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class BadStatusBefore {
	
	private String message;
	
	//味方オブジェクトの処理
	public Integer execution( AllyData allyData ) {
		
		//行動不能系のステータス異常の数を抽出（リストサイズが1以上なら行動ができない）
		List<Status> statusList = allyData.getStatusSet().stream()
		.filter( s -> s.actionStatusBefore() == 1 )
		.collect( Collectors.toList() );
		
		//状態異常のメッセージ
		allyData.getStatusSet().stream()
		.filter( s -> !s.statusMessageBefore().equals( "no" ))
		.map( s -> s.statusMessageBefore() )
		.forEach( s -> message = s );
		
		return statusList.size();
	}
	
	
	//敵オブジェクトの処理
	public Integer execution( MonsterData monsterData ) {
		
		//行動不能系のステータス異常の数を抽出（リストサイズが1以上なら行動ができない）
		List<Status> statusList = monsterData.getStatusSet().stream()
		.filter( s -> s.actionStatusBefore() == 1 )
		.collect( Collectors.toList() );
		
		//状態異常のメッセージ
		monsterData.getStatusSet().stream()
		.filter( s -> !s.statusMessageBefore().equals( "no" ))
		.map( s -> s.statusMessageBefore() )
		.forEach( s -> message = s );
		
		return statusList.size();
	}
	
}
