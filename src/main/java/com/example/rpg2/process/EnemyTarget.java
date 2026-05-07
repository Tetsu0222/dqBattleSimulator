package com.example.rpg2.process;

import java.util.List;
import java.util.Random;

public class EnemyTarget {
	
	
	//プレイアブルキャラクターの人数
	private static final int fourSome = 4;
	private static final int threeSome = 3;
	private static final int twoSome = 2;
	private static final int oneSome = 1;
	
	//確率の分母
	private static final int denominator = 10;
	private static final int halfDenominator = 2;
	
	
	//先頭の方が狙われやすい。
	public static Integer enemyTarget( List<Integer> targetList , int numberOfPeople ) {
		
		Random random = new Random();
		int target = 0;
		int targetiD = 0;
		
		
		switch( numberOfPeople ) {
		
			case fourSome:
				
				target = random.nextInt( denominator );
				
				if( target < 4 ) {
					targetiD = targetList.get( 0 );	//0～3の数値だと先頭のメンバーを攻撃対象に選択(40%)
					
				}else if( target < 7 ){
					targetiD = targetList.get( 1 ); //33%
					
				}else if( target < 9 ){
					targetiD = targetList.get( 2 ); //20%
					
				}else if( target == 9 ){
					targetiD = targetList.get( 3 ); //10%
				}
				
				break;
				
				
			case threeSome:
				
				target = random.nextInt( denominator );
				
				if( target < 5 ) {
					targetiD = targetList.get( 0 );
					
				}else if( target < 9 ){
					targetiD = targetList.get( 1 );
						
				}else if( target == 9 ){
					targetiD = targetList.get( 2 );
				}
				
				break;
				
				
			case twoSome:	//50%でどちらかを対象に
				
				target = random.nextInt( halfDenominator );
				
				if( target == 0 ) {
					targetiD = targetList.get( 0 );
					
				}else{
					targetiD = targetList.get( 1 );
				}
				
				break;
				
				
			case oneSome:
				
				targetiD = targetList.get( 0 );
				
				break;
			
		}
		
		return targetiD;
	}

}
