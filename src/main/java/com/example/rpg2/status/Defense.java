package com.example.rpg2.status;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class Defense implements Status{
	
	private String  name = "防御";
	private Integer count;
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	
	public Defense( AllyData allyData ) {
		this.count = 1;
	}
	
	public Defense( MonsterData monsterData ) {
		this.count = 1;
	}
	
	public Defense() {
		
	}
	
	@Override
	public Integer actionStatusBefore() {

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
		return 0;
	}
	
	@Override
	public String statusMessageAfter() {
		//no
		return "no";
	}
	
	@Override
	public Integer countDown() {
		//選択解除のみ
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return "no";
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
