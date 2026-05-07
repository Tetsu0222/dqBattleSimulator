package com.example.rpg2.action.magic;

import com.example.rpg2.action.TaregetEnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.process.BadStatusEnemy;

import lombok.Data;

@Data
public class DeBuffMagic implements TaregetEnemyAction{
	
	private MonsterData monsterData;
	private Integer damage;
	private String stratMessage;
	private String damageMessage;
	private String  notEnoughMpMessage;
	private String resultMessage;
	private Integer target;
	private AllyData allyData;
	private Magic    magic;
	
	
	public DeBuffMagic( AllyData allyData , Magic magic ) {
		this.allyData = allyData;
		this.magic = magic;
		this.stratMessage =  allyData.getName() + "は" + magic.getName() + "を放った!!";
	}
	
	
	//MP判定
	public boolean isNotEnoughMp() {
		boolean check = magic.getMp() <= allyData.getCurrentMp();
		
		if( !check ) {
			this.notEnoughMpMessage = "しかしMPが足りない･･･" ;
		}
		
		return !check;
	}
	
	
	//妨害魔法
	@Override
	public MonsterData action( MonsterData monsterData ) {
		
		double buffPoint = 0.0;
		
		switch( magic.getBuffcategory() ) {
		
			//守備力のデバフ
			case "DEF" :
				
				double def = monsterData.getCurrentDEF();
				
				//下限チェック
				if( def < 1 ) {
					this.resultMessage = monsterData.getName() + "の守備力は、これ以上は下がらなかった･･･";
					break;
				}
				
				buffPoint = magic.getPercentage() + 1.2 ;
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
					break;
				}
				
				buffPoint = magic.getPercentage() + 1.2 ;
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
				
				
			//デバフではなく状態異常
			default:
				
				//悪性状態異常の処理を委譲
				BadStatusEnemy badStatusEnemy = new BadStatusEnemy();
				
				//キャラクターへ反映
				monsterData = badStatusEnemy.bad( monsterData , magic );
				
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
