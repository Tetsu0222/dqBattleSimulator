package com.example.rpg2.process;

import com.example.rpg2.battle.AllyData;

public class IsStartSkillStop {
	
	//スタートスキルを停止すべきか判定
	public static boolean isStartSkillStop( AllyData allyData ) {
		
		//行動不能系の状態異常が追加された場合、filterで判定を追加していく。
		Long juds = allyData.getStatusSet().stream()
			.filter( s -> s.getName().equals( "睡眠" ))
			.filter( s -> s.getName().equals( "気絶" ))
			.count();
		
		//行動不能系の状態異常が存在すれば、スタートスキルの発動を停止(falseで返す)
		return juds == 0;
	}
}