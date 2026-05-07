package com.example.rpg2.action.skill;

import java.util.Random;

import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Skill;

import lombok.Data;

@Data
public class ResuscitationSkill implements TargetAllyAction{
	
	private Integer recovery;
	private String  stratMessage;
	private String  notEnoughMpMessage;
	private String  recoveryMessage;
	private AllyData allyData;
	private Skill skill;
	private String resultMessage;
	
	
	//コンストラクタ 必要な情報を設定
	public ResuscitationSkill( AllyData allyData , Skill skill ) {
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
	
	
	//特技による蘇生（内部処理が魔法と重複しているため、別クラスに委譲させて、共通化させたい)
	public AllyData action( AllyData receptionAllyData ) {
		
		//確定蘇生魔法
		if( skill.getPercentage() == 1 ) {
			int HP = receptionAllyData.getMaxHP();
			receptionAllyData.setCurrentHp( HP );
			receptionAllyData.resuscitation();
			this.recoveryMessage = receptionAllyData.getName() + "は完全に生き返った!!";
			
			return receptionAllyData;
		}
		
		//蘇生判定処理
		Random random = new Random();
		int judgement = random.nextInt( 3 );
			
		//蘇生成功
		if( judgement > 0 ) {
			int HP = receptionAllyData.getMaxHP() / 2;
			receptionAllyData.setCurrentHp( HP );
				
			//その他の蘇生処理はキャラクターデータのオブジェクト内で実行
			receptionAllyData.resuscitation();
			this.recoveryMessage = receptionAllyData.getName() + "は生き返った!!";
			
		//蘇生失敗
		}else{
			this.recoveryMessage = receptionAllyData.getName() + "は生き返らなかった･･･";
		}
		
		return receptionAllyData;
		
	}	

}
