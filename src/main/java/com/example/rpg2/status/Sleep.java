package com.example.rpg2.status;

import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

public class Sleep implements Status{
	
	private String name = "睡眠";
	private String targetName;
	private Integer count;
	
	Random random = new Random();
	
	
	public int hashCode() {
		
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	
	//コンストラクタ
	public Sleep( AllyData allyData ) {
		this.targetName = allyData.getName();
		this.count = 1;
	}
	
	public Sleep( MonsterData monsterData ) {
		this.targetName = monsterData.getName();
		this.count = 1;
	}
	
	//状態解除用のコンストラクタ
	public Sleep() {
		
	}
	
	
	
	@Override
	public Integer actionStatusBefore() {

		return 1;
	}
	
	@Override
	public String statusMessageBefore() {
		
		return targetName + "は眠っていて動けない";
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
		
		this.count = random.nextInt( 2 );
		
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return targetName + "は目を覚ました!!";
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
