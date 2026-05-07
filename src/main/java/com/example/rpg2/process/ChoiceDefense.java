package com.example.rpg2.process;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.status.Defense;
import com.example.rpg2.status.Status;

public class ChoiceDefense {
	
	public static AllyData choiceDefense( AllyData allyData ) {
		
		Set<Status> statusSet = allyData.getStatusSet();
		
		//行動不能系のスキルがなければ防御処理
		if( IsStartSkillStop.isStartSkillStop( allyData )) {
			statusSet.stream()
					.filter( s -> !s.getName().equals( "正常" ) )
					.collect( Collectors.toSet() );
			
			statusSet.add( new Defense( allyData ) );
			allyData.setStatusSet( statusSet );
		}
		
		return allyData;
	}
}
