package com.example.rpg2.action.endskill;

import java.util.Random;

import com.example.rpg2.battle.AllyData;

public class AutoRecovery implements EndSkill{
	
	public AllyData action( AllyData allyData ) {
		
		Random random = new Random();
		
		//回復量を設定
		int recovery = allyData.getMaxHP() / 10 + random.nextInt( 10 ) - random.nextInt( 10 );
		int HP		 = allyData.getCurrentHp();
		
		//自動回復がダメージとならないように分岐
		if( recovery < 0 ) {
			return allyData;
		}
		
		//回復処理
		HP += recovery;
		allyData.setEndSkillMessage( allyData.getName() +  "はキズが回復した!!" );
		
		//最大値を超えないように修正
		if( allyData.getMaxHP() < HP ) {
			HP = allyData.getMaxHP();
		}
		
		allyData.setCurrentHp( HP );
		
		return allyData;
	}

}
