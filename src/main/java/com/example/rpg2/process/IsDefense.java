package com.example.rpg2.process;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.status.Defense;
import com.example.rpg2.status.Hubaha;
import com.example.rpg2.status.MagicBarrier;

public class IsDefense {
	
	
	//味方の防御状態チェック
	public static boolean isDefense( AllyData allyData ) {
		
		//対象者のステータス異常の中に防御が含まれているか確認(1が返れば含まれていると判定)
		boolean i = allyData.getStatusSet().contains( new Defense() );
		
		//防御が含まれていればtureを返す。
		return i;
	}
	
	
	//敵の防御状態チェック
	public static boolean isDefense( MonsterData monsterData ) {
		
		//対象者のステータス異常の中に防御が含まれているか確認(1が返れば含まれていると判定)
		boolean i = monsterData.getStatusSet().contains( new Defense() );
		
		//防御が含まれていればtureを返す。
		return i;
	}
	
	
	//味方のマジックバリア状態チェック
	public static boolean isDefense( AllyData allyData , Integer dummy ) {
		
		//対象者のステータス異常の中に防御が含まれているか確認(1が返れば含まれていると判定)
		boolean i = allyData.getStatusSet().contains( new MagicBarrier() );
		
		//防御が含まれていればtureを返す。
		return i;
	}
	
	
	//味方のフバーハ状態チェック
	public static boolean isDefense( AllyData allyData , String dummy ) {
		
		//対象者のステータス異常の中に防御が含まれているか確認(1が返れば含まれていると判定)
		boolean i = allyData.getStatusSet().contains( new Hubaha() );
		
		//防御が含まれていればtureを返す。
		return i;
	}

}
