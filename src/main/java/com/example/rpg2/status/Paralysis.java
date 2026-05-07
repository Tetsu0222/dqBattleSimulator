package com.example.rpg2.status;

import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class Paralysis implements Status{
	
	private String name = "麻痺";
	private String targetName;
	private Integer count;
	
	Random random = new Random();
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	
	public Paralysis( AllyData allyData ) {
		this.targetName = allyData.getName();
		this.count = 1;
	}
	
	public Paralysis( MonsterData monsterData ) {
		this.targetName = monsterData.getName();
		this.count = 1;
	}
	
	@Override
	public Integer actionStatusBefore() {
		//50%の確率で行動不能
		return random.nextInt( 2 );
	}
	
	@Override
	public String statusMessageBefore() {
		return targetName + "は麻痺している";
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
		//永続
		return 1;
	}
	
	@Override
	public String recoverymessage() {
		//自然治癒しない。
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
