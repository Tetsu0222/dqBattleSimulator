package com.example.rpg2.action;

import java.util.Random;

import com.example.rpg2.action.magic.MagicAttack;
import com.example.rpg2.action.skill.SkillAttack;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;


public class SortingAttackAction {
	
	
	public static int actions;
	public static boolean targetRandom;
	
	
	//攻撃開始前の生成
	public static TaregetEnemyAction sortingCreateAttackAction( AllyData allyData , Magic magic , Skill skill ) {
		
		TaregetEnemyAction taregetEnemyAction = null;
		
		//攻撃回数
		actions = 1;
		
		Random random = new Random();
		
		//魔法攻撃を生成
		if( magic != null) {
			
			//無差別ターゲット攻撃か確認(無差別攻撃は未実装)
			targetRandom = false;
			
			//発動回数を取得
			actions = magic.getFrequency();
			
			//発動回数はランダム
			if( actions == 0 ) {
				actions = random.nextInt( 6 ) + 1;
			}
			
			//攻撃魔法を生成
			taregetEnemyAction = new MagicAttack( allyData , magic );
		
		//特技を生成
		}else{
			
			//無差別ターゲット攻撃か確認
			targetRandom = skill.getTarget().equals( "random" );
			
			//特技毎の発動回数を取得
			actions = skill.getFrequency();
			
			//発動回数はランダム
			if( actions == 0 ) {
				actions = random.nextInt( skill.getMaxfrequency() ) + 1;
			}
			
			//特技を生成
			taregetEnemyAction = new SkillAttack( allyData , skill );
		}
		
		return taregetEnemyAction;
	}
	
	
	//攻撃中のアクションオブジェクト再生成(対象撃破時の自動ターゲット変更に対応するため)
	public static TaregetEnemyAction sortingRegenerationAttackAction( AllyData allyData , Magic magic , Skill skill ) {
		
		TaregetEnemyAction taregetEnemyAction = null;
		
		//魔法攻撃を生成
		if( magic != null) {
			taregetEnemyAction = new MagicAttack( allyData , magic );
		
		//特技を生成
		}else{
			taregetEnemyAction = new SkillAttack( allyData , skill );
		}
		
		return taregetEnemyAction;
	}

}
