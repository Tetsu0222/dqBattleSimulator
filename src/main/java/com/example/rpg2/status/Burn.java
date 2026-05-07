package com.example.rpg2.status;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class Burn implements Status{
	
	private String name = "火傷";
	private Integer damage;
	private String message;
	private String targetName;
	private Integer count;
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	//コンストラクタ
	public Burn( AllyData allyData ) {
		this.damage = allyData.getMaxHP() / 30 ;
		this.targetName = allyData.getName();
		this.count = 3;
	}
	
	public Burn( MonsterData monsterData ) {
		this.damage = monsterData.getMaxHP() / 30 ;
		this.targetName = monsterData.getName();
		this.count = 3;
	}
	
	//状態解除用のコンストラクタ
	public Burn() {
		
	}
	
	@Override
	public Integer actionStatusBefore() {
		//no
		return 0;
	}
	
	@Override
	public String statusMessageBefore() {
		//no
		return "no";
	}
	
	@Override
	public Integer actionStatusAfter() {
		//no
		return damage;
	}
	
	@Override
	public String statusMessageAfter() {
		this.message = targetName + "は火傷により" + damage + "ダメージを受けた";
		return message;
	}
	
	@Override
	public Integer countDown() {
		
		if( count < 0 ) {
			this.count = 0;
		}else {
			this.count -= 1;
		}
		
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return targetName + "の火傷が治った!";
	}
	
	
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	

}
