package com.example.rpg2.action;

import com.example.rpg2.battle.AllyData;

public interface TargetAllyAction {
	
	public String getStratMessage();
	public String getNotEnoughMpMessage();
	public String getResultMessage();
	public String getRecoveryMessage();
	
	public boolean isNotEnoughMp();
	public AllyData action( AllyData receptionAllyData );

}
