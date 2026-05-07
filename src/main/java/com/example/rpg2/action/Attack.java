package com.example.rpg2.action;

import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.process.Awakening;
import com.example.rpg2.process.Funeral;
import com.example.rpg2.process.IsDefense;
import com.example.rpg2.status.Sleep;

import lombok.Data;

@Data
public class Attack implements TaregetEnemyAction{
	
	private MonsterData monsterData;
	private Integer damage;
	private String stratMessage;
	private String damageMessage;
	private String resultMessage;
	private Integer target;
	private AllyData allyData;
	
	//使用しない
	private String getNotEnoughMpMessage;
	private boolean isNotEnoughMp;
	
	
	public Attack( AllyData allyData ) {
		this.allyData = allyData;
		this.stratMessage =  allyData.getName() + "の攻撃!!!";
	}

	//通常攻撃
	@Override
	public MonsterData action( MonsterData monsterData ) {
		
		//---ダメージの事前処理---
		
		//会心の発生率設定
		Random random = new Random();
		int critical = random.nextInt( 255 ); //引数をキャラクターの会心率に応じて引き算し、会心発生率を上昇させる予定
		
		//非会心
		if( critical > 6 ) {
			//(攻撃力-防御力/2) + 乱数 = ダメージ
			this.damage = ( allyData.getCurrentATK() - ( monsterData.getCurrentDEF() / 2 )) + ( random.nextInt( allyData.getCurrentATK() ) / 2 );
			this.damageMessage = monsterData.getName() + "に" + damage + "のダメージを与えた!!";
			
		//会心
		}else{
			//(攻撃力 * 2) + 乱数 = ダメージ
			this.damage = allyData.getCurrentATK() * 2 + ( random.nextInt( allyData.getCurrentATK() ) / 2 );
			this.damageMessage = "会心の一撃!!!  " +  monsterData.getName() + "に" + damage + "のダメージを与えた!!";
		}
		
		//防御状態チェック
		if( IsDefense.isDefense( monsterData )){
			this.damage = damage / 2;
		}
		
		//ダメージを与えられない。
		if( damage < 0 ) {
			this.damage = 0;
			this.damageMessage = monsterData.getName() + "にダメージを与えられない";
			
			return monsterData;
		}
		
		//---ダメージ処理実行---
		Integer HP = monsterData.getCurrentHp() - this.damage;
		
		if( HP < 0 ) {
			monsterData = Funeral.execution( monsterData );
			this.resultMessage =  monsterData.getName() + "を倒した!!";
			
			return monsterData;
		}
		
			
		//対象が睡眠状態の場合は、それを解除する。
		if( monsterData.getStatusSet().contains( new Sleep() )) {
				
			//メッセージを追加
			this.resultMessage = monsterData.getName() + "は目を覚ました!";
				
			//睡眠解除
			monsterData = Awakening.awakening( monsterData , new Sleep() );
		}
			
		monsterData.setCurrentHp( HP );
		
		return monsterData;
	}


	@Override
	public String getNotEnoughMpMessage() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void setResultMessage() {
		// TODO 自動生成されたメソッド・スタブ
		
	}




}
