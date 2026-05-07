package com.example.rpg2.action.magic;

import java.util.Random;

import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Magic;

import lombok.Data;


@Data
public class RecoveryMagic implements TargetAllyAction {
	
	private Integer recovery;
	private String  stratMessage;
	private String  notEnoughMpMessage;
	private String  recoveryMessage;
	private AllyData allyData;
	private Magic    magic;
	private String resultMessage;
	
	
	//コンストラクタ 必要な情報を設定
	public RecoveryMagic( AllyData allyData , Magic magic ) {
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
	
	
	
	//味方への回復と補助魔法の分岐と処理（回復と補助の複合かも判定）
	public AllyData action( AllyData receptionAllyData ) {
		
		
		//治療・バフ効果の処理
		if( !magic.getBuffcategory().equals( "no" )) {
			BuffMagic buffMagic = new BuffMagic( receptionAllyData , magic );
			receptionAllyData = buffMagic.action( receptionAllyData );
			this.resultMessage = buffMagic.getResultMessage();
		}
		
		//回復効果の処理
		if( magic.getPoint() != 0 || magic.getPercentage() == 1 ) {
		
			Random random = new Random();
			
			//回復量変動タイプ
			if( magic.getPercentage() == 0 ) {
				int HP = receptionAllyData.getCurrentHp();
				this.recovery = magic.getPoint() + random.nextInt( magic.getPoint() / 4 ) - random.nextInt( magic.getPoint() / 4 );
					
				HP += recovery;
					
				if( receptionAllyData.getMaxHP() < HP ) {
					HP = receptionAllyData.getMaxHP();
				}
				
				receptionAllyData.setCurrentHp( HP );
				this.recoveryMessage = receptionAllyData.getName() + "は" + recovery + "のHPを回復した!";
			
			//全回復魔法
			}else if( magic.getPercentage() == 1 ){
				int HP = receptionAllyData.getMaxHP();
				receptionAllyData.setCurrentHp( HP );
				this.recoveryMessage = receptionAllyData.getName() + "は全快した!";
			}
		}
		
		return receptionAllyData;
		
	}
	
}
