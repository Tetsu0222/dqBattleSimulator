package com.example.rpg2.battle;

import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BattleProgressService {

	//------------------------------------------------------
	//素早さ順で行動処理を実行させるメソッド
	//------------------------------------------------------
	public boolean turnAction( Battle battle , Integer actionObj , Queue<Integer> turnqueue) {

        // 【暫定実装】
        // 本来 null が渡されることは想定外だが、防御的に false (ターン終了扱い) を返している。
        // 「ログ出力 + 例外スロー」「Controller側での集約ハンドリング」など、
        // 適切なエラーハンドリングは Controller のリファクタリング時に併せて設計する。
        // それまでの間、この経路に到達した場合は警告ログを出力する。
		if( battle == null || actionObj == null || turnqueue == null) {
            log.warn("turnAction received null argument. battle={}, actionObj={}, turnqueue={}",
                     battle, actionObj, turnqueue);
            return false;
        }

        //ターン終了判定
        return this.isPossible( battle , actionObj , turnqueue );
	}

	//-----------------------------------------------------
	//ターン継続判定を行うメソッド
	//再帰的に処理し、falseを返すとターン終了させる。
	//-----------------------------------------------------
	private boolean isPossible( Battle battle , Integer actionObj , Queue<Integer> turnqueue ) {
		
		boolean possible = false;
		
		//味方側の生存チェック
		if( battle.getPartyMap().get( actionObj ) != null ){
			
			//生存しているかどうかで処理を分岐
			if( battle.getPartyMap().get( actionObj ).getSurvival() == 0 ) {
				
				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();
					
					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battle , actionObj , turnqueue)) {
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
					if( this.isPossible( battle , actionObj , turnqueue)) {
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
