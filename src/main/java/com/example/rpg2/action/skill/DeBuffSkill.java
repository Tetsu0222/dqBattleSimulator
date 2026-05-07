package com.example.rpg2.action.skill;

import com.example.rpg2.action.TaregetEnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.process.BadStatusEnemy;

import lombok.Data;

@Data
public class DeBuffSkill implements TaregetEnemyAction{
	
	private MonsterData monsterData;
	private Integer damage;
	private String stratMessage;
	private String damageMessage;
	private String  notEnoughMpMessage;
	private String resultMessage;
	private Integer target;
	private AllyData allyData;
	private Skill skill;
	
	
	public DeBuffSkill( AllyData allyData , Skill skill ) {
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
	
	
	//妨害魔法
	@Override
	public MonsterData action( MonsterData monsterData ) {
		
		switch( skill.getBuffcategory() ) {
		
		
		//防御力のデバフ
		case "DEF":
			
			double def = monsterData.getCurrentDEF();
			
			double buffPoint = 0.0;
			
			//下限チェック
			if( def < 1 ) {
				this.resultMessage = monsterData.getName() + "の守備力は、これ以上は下がらなかった･･･";
				return monsterData;
			}
			//下限未達
			buffPoint = skill.getPercentage() + 0.2 ;
			def = def / buffPoint;
				
			//補正値が上限を上回らないように再分岐
			if( def < 1 ) {
					def = 0 ;
			}
			
			//キャラクターへ反映
			monsterData.setCurrentDEF( (int) def );
			
			//メッセージを設定
			this.resultMessage = monsterData.getName() + "の守備力が下がった!!";
			
			break;
		
			
		//攻撃力のデバフ	
		case "ATK":
			double atk = monsterData.getCurrentATK();
			
			//下限チェック
			if( atk < 1 ) {
				this.resultMessage = monsterData.getName() + "の攻撃力は、これ以上は下がらなかった･･･";
				return monsterData;
			}
			
			//下限未達
			buffPoint = skill.getPercentage() + 1.2 ;
			atk = atk / buffPoint;
				
			//補正値が上限を上回らないように再分岐
			if( atk < 1 ) {
					atk = 0 ;
			}
			
			//キャラクターへ反映
			monsterData.setCurrentDEF( (int) atk );
			
			//メッセージを設定
			this.resultMessage = monsterData.getName() + "の攻撃力が下がった!!";
			
			break;
		
		//デバフではなく悪性状態異常を付与
		default:
			
			//悪性状態異常の処理を委譲
			BadStatusEnemy badStatusEnemy = new BadStatusEnemy();
			
			//キャラクターへ反映
			monsterData = badStatusEnemy.bad( monsterData , skill );
			
			//メッセージを設定
			this.resultMessage = badStatusEnemy.getResultMessage();
		}
		
		return monsterData;
	}


	@Override
	public void setResultMessage() {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	
}
