package com.example.rpg2.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.rpg2.entity.Monster;
import com.example.rpg2.entity.MonsterPattern;
import com.example.rpg2.repository.MonsterPatternRepository;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class MonsterData {
	
	private String  name;
	private String  originalName;
	private Integer enemyId;
	
	private Integer maxHP;
	private Integer maxMP;
	private Integer defaultATK;
	private Integer defaultDEF;
	private Integer defaultSPE;
	private String pattern;
	private String actions;
	private Integer resistance;
	
	private Integer currentHp;
	private Integer currentMp;
	private Integer currentATK;
	private Integer currentDEF;
	private Integer currentSPE;
	
	private int survival;
	
	
	//モンスターの行動パターンを格納
	List<MonsterPattern> patternList = new ArrayList<>();
	
	//モンスターの行動回数を格納
	List<Integer> actionsList = new ArrayList<>();
	
	//状態異常を管理
	Set<Status> statusSet = new HashSet<>();
	
	
	public MonsterData( Monster monster , MonsterPatternRepository monsterPatternRepository , Integer enemyId ) {
		
		this.name = monster.getName();
		this.enemyId = enemyId;
		
		//固定ステータスの設定
		this.maxHP = monster.getHp();
		this.maxMP = monster.getMp();
		this.defaultATK = monster.getAtk();
		this.defaultDEF = monster.getDef();
		this.defaultSPE = monster.getSpe();
		this.pattern = monster.getPattern();
		this.actions = monster.getActions();
		this.resistance = monster.getResistance();
		
		//変動ステータスの設定
		this.currentHp  = monster.getHp();
		this.currentMp  = monster.getMp();
		this.currentATK = monster.getAtk();
		this.currentDEF = monster.getDef();
		this.currentSPE = monster.getSpe();
		
		//モンスターの行動パターンを設定
		String[] patternSource = pattern.split( "," );
		List<String> patternSourceList = Arrays.asList( patternSource );
		patternSourceList.stream()
		.map( s -> Integer.parseInt( s ) )
		.map( s ->  monsterPatternRepository.findById( s ) )
		.forEach( s -> patternList.add( s.orElseThrow() ));
		
		//モンスターの行動回数を設定
		String[] actionsSource = actions.split( "," );
		List<String> actionsSourceList = Arrays.asList( actionsSource );
		actionsSourceList.stream()
		.map( s -> Integer.parseInt( s ) )
		.forEach( s -> actionsList.add( s ));
		
		//生存設定
		this.survival = 1;
		this.statusSet.add( new Normal() );
	}
	
	
	//蘇生時のステータス処理
	public void resuscitation() {
		this.survival = 1;
		this.currentATK = defaultATK;
		this.currentDEF = defaultDEF;
		this.currentSPE = defaultSPE;
		this.statusSet.add( new Normal() );
	}
	
	public int hashCode() {
		
		return enemyId.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}

}
