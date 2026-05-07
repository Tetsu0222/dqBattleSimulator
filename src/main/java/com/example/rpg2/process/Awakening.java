package com.example.rpg2.process;

import java.util.Set;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Sleep;
import com.example.rpg2.status.Status;


public class Awakening {
	
	
	//物理攻撃による睡眠解除
	public static AllyData awakening( AllyData allyData ){
		
		Sleep sleep = new Sleep();
		Set<Status> statusSet = allyData.getStatusSet();
		statusSet.remove( sleep );
		
		int size = statusSet.size();
		
		if( size == 0 ) {
			statusSet.add( new Normal() );
		}
		
		allyData.setStatusSet( statusSet );
		
		return allyData;
	}
	
	
	//物理攻撃による睡眠解除
	public static MonsterData awakening( MonsterData monsterData , Sleep sleep ){
		
		Set<Status> statusSet = monsterData.getStatusSet();
		statusSet.remove( sleep );
		
		int size = statusSet.size();
		
		if( size == 0 ) {
			statusSet.add( new Normal() );
		}
		
		monsterData.setStatusSet( statusSet );
		
		return monsterData;
	}

}
