package com.example.rpg2.service.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.rpg2.action.SortingRecoveryAction;
import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.dto.BattleRecord;
import com.example.rpg2.dto.BattleState;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BattleRecoveryService {

	private final BattleManagementService battleManagementService;

	private final Random random = new Random();


	public boolean magicOrSkillRecovery( AllyData allyData , Magic magic , Skill skill , Integer target , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		boolean isMpEmpty = false;

		//回復or補助or蘇生の魔法か特技か判定して該当オブジェクトを生成
		TargetAllyAction targetAllyAction = SortingRecoveryAction.sortingCreateRecoveryAction( allyData , magic , skill );

		//行動を宣言
		battleState.getMesageList().add( targetAllyAction.getStratMessage() );

		//MP判定 MPが足りないとtureが返る。
		if( targetAllyAction.isNotEnoughMp() ){
			battleState.getMesageList().add( targetAllyAction.getNotEnoughMpMessage() );
			isMpEmpty = true;

		//MP判定OK
		}else{

			Set<Integer> targetSetAlly      = battleState.getTargetSetAlly();
			Map<Integer, com.example.rpg2.battle.Target> targetMap = battleState.getTargetMap();

			//魔法特技の指定回数分の処理
			for( int i = 0 ; i < SortingRecoveryAction.actions ; i++ ){

				//無差別回復
				if( SortingRecoveryAction.targetRandom ) {
					List<Integer> targetList = new ArrayList<Integer>( targetSetAlly );
					target = random.nextInt( targetList.size() );
					this.singleSupport( targetAllyAction , target , key , battleRecord , battleState );

				//蘇生
				}else if( SortingRecoveryAction.isResuscitation ) {

					//全体蘇生魔法の処理
					if( targetMap.get( key ).getTargetSetAlly() != null ) {
						this.resuscitationMagicExecution( targetAllyAction , -1 , key , battleRecord , battleState );

					//単体蘇生魔法の処理
					}else{
						this.resuscitationMagicExecution( targetAllyAction , target , key , battleRecord , battleState );
					}

					//全体回復魔法の処理
					}else if( targetMap.get( key ).getTargetSetAlly() != null ) {
						this.generalSupport( targetAllyAction , key , battleRecord , battleState );

					//単体回復魔法の処理
					}else{
						this.singleSupport( targetAllyAction , target , key , battleRecord , battleState );
				}
			}
		}

		return isMpEmpty;
	}


	//単体回復・補助のメソッド
	public void singleSupport( TargetAllyAction targetAllyAction , Integer target , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData> partyMap      = battleRecord.partyMap();
		Set<Integer>          targetSetAlly = battleState.getTargetSetAlly();

		//対象の味方キャラクターのオブジェクトを取得
		AllyData receptionAllyData = partyMap.get( target );

		//対象がターン中に死亡している場合は、別の生存対象へ処理対象を変更
		if( receptionAllyData.getSurvival() == 0 ) {
			target = targetSetAlly.stream().findAny().orElse( 0 );
			battleManagementService.selectionAllyMagic( key , target , battleState.getTargetMap().get( key ).getExecutionMagic() , battleRecord , battleState );
		}

		//回復・補助魔法の処理と結果の格納
		receptionAllyData = targetAllyAction.action( partyMap.get( target ) );
		partyMap.put( target , receptionAllyData );

		//回復効果があれば表示に追加
		if( targetAllyAction.getRecoveryMessage() != null ) {
			battleState.getMesageList().add( targetAllyAction.getRecoveryMessage() );
		}

		//状態異常の治癒があれば結果に追加
		if( targetAllyAction.getResultMessage() != null ) {
			battleState.getMesageList().add( targetAllyAction.getResultMessage() );
		}

	}


	//全体回復・補助のメソッド
	public void generalSupport( TargetAllyAction targetAllyAction , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData> partyMap      = battleRecord.partyMap();
		Set<Integer>          targetSetAlly = battleState.getTargetSetAlly();

		//ターゲットを全体に変更
		for( Integer target : targetSetAlly ) {

			//処理結果後のデータを取得して格納
			AllyData receptionAllyData = targetAllyAction.action( partyMap.get( target ) );
			partyMap.put( target , receptionAllyData );

			//回復効果があれば表示に追加
			if( targetAllyAction.getRecoveryMessage() != null ) {
				battleState.getMesageList().add( targetAllyAction.getRecoveryMessage() );
			}

			//状態異常の治癒があれば結果に追加
			if( targetAllyAction.getResultMessage() != null ) {
				battleState.getMesageList().add( targetAllyAction.getResultMessage() );
			}
		}
	}


	//蘇生魔法の処理メソッド
	public void resuscitationMagicExecution( TargetAllyAction targetAllyAction , Integer target , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		Map<Integer,AllyData> partyMap      = battleRecord.partyMap();
		Set<Integer>          targetSetAlly = battleState.getTargetSetAlly();

		//全体魔法の処理
		if( target < 0 ) {

			//生死問わず、全体に作用するようにリストを生成
			List<Integer> list = new ArrayList<>( partyMap.keySet() );

			//ターゲットを全体で再設定
			for( Integer target2 : list ) {

				//蘇生処理を実行
				AllyData receptionAllyData = targetAllyAction.action( partyMap.get( target2 ) );

				//蘇生判定の確認
				if( receptionAllyData.getSurvival() > 0 ) {

					//蘇生に成功していれば結果を格納
					partyMap.put( target2 , receptionAllyData );
					targetSetAlly.add( target2 );
				}

				//回復メッセージを格納
				battleState.getMesageList().add( targetAllyAction.getRecoveryMessage() );
			}

		//単体魔法の処理
		}else{

			//蘇生処理を実行
			AllyData receptionAllyData = targetAllyAction.action( partyMap.get( target ) );

			//蘇生判定の確認
			if( receptionAllyData.getSurvival() > 0 ) {

				//蘇生に成功していれば結果を格納
				partyMap.put( target , receptionAllyData );
				targetSetAlly.add( target );
			}

			//蘇生メッセージを格納
			battleState.getMesageList().add( targetAllyAction.getRecoveryMessage() );
		}
	}
}
