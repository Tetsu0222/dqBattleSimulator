package com.example.rpg2.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;


public class TurnOrderCreate {
	
	public static List<Entry<Integer, Integer>> create( Map<Integer,AllyData> partyMap ,
														Map<Integer,MonsterData> monsterDataMap ,
														Set<Integer> targetSetAlly ,
														Set<Integer> targetSetEnemy ) {
		
		//各キャラの座標と素早さで構成されたマップ
		Map<Integer,Integer> turnMap = new HashMap<>();
		
		//素早さの補正用
		Random random = new Random();
		
		//味方の座標と素早さをマップへ格納
		for( Integer index : targetSetAlly ) {
			Integer spe   = partyMap.get( index ).getCurrentSPE();
			spe += random.nextInt( spe / 2 + 1 );
			turnMap.put( index , spe );
		}
		
		//敵の座標と素早さをマップへ格納
		for( Integer index : targetSetEnemy ) {
			Integer spe   = monsterDataMap.get( index ).getCurrentSPE();
			spe += random.nextInt( spe / 2 + 1 );
			turnMap.put( index , spe );
		}
		
		//敵味方の混合マップからエントリーを抽出、各キャラの座標が素早さの高い順（降順）でソートされているリストを生成
		List<Entry<Integer, Integer>> turnList = new ArrayList<Entry<Integer, Integer>>( turnMap.entrySet() );
        Collections.sort( turnList , new Comparator<Entry<Integer, Integer>>() {
            public int compare( Entry<Integer, Integer> obj1 , Entry<Integer, Integer> obj2 )
            {
            	return obj2.getValue().compareTo( obj1.getValue() );
            }
        });
        
        return turnList;
	}

}
