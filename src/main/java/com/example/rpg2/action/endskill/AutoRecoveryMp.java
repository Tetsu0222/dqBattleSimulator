package com.example.rpg2.action.endskill;

import java.util.Random;

import com.example.rpg2.battle.AllyData;

public class AutoRecoveryMp implements EndSkill{
	
	public AllyData action( AllyData allyData ) {
		
		Random random = new Random();
		
		//回復量を設定
		int recovery = allyData.getMaxMP() / 30 + random.nextInt( 10 ) - random.nextInt( 10 );
		int MP		 = allyData.getCurrentMp();
		
		//自動回復がダメージとならないように分岐
		if( recovery < 0 ) {
			return allyData;
		}
			
		//回復処理
		MP += recovery;
		allyData.setEndSkillMessage( allyData.getName() +  "は魔力が回復した!!" );
		
		//最大値を超えないように修正
		if( allyData.getMaxMP() < MP ) {
			MP = allyData.getMaxMP();
		}
		
		allyData.setCurrentMp( MP );
		
		return allyData;
	}

}
