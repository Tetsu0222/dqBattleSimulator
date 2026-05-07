package com.example.rpg2.process;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;


//素早さ順に応じて処理するための順序を規定
public class TurnQueue {
	
	
	public static Queue<Integer> getTurnQueue( List<Entry<Integer, Integer>> turnList ){
		
		Queue<Integer> turnqueue = new ArrayDeque<>();
		
		for( Entry<Integer, Integer> entry : turnList){
			Integer key = entry.getKey();
			turnqueue.add( key );
		}
		
		return turnqueue;
	}
}
