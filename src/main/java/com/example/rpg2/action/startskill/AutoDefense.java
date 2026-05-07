package com.example.rpg2.action.startskill;

import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.process.ChoiceDefense;

public class AutoDefense implements StartSkill{
	
	
	public AllyData action( AllyData allyData ) {
		
		Random random = new Random();
		
		int juds = random.nextInt( 2 );
		
		if( juds > 0 ) {
			allyData = ChoiceDefense.choiceDefense( allyData );
			allyData.setStartSkillMessage( allyData.getName() +  "はオートガードを発動!" );
		}
		
		return allyData;
	}

}
