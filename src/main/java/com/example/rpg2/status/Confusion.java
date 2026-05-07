package com.example.rpg2.status;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class Confusion implements Status{
	
	private String name = "混乱";
	private String targetName;
	private Integer count;
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	//コンストラクタ
	public Confusion( AllyData allyData ) {
		this.targetName = allyData.getName();
		this.count = 3;
	}
	
	public Confusion( MonsterData monsterData ) {
		this.targetName = monsterData.getName();
		this.count = 3;
	}
	
	//状態解除用のコンストラクタ
	public Confusion() {
		
	}
	
	@Override
	public Integer actionStatusBefore() {
		return 0;
	}
	
	@Override
	public String statusMessageBefore() {
		return targetName + "は混乱している。";
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
		
		this.count -= 1;
		
		if( count < 0 ) {
			this.count = 0;
		}
		
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return targetName + "は意識を回復させた!";
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
