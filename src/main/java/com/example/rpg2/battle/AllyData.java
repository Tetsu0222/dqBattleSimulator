package com.example.rpg2.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.rpg2.entity.Ally;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.repository.MagicRepository;
import com.example.rpg2.repository.SkillRepository;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class AllyData {
	
	private Integer allyId;
	private String  name;
	private Integer maxHP;
	private Integer maxMP;
	private Integer defaultATK;
	private Integer defaultDEF;
	private Integer defaultSPE;
	
	private Integer currentHp;
	private Integer currentMp;
	private Integer currentATK;
	private Integer currentDEF;
	private Integer currentSPE;
	private String  magic;
	private String  skill;
	
	private int survival;
	private Integer resistance;
	
	private String startSkillMessage;
	private String endSkillMessage;
	
	//使用可能な魔法を格納
	List<Magic> magicList = new ArrayList<>();
	
	//使用可能な特技を格納
	List<Skill> skillList = new ArrayList<>();
	
	//状態異常を管理
	Set<Status> statusSet = new HashSet<>();
	
	//ターン開始時に発動するスキルを管理
	Set<String> turnStartSkillSet = new HashSet<>();
	
	//ターン開始時に発動するスキルを管理
	Set<String> turnEndSkillSet = new HashSet<>();
	
	
	public AllyData( Ally ally , MagicRepository magicRepository , SkillRepository skillRepository , Integer allyId ) {

		this.name = ally.getName();
		
		//SQL上のIDとは別
		this.allyId = allyId;
		
		//固定ステータスの設定
		this.maxHP = ally.getHp();
		this.maxMP = ally.getMp();
		this.defaultATK = ally.getAtk();
		this.defaultDEF = ally.getDef();
		this.defaultSPE = ally.getSpe();
		this.resistance = ally.getResistance();
		
		//変動ステータスの設定
		this.currentHp  = ally.getHp();
		this.currentMp  = ally.getMp();
		this.currentATK = ally.getAtk();
		this.currentDEF = ally.getDef();
		this.currentSPE = ally.getSpe();
		
		
		//使用可能な魔法の設定
		this.magic = ally.getMagic() == null ? "26" : ally.getMagic();
		
		//元の魔法そのものの削除などによる例外対策用のダミーオブジェクト
		Magic dummyMagic = magicRepository.findById( 26 ).orElseThrow();
		
		String[] magicSource = magic.split( "," );
		List<String> sourceList = Arrays.asList( magicSource );
		sourceList.stream()
		.map( s -> Integer.parseInt( s ) )
		.map( s ->  magicRepository.findById( s ) )
		.forEach( s -> magicList.add( s.orElse( dummyMagic ) ));
		
		//使用可能な特技の設定
		this.skill = ally.getSkill() == null ? "2" : ally.getSkill();
		
		//元のスキルそのものの削除などによる例外対策用のダミーオブジェクト
		Skill dummySkill = skillRepository.findById( 2 ).orElseThrow();
		
		String[] skillSource = skill.split( "," );
		List<String> sourceSkillList = Arrays.asList( skillSource );
		sourceSkillList.stream()
		.map( s -> Integer.parseInt( s ) )
		.map( s ->  skillRepository.findById( s ) )
		.forEach( s -> skillList.add( s.orElse( dummySkill ) ));
		
		//生存設定
		this.survival = 1;
		statusSet.add( new Normal() );
		
		
		//スタートスキルの設定
		String[] startSource = ally.getTurnstartskill().split( "," );
		Collections.addAll( turnStartSkillSet , startSource );
		if( turnStartSkillSet.contains( "なし" )) {
			turnStartSkillSet = null;
		}

		//エンドスキルの設定
		String[] endSource = ally.getTurnendskill().split( "," );
		Collections.addAll( turnEndSkillSet , endSource );
		if( turnEndSkillSet.contains( "なし" )) {
			turnEndSkillSet = null;
		}
		
	}
	
	
	//蘇生時のステータス処理
	public void resuscitation() {
		this.survival = 1;
		this.currentATK = defaultATK;
		this.currentDEF = defaultDEF;
		this.currentSPE = defaultSPE;
		statusSet.clear();
		statusSet.add( new Normal() );
	}
	

	//hashCode()とequals()は独自定義
	public int hashCode() {
		
		return allyId.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
}