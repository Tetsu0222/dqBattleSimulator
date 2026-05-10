package com.example.rpg2.battle;

import java.util.Locale;
import java.util.Queue;

public class BattleProgressService {

    	private Queue<Integer> turnqueue;

	//------------------------------------------------------
	//素早さ順で行動処理を実行させるメソッド
	//------------------------------------------------------
	public boolean turnAction( Battle battle , Locale locale , Integer actionObj) {
		
		if( locale == null || battle == null || actionObj == null) {
            return false;
        }

        //ターン終了判定
        return this.isPossible( battle , actionObj );
	}

	//-----------------------------------------------------
	//ターン継続判定を行うメソッド
	//再帰的に処理し、falseを返すとターン終了させる。
	//-----------------------------------------------------
	private boolean isPossible( Battle battle , Integer actionObj ) {
		
		boolean possible = false;
		
		//味方側の生存チェック
		if( battle.getPartyMap().get( actionObj ) != null ){
			
			//生存しているかどうかで処理を分岐
			if( battle.getPartyMap().get( actionObj ).getSurvival() == 0 ) {
				
				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();
					
					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battle , actionObj )) {
						possible = true;
					
					//自メソッドを繰り返し、結果的に値がなくなっていればターン終了判定(false)を返す。
					}else{
						possible = false;
					}
				
				//次の値が存在しなければターン終了(falseを返す)
				}else{
					possible = false;
				}
				
			//生存していれば処理実行
			}else{
				possible = true;
			}
			
		//敵側の生存チェック
		}else if( battle.getMonsterDataMap().get( actionObj ) != null ){
			
			if( battle.getMonsterDataMap().get( actionObj ).getSurvival() == 0 ) {
					
				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();
						
					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battle , actionObj )) {
						possible = true;
						
					//自メソッドを繰り返し、結果的に値がなくなっていればターン終了判定(false)を返す。
					}else{
						possible = false;
					}
					
				//次の値が存在しなければターン終了(falseを返す)
				}else{
					possible = false;
				}
					
			//生存していれば処理実行
			}else{
				possible = true;
			}
		}
		return possible;
	}
}
