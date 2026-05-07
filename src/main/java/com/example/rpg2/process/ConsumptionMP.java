package com.example.rpg2.process;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

public class ConsumptionMP {
	
	//魔法のMP消費処理
	public static AllyData consumptionMP( AllyData allyData , Magic magic , Skill skill ) {
		
		//MP消費処理
		if( magic != null ) {
			int MP = allyData.getCurrentMp();
			MP -= magic.getMp();
			allyData.setCurrentMp( MP );
			
		}else if( skill != null ) {
			int MP = allyData.getCurrentMp();
			MP -= skill.getMp();
			allyData.setCurrentMp( MP );
			
		}else{
			//処理しない。
		}
		
		return allyData;
	}
}
