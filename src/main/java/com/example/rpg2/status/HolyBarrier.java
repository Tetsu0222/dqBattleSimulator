package com.example.rpg2.status;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class HolyBarrier implements Status{
	
	private String  name = "聖なる守り";
	private String  targetName;
	private Integer count;
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	
	public HolyBarrier( AllyData allyData ) {
		this.targetName = allyData.getName();
		this.count = 3;
	}
	
	public HolyBarrier( MonsterData monsterData ) {
		this.targetName = monsterData.getName();
		this.count = 3;
	}
	
	public HolyBarrier() {
		
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
		
		if( count < 0 ) {
			this.count = 0;
			
		}else {
			this.count -= 1;
		}
		
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return targetName + "の聖なる守りの効果が切れてしまった…";
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
