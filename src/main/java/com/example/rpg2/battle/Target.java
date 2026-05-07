package com.example.rpg2.battle;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

import lombok.Data;

@Data
public class Target {
	
	private String  skillName;
	private Integer executionId;
	private String  selectionName;
	private Integer selectionId;
	private String  category;
	private Magic   executionMagic;
	private Skill   executionSkill;
	private String  groupName;
	private Set<Integer> targetSetEnemy = new TreeSet<>();
	private Set<Integer> targetSetAlly  = new TreeSet<>();
	

	//通常攻撃
	public Target( MonsterData monsterData , Integer myKeys , Integer key ) {
		
		this.skillName      = "通常攻撃";
		this.selectionName  = monsterData.getName();
		this.selectionId    = key;
		this.executionId    = myKeys;
		this.category	    = "attack";
		this.executionMagic = null;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
		
	}
	
	//味方への魔法
	public Target( AllyData receptionAllyData , Integer myKeys , Integer key , Magic magic ) {
		
		this.skillName      = magic.getName();
		this.selectionName  = receptionAllyData.getName();
		this.selectionId    = key;
		this.executionId    = myKeys;
		this.category	    = magic.getCategory();
		this.executionMagic = magic;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}
	
	//味方への全体魔法
	public Target( Map<Integer,AllyData> partyMap , Set<Integer> targetSetAlly , Integer myKeys , Magic magic , int i ) {
		
		this.skillName      = magic.getName();
		this.selectionName  = "味方全体";
		this.executionId    = myKeys;
		this.category	    = magic.getCategory();
		this.executionMagic = magic;
		this.targetSetEnemy = null;
		this.targetSetAlly  = targetSetAlly;
	}
	
	//攻撃魔法
	public Target( MonsterData monsterData , Integer myKeys , Integer key , Magic magic ) {
		
		this.skillName      = magic.getName();
		this.selectionName  = monsterData.getName();
		this.selectionId    = key;
		this.executionId    = myKeys;
		this.category	    = magic.getCategory();
		this.executionMagic = magic;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}
	
	//グループ攻撃魔法
	public Target( String groupName , Integer myKeys , Magic magic ) {
		
		this.skillName      = magic.getName();
		this.selectionName  = groupName + "グループ";
		this.executionId    = myKeys;
		this.category	    = magic.getCategory();
		this.executionMagic = magic;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
		this.groupName		= groupName;
	}
	
	//全体攻撃魔法
	public Target( Map<Integer,MonsterData> monsterDataMap , Set<Integer> targetSetEnemy , Integer myKeys , Magic magic ) {
		
		this.skillName      = magic.getName();
		this.selectionName  = "敵全体";
		this.executionId    = myKeys;
		this.category	    = magic.getCategory();
		this.executionMagic = magic;
		this.targetSetEnemy = targetSetEnemy;
		this.targetSetAlly  = null;
	}
	
	//味方への特技
	public Target( AllyData receptionAllyData , Integer myKeys , Integer key , Skill skill ) {
		
		this.skillName      = skill.getName();
		this.selectionName  = receptionAllyData.getName();
		this.selectionId    = key;
		this.executionId    = myKeys;
		this.category	    = skill.getCategory();
		this.executionSkill = skill;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}
	
	//味方への全体特技
	public Target( Map<Integer,AllyData> partyMap , Set<Integer> targetSetAlly , Integer myKeys , Skill skill , int i ) {
		
		this.skillName      = skill.getName();
		this.selectionName  = "味方全体";
		this.executionId    = myKeys;
		this.category	    = skill.getCategory();
		this.executionSkill = skill;
		this.targetSetEnemy = null;
		this.targetSetAlly  = targetSetAlly;
	}
	
	//攻撃特技
	public Target( MonsterData monsterData , Integer myKeys , Integer key , Skill skill ) {
		
		this.skillName      = skill.getName();
		this.selectionName  = monsterData.getName();
		this.selectionId    = key;
		this.executionId    = myKeys;
		this.category	    = skill.getCategory();
		this.executionSkill = skill;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}
	
	//グループ攻撃魔法
	public Target( String groupName , Integer myKeys , Skill skill ) {
		
		this.skillName      = skill.getName();
		this.selectionName  = groupName + "グループ";
		this.executionId    = myKeys;
		this.category	    = skill.getCategory();
		this.executionSkill = skill;
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
		this.groupName		= groupName;
	}
	
	//全体攻撃特技
	public Target( Map<Integer,MonsterData> monsterDataMap , Set<Integer> targetSetEnemy , Integer myKeys , Skill skill ) {
		
		this.skillName      = skill.getName();
		this.selectionName  = "敵全体";
		this.executionId    = myKeys;
		this.category	    = skill.getCategory();
		this.executionSkill = skill;
		this.targetSetEnemy = targetSetEnemy;
		this.targetSetAlly  = null;
	}
	
	//防御選択時の処理
	public Target( Integer myKeys , String skillName ) {
		
		this.skillName     = skillName;
		this.selectionName = "";
		this.selectionId   = 0;
		this.executionId   = myKeys;
		this.category	   = "defense";
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}
	
	//死亡時
	public Target( Integer myKeys ) {
		
		this.skillName     = "";
		this.selectionName = "";
		this.selectionId   = 0;
		this.executionId   = myKeys;
		this.category	   = "unable";
		this.targetSetEnemy = null;
		this.targetSetAlly  = null;
	}

}
