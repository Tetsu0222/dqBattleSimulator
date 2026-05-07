package com.example.rpg2.action;

import java.util.Random;

import com.example.rpg2.action.magic.RecoveryMagic;
import com.example.rpg2.action.magic.ResuscitationMagic;
import com.example.rpg2.action.skill.RecoverySkill;
import com.example.rpg2.action.skill.ResuscitationSkill;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

public class SortingRecoveryAction {
	
	public static int actions;
	public static boolean targetRandom;
	public static boolean isResuscitation;
	
	
	//回復・補助開始前の生成
	public static TargetAllyAction sortingCreateRecoveryAction( AllyData allyData , Magic magic , Skill skill ) {
		
		TargetAllyAction targetAllyAction = null;
		
		//回数
		actions = 1;
		
		Random random = new Random();
		
		//魔法を生成
		if( magic != null) {
			
			//回復魔法は非無差別ターゲットで固定
			targetRandom = false;
			
			//魔法毎に設定されている発動回数を取得
			actions = magic.getFrequency();
			
			//発動回数ランダム
			if( actions == 0 ) {
				actions = random.nextInt( 6 ) + 1;
			}
			
			//蘇生魔法の生成
			if( magic.getCategory().equals( "resuscitationmagic" )) {
				targetAllyAction = new ResuscitationMagic( allyData , magic );
				isResuscitation = true;
				
			}else{
				//回復治癒魔法の生成
				targetAllyAction = new RecoveryMagic( allyData , magic );
				isResuscitation = false;
			}
		
		//特技を生成
		}else{
			
			//無差別ターゲットか確認
			targetRandom = skill.getTarget().equals( "random" );
			
			//特技毎に設定されている発動回数を所得
			actions = skill.getFrequency();
			
			//発動回数ランダム
			if( actions == 0 ) {
				actions = random.nextInt( skill.getMaxfrequency() ) + 1;
			}
			
			//蘇生魔法の生成
			if( skill.getCategory().equals( "resuscitationskill" )) {
				targetAllyAction = new ResuscitationSkill( allyData , skill );
				isResuscitation = true;
				
			}else{
				//回復特技を生成
				targetAllyAction = new RecoverySkill( allyData , skill );
				isResuscitation = false;
			}
		}
		
		return targetAllyAction;
	}
}
