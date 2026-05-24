package com.example.rpg2.service.battle;

import java.util.Queue;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.rpg2.action.endskill.SortingEndSkill;
import com.example.rpg2.action.startskill.SortingStartSkill;
import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.util.battle.BadStatusBefore;
import com.example.rpg2.util.battle.CancelDefense;
import com.example.rpg2.util.battle.ChoiceDefense;
import com.example.rpg2.util.battle.ConsumptionMP;
import com.example.rpg2.util.battle.IsEndSkillStop;
import com.example.rpg2.util.battle.IsStartSkillStop;
import com.example.rpg2.util.battle.TurnOrderCreate;
import com.example.rpg2.status.Confusion;

@Slf4j
@Service
@RequiredArgsConstructor
public class BattleProgressService {

	private final BattleAttackService   battleAttackService;
	private final BattleRecoveryService battleRecoveryService;
	private final BattleStatusService   battleStatusService;
	private final BattleEnemyService    battleEnemyService;


	//------------------------------------------------------
	//行動順を決定
	//------------------------------------------------------
	public void turn( BattleRecord battleRecord , BattleState battleState ) {
		battleState.setTurnList( TurnOrderCreate.create(
			battleRecord.partyMap() , battleRecord.monsterDataMap() ,
			battleState.getTargetSetAlly() , battleState.getTargetSetEnemy() ));
	}


	//------------------------------------------------------
	//戦闘処理(味方または敵の1キャラクター分の行動)
	//------------------------------------------------------
	public void startBattle( Integer key , BattleRecord battleRecord , BattleState battleState ) {

        //------------------味方側の処理----------------------
		if( battleRecord.partyMap().get( key ) != null ) {
    		AllyData allyData         = battleRecord.partyMap().get( key );
    		Integer  target	          = battleState.getTargetMap().get( key ).getSelectionId();
    		String   movementPattern  = battleState.getTargetMap().get( key ).getCategory();
			Skill 	 skill            = battleState.getTargetMap().get( key ).getExecutionSkill();
			Magic 	 magic            = battleState.getTargetMap().get( key ).getExecutionMagic();
			boolean  isMpEmpty        = false;

    		//ターン中に死亡している場合は、処理を中断(カウンターなどを想定)
    		if( allyData.getSurvival() == 0 ) {
    			movementPattern = "";
    		}

			//行動不能系の状態異常の所持数をチェック
    		BadStatusBefore badStatusBefore = new BadStatusBefore();
    		Integer juds = badStatusBefore.execution( allyData );

    		//行動不能の状態異常があれば、そのメッセージを格納
    		if( badStatusBefore.getMessage() != null ) {
    			battleState.getMesageList().add( badStatusBefore.getMessage() );
    		}

    		//行動不能と判定された状態異常が1つ以上あれば処理中断
    		if( juds > 0 ) {
    			battleStatusService.badStatusAfter( allyData , key , battleRecord , battleState );
    			movementPattern = "";
    		}

    		//混乱中の場合の処理
    		if( allyData.getStatusSet().contains( new Confusion() )) {
    			movementPattern = "confusion";
    		}


    		//プレイアブルキャラクターの行動
    		switch( movementPattern ) {

    			//通常攻撃の処理
	    		case "attack":
					battleAttackService.normalAttack( target , key , magic , skill , allyData , battleRecord , battleState );
					break;


				//回復・補助魔法や特技の処理
	    		case "targetally" :
	    		case "resuscitationmagic":
	    		case "resuscitationskill":
					isMpEmpty = battleRecoveryService.magicOrSkillRecovery( allyData , magic , skill , target , key , battleRecord , battleState );
					break;


				//攻撃・妨害魔法や特技の処理
	    		case "targetenemy" :
					isMpEmpty = battleAttackService.magicOrSkillAttack( allyData , magic , skill , target , key , battleRecord , battleState );
					break;


				//防御中
	    		case "defense" :
	    			battleState.getMesageList().add( allyData.getName() + "は防御している" );
	    			break;


	    		//混乱中
	    		case "confusion":
					battleStatusService.confusion( allyData , battleRecord , battleState );
					break;
    		}

			//MP消費処理
			if( !isMpEmpty ) {
				allyData = ConsumptionMP.consumptionMP( allyData , magic , skill );
				battleRecord.partyMap().put( key , allyData );
			}

			//行動終了後に作用する状態異常の処理
			battleStatusService.badStatusAfter( allyData , key , battleRecord , battleState );


		//------------------敵側の処理------------------------
		}else if( battleRecord.monsterDataMap().get( key ) != null ){

			//敵の行動を処理
			battleEnemyService.enemyAction( key , battleRecord , battleState );

    		//行動終了後の状態異常を処理
			MonsterData monsterData = battleRecord.monsterDataMap().get( key );
    		battleStatusService.badStatusAfter( monsterData , key , battleRecord , battleState );
    	}
	}


