package com.example.rpg2.action;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.entity.MonsterPattern;
import com.example.rpg2.process.Awakening;
import com.example.rpg2.process.BadStatusAlly;
import com.example.rpg2.process.EnemyTarget;
import com.example.rpg2.process.Funeral;
import com.example.rpg2.process.IsDefense;

import lombok.Data;

@Data
public class EnemyAction {
	
	private String pattern;
	private String range;
	private MonsterPattern monsterPattern;
	private MonsterData monsterData;
	private Integer targetId;

	private Integer recovery;
	private Integer damage  = 0;
	private String  startMessage ;
	private String  battleMessage;
	private String  buffMessage;
	private String  resultMessage;
	private String  dedMessage;
	
	Random random = new Random();
	
	public void decision( MonsterData monsterData ) {
		this.monsterData = monsterData;
		
		//モンスターの行動をリストからランダムで指定
		this.monsterPattern = monsterData.getPatternList().get( random.nextInt( monsterData.getPatternList().size() ) );
		this.pattern = monsterPattern.getCategory();
		this.range   = monsterPattern.getRange();
		this.startMessage = monsterData.getName() + monsterPattern.getText();
	}
	
	
	//単体攻撃を処理
	public AllyData attackSkillSingle( Map<Integer,AllyData> partyMap , List<Integer> targetList ) {
		
		//対象をランダムに指定
		this.targetId = EnemyTarget.enemyTarget( targetList , targetList.size() );
		
		AllyData allyData = partyMap.get( targetId );
		
		//悪性ステータス異常
		if( !monsterPattern.getBuffcategory().equals( "no" ) ) {
			BadStatusAlly badStatusAlly = new BadStatusAlly();
			allyData = badStatusAlly.bad( allyData, monsterPattern );
			this.buffMessage = badStatusAlly.getBuffMessage();
		}
		
		//物理攻撃の計算処理
		if( this.pattern.equals( "attackskill" ) && monsterPattern.getPercentage() == 0 ) {
			allyData = this.physical( allyData );
			
		//魔法攻撃の計算処理
		}else if( this.pattern.equals( "attackmagic" ) && monsterPattern.getPercentage() == 0 ){
			allyData = this.magic( allyData );
		}

		return allyData;
	}
	
	
	//全体攻撃を処理
	public AllyData attackSkillWhole( Map<Integer,AllyData> partyMap , Integer target ) {
		
		this.targetId = target;
		AllyData allyData = partyMap.get( targetId );
		
		//悪性ステータス異常
		if( !monsterPattern.getBuffcategory().equals( "no" ) ) {
			BadStatusAlly badStatusAlly = new BadStatusAlly();
			allyData = badStatusAlly.bad( allyData, monsterPattern );
			this.buffMessage = badStatusAlly.getBuffMessage();
		}
		
		//物理攻撃の計算処理
		if( this.pattern.equals( "attackskill" ) && monsterPattern.getPercentage() == 0 ) {
			allyData = this.physical( allyData );
			
		//魔法攻撃の計算処理
		}else if( this.pattern.equals( "attackmagic" ) && monsterPattern.getPercentage() == 0 ){
			allyData = this.magic( allyData );
		}
		
		return allyData;
	}
	
	
	//死亡時などの処理
	public void noAction() {
		//明示的に何も処理しない。
	}
	
	
	//物理攻撃を処理
	public AllyData physical( AllyData allyData ) {
		
		Integer plusDamage = 0;
		
		if( monsterPattern.getPoint() == 0 ){
			plusDamage = ( random.nextInt( monsterData.getCurrentATK() + 1 ) / 10 ) - ( random.nextInt( monsterData.getCurrentATK() + 1 ) / 10 );
			//(攻撃力-防御力/2) + 乱数 = ダメージ
			this.damage = ( monsterData.getCurrentATK() - ( allyData.getCurrentDEF() / 2 + 1 )) + plusDamage;
			
		}else{
			plusDamage = ( random.nextInt( monsterPattern.getPoint() + 1 ) / 10 ) - ( random.nextInt( monsterPattern.getPoint() + 1 ) / 10 );
			//行動に設定された威力 + 乱数 = ダメージ
			this.damage = monsterPattern.getPoint() + plusDamage;
		}
		
		//防御状態チェック
		if( IsDefense.isDefense( allyData )){
			this.damage = damage / 2;
		}
		
		//ブレス攻撃かつ対象がフバーハかチェック、第2引数はダミー
		if( monsterPattern.getAttribute().equals( "breath" ) && IsDefense.isDefense( allyData , "ダミー" ) ) {
				this.damage = damage / 2;
		}
		
		//ダメージがゼロなら処理終了
		if( damage < 0 ) {
			this.damage = 0;
			this.battleMessage = allyData.getName() + "にダメージを与えられない…";
			
			return allyData;
		}
		
		this.battleMessage = allyData.getName() + "に" + damage + "のダメージ!!!";
			
		//対象が睡眠状態の場合は、それを解除する。
		if( allyData.getStatusSet().stream()
				.filter( s -> s.getName().equals( "睡眠" ))
				.count() == 1 ) {
			this.resultMessage = allyData.getName() + "は目を覚ました!";
			allyData = Awakening.awakening( allyData );
		}
		
		//ダメージ計算
		Integer HP = allyData.getCurrentHp() - damage;

		//攻撃で味方がやられてしまった時の処理
		if( HP <= 0 ) {
			allyData = Funeral.execution( allyData );
			this.dedMessage = allyData.getName() + "は死んでしまった…";
			
		//ダメージを反映
		}else{
			allyData.setCurrentHp( HP );
		}
		
		return allyData;
	}
	
	
	//魔法攻撃を処理
	public AllyData magic( AllyData allyData ) {
		
		Integer plusDamage = 0;
		
		if( monsterPattern.getPoint() == 0 ){
			plusDamage = 0;
			
		}else{
			plusDamage = ( random.nextInt( monsterPattern.getPoint() + 1 ) / 10 ) - ( random.nextInt( monsterPattern.getPoint() + 1 ) / 10 );
		}
		
		//攻撃力 + 乱数 = ダメージ(防御力無視だけで暫定対応、耐性値を実装して値に干渉する予定)
		this.damage = monsterPattern.getPoint() + plusDamage;
		
		//防御状態をチェック
		if( IsDefense.isDefense( allyData )){
			this.damage = damage / 2;
		}
		
		//マジックバリア状態をチェック、第2引数はダミー
		if( IsDefense.isDefense( allyData , 1 )){
			this.damage = damage / 2;
		}
		
		//ダメージがゼロなら処理終了
		if( damage < 0 ) {
			this.damage = 0;
			this.battleMessage = allyData.getName() + "にダメージを与えられない…";
			return allyData;
		}
		
		this.battleMessage = allyData.getName() + "に" + damage + "のダメージ!!!";
		
		//ダメージ計算
		Integer HP = allyData.getCurrentHp() - damage;

		//攻撃で味方がやられてしまった時の処理
		if( HP <= 0 ) {
			allyData = Funeral.execution( allyData );
			this.dedMessage = allyData.getName() + "は死んでしまった…";
		
		//ダメージを反映
		}else{
			allyData.setCurrentHp( HP );
		}
		
		return allyData;
	}
	
}
