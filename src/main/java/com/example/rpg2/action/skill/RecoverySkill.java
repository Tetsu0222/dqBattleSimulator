package com.example.rpg2.action.skill;

import java.util.Random;

import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Skill;

import lombok.Data;

@Data
public class RecoverySkill implements TargetAllyAction{
	
	private Integer recovery;
	private String  stratMessage;
	private String  notEnoughMpMessage;
	private String  recoveryMessage;
	private AllyData allyData;
	private Skill skill;
	private String resultMessage;
	
	
	//コンストラクタ 必要な情報を設定
	public RecoverySkill( AllyData allyData , Skill skill ) {
		this.allyData = allyData;
		this.skill = skill;
		this.stratMessage =  allyData.getName() + "は" + skill.getName() + "を放った!!";
	}
	
	//MP判定
	public boolean isNotEnoughMp() {
		boolean check = skill.getMp() <= allyData.getCurrentMp();
		
		if( !check ) {
			this.notEnoughMpMessage = "しかしMPが足りない･･･" ;
		}
		
		return !check;
	}
	
	
	//味方への回復魔法
	public AllyData action( AllyData receptionAllyData ) {
		
		//回復効果の処理
		if( skill.getPoint() != 0 || skill.getPercentage() == 1 ) {
		
			Random random = new Random();
			
			//回復量変動タイプ
			if( skill.getPercentage() == 0 ) {
				int HP = receptionAllyData.getCurrentHp();
				this.recovery = skill.getPoint() + random.nextInt( skill.getPoint() / 4 ) - random.nextInt( skill.getPoint() / 4 );
					
				HP += recovery;
					
				if( receptionAllyData.getMaxHP() < HP ) {
					HP = receptionAllyData.getMaxHP();
				}
				receptionAllyData.setCurrentHp( HP );
				this.recoveryMessage = receptionAllyData.getName() + "は" + recovery + "のHPを回復した!";
			
			//全回復魔法
			}else if( skill.getPercentage() == 1 ){
				int HP = receptionAllyData.getMaxHP();
				receptionAllyData.setCurrentHp( HP );
				this.recoveryMessage = receptionAllyData.getName() + "は全快した!";
			}
		}
		
		//治療・バフ効果の処理
		if( !skill.getBuffcategory().equals( "no" )) {
			
			BuffSkill buffSkill = new BuffSkill( receptionAllyData , skill );
			receptionAllyData = buffSkill.action( receptionAllyData );
			this.resultMessage = buffSkill.getResultMessage();
		}
		
		return receptionAllyData;
		
	}

}