	//------------------------------------------------------
	//ターンスタート時の処理
	//------------------------------------------------------
	public void startSkill( BattleRecord battleRecord , BattleState battleState ) {

		for( int index : battleState.getTargetSetAlly() ) {

			com.example.rpg2.domain.Target target = battleState.getTargetMap().get( index );
			AllyData allyData = battleRecord.partyMap().get( index );

			//防御の発動処理
			if( target.getSkillName().equals( "防御" )) {
				allyData = ChoiceDefense.choiceDefense( allyData );
				battleRecord.partyMap().put( index , allyData );
			}

			//スタートスキルを所持しつつ行動不能系の状態異常がなければ続行
			if( IsStartSkillStop.isStartSkillStop( allyData ) && allyData.getTurnStartSkillSet() != null) {
				AllyData allyData2 = battleRecord.partyMap().get( index ); //実質的にファイナルとするため再初期化
				allyData.getTurnStartSkillSet().stream()
				.map( s -> SortingStartSkill.sortingSkill( s ))
				.map( s -> s.action( allyData2 ) )
				.peek( s -> battleRecord.partyMap().put( index , allyData2 ))
				.filter( s -> s.getStartSkillMessage() != null )
				.peek( s -> battleState.getMesageList().add( s.getStartSkillMessage() ))
				.forEach( s -> s.setStartSkillMessage( null ));
			}
		}
	}


	//------------------------------------------------------
	//ターンエンド時の処理
	//------------------------------------------------------
	public void endSkill( BattleRecord battleRecord , BattleState battleState ) {

		for( int index : battleState.getTargetSetAlly() ) {

			//防御状態の解除（行動不能でも実行）
			AllyData allyData = battleRecord.partyMap().get( index );
			allyData = CancelDefense.cancelDefense( allyData );
			battleRecord.partyMap().put( index , allyData );

			//エンドスキルを所持しつつ行動不能系の状態異常がなければ続行
			if( IsEndSkillStop.isEndSkillStop( allyData ) && allyData.getTurnEndSkillSet() != null ) {
				AllyData allyData2 = battleRecord.partyMap().get( index ); //実質的にファイナルとするため再初期化
				allyData2.getTurnEndSkillSet().stream()
				.map( s -> SortingEndSkill.sortingSkill( s ))
				.map( s -> s.action( allyData2 ) )
				.peek( s -> battleRecord.partyMap().put( index , allyData2 ))
				.filter( s -> s.getEndSkillMessage() != null )
				.peek( s -> battleState.getMesageList().add( s.getEndSkillMessage() ))
				.forEach( s -> s.setEndSkillMessage( null ));
			}
		}
	}


	//------------------------------------------------------
	//素早さ順で行動処理を実行させるメソッド
	//------------------------------------------------------
	public boolean turnAction( BattleRecord battleRecord , BattleState battleState , Integer actionObj , Queue<Integer> turnqueue) {

        // 【暫定実装】
        // 本来 null が渡されることは想定外だが、防御的に false (ターン終了扱い) を返している。
        // 「ログ出力 + 例外スロー」「Controller側での集約ハンドリング」など、
        // 適切なエラーハンドリングは Controller のリファクタリング時に併せて設計する。
        // それまでの間、この経路に到達した場合は警告ログを出力する。
		if( battleRecord == null || battleState == null || actionObj == null || turnqueue == null) {
            log.warn("turnAction received null argument. battleRecord={}, battleState={}, actionObj={}, turnqueue={}",
                     battleRecord, battleState, actionObj, turnqueue);
            return false;
        }

        //ターン終了判定
        return this.isPossible( battleRecord , battleState , actionObj , turnqueue );
	}

	//-----------------------------------------------------
	//ターン継続判定を行うメソッド
	//再帰的に処理し、falseを返すとターン終了させる。
	//-----------------------------------------------------
	private boolean isPossible( BattleRecord battleRecord , BattleState battleState , Integer actionObj , Queue<Integer> turnqueue ) {

		boolean possible = false;

		//味方側の生存チェック
		if( battleRecord.partyMap().get( actionObj ) != null ){

			//生存しているかどうかで処理を分岐
			if( battleRecord.partyMap().get( actionObj ).getSurvival() == 0 ) {

				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();

					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battleRecord , battleState , actionObj , turnqueue)) {
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
		}else if( battleRecord.monsterDataMap().get( actionObj ) != null ){

			if( battleRecord.monsterDataMap().get( actionObj ).getSurvival() == 0 ) {

				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();

					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battleRecord , battleState , actionObj , turnqueue)) {
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
