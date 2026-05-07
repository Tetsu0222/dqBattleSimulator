package com.example.rpg2.process;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Status;

public class CancelDefense {
	
	
	//防御解除の処理
	public static AllyData cancelDefense( AllyData allyData ) {
		
		Set<Status> statusSet = allyData.getStatusSet()
				.stream()
				.filter( s -> !s.getName().equals( "防御" ) )
				.collect( Collectors.toSet() );
		
		//状態異常中でなければ正常状態へ戻す。
		if( statusSet.size() == 0 ) {
			statusSet.add( new Normal() );
		}
		
		allyData.setStatusSet( statusSet );
		
		return allyData;
	}

}
