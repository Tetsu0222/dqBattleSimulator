package com.example.rpg2.action.skill;

import java.util.Random;

import com.example.rpg2.action.TaregetEnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.process.Awakening;
import com.example.rpg2.process.Funeral;
import com.example.rpg2.process.IsDefense;
import com.example.rpg2.status.Sleep;

import lombok.Data;

@Data
public class SkillAttack implements TaregetEnemyAction{
	
	private MonsterData monsterData;
	private Integer damage;
	private String stratMessage;
	private String damageMessage;
	private String  notEnoughMpMessage;
	private String resultMessage;
	private Integer target;
	private AllyData allyData;
	private Skill skill;
	
	
	public SkillAttack( AllyData allyData , Skill skill ) {
		this.allyData = allyData;
		this.skill    = skill;
		this.stratMessage =  allyData.getName() + "は" + skill.getName() + "を放った!!";
	}
	
	
	//MP判定
	public boolean isNotEnoughMp() {
		boolean check = skill.getMp() <= allyData.getCurrentMp();//
		
		if( !check ) {
			this.notEnoughMpMessage = "しかしMPが足りない･･･" ;
		}
		
		return !check;
	}
	
	
	//特技の処理
	@Override
	public MonsterData action( MonsterData monsterData  ) {
		
		Random random = new Random();
		
		//状態異常の処理
		if( !skill.getBuffcategory().equals( "no" )) {
			
			//状態異常を生成
			TaregetEnemyAction deBuffSkill = new DeBuffSkill( allyData , skill  );
			monsterData = deBuffSkill.action( monsterData );
			
			//結果を取得
			this.resultMessage = deBuffSkill.getResultMessage();
			
		}
		
		//ダメージの処理(魔法系）
		if( skill.getPoint() != 0 ) {
			
			//魔法威力 + 乱数 = ダメージ
			Integer damage = skill.getPoint() + ( random.nextInt( skill.getPoint() ) / 4 ) - random.nextInt( skill.getPoint() / 4 );
			
			//防御状態チェック
			if( IsDefense.isDefense( monsterData )){
				this.damage = damage / 2;
			}
			
			//ダメージがなければ処理中断
			if( damage < 0 ) {
				damage = 0;
				return monsterData;
			}
			
			
			Integer HP = monsterData.getCurrentHp() - damage;
			this.damageMessage = monsterData.getName() + "に" + damage + "のダメージを与えた!!";
			
			//対象が戦闘不能の場合の処理
			if( HP <= 0 ) {
				monsterData = Funeral.execution( monsterData );
				this.resultMessage = monsterData.getName() + "を倒した!!";
			
			//対象が生存している場合の処理
			}else{
				monsterData.setCurrentHp( HP );
			}
			
		//物理系
		}else{
			
			//会心の発生率設定
			int critical = random.nextInt( 255 ); //引数をキャラクターの会心率に応じて引き算し、会心発生率を上昇させる予定
			
			double damageSource = 0;
			
			//非会心
			if( critical > 6 ) {
				//(攻撃力 * スキル補正 - (防御力 / 2) ) + 乱数 = ダメージ
				damageSource = ( allyData.getCurrentATK() * skill.getPercentage() - ( monsterData.getCurrentDEF() / 2 )) 
						+ ( random.nextInt( allyData.getCurrentATK() ) / 2 );
				
			//会心
			}else{
				//( 攻撃力 * 2 * スキル補正) + 乱数 = ダメージ
				damageSource = allyData.getCurrentATK() * 2 * skill.getPercentage() 
						+ ( random.nextInt( allyData.getCurrentATK() ) / 2 );
			}
			
			//ダメージをキャスト
			this.damage = (int)damageSource;
			
			//防御状態チェック
			if( IsDefense.isDefense( monsterData )){
				this.damage = damage / 2;
			}
			
			//ダメージがなければ処理中断
			if( damage < 0 ) {
				damage = 0;
				return monsterData;
			}
			
			Integer HP = monsterData.getCurrentHp() - damage;
			
			//会心の有無でメッセージを変更
			if( critical > 6 ) {
				this.damageMessage = monsterData.getName() + "に" +damage + "のダメージを与えた!!";
				
			}else{
				this.damageMessage = "会心の一撃!!!" + monsterData.getName() + "に" + damage + "のダメージを与えた!!";
			}
			
			//討伐判定
			if( HP < 0 ) {
				monsterData = Funeral.execution( monsterData );
				this.resultMessage =  monsterData.getName() + "を倒した!!";
				
			}else{
				
				Sleep sleep = new Sleep();
				
				//対象が睡眠状態の場合は、それを解除する。
				if( monsterData.getStatusSet().contains( sleep )) {
					
					//メッセージを追加
					this.resultMessage = monsterData.getName() + "は目を覚ました!";
					
					//睡眠解除
					monsterData = Awakening.awakening( monsterData , sleep );
				}
				
				monsterData.setCurrentHp( HP );
			}
		}
		
		return monsterData;
	}


	@Override
	public void setResultMessage() {
		this.resultMessage = null;
	}

}
