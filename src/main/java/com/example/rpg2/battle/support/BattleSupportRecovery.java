package com.example.rpg2.battle.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.example.rpg2.action.SortingRecoveryAction;
import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

import lombok.Data;

@Data
public class BattleSupportRecovery {
	
	//プレイアブルメンバーを管理
	private Map<Integer,AllyData> partyMap;
	
	//エネミーメンバーを管理
	private Map<Integer,MonsterData> monsterDataMap;
	
	//プレイアブルメンバーの行動選択を管理
	private Map<Integer,Target> targetMap;
	
	//味方の数とキーを管理、キャラの特定に使用
	private Set<Integer> targetSetAlly;
	
	//敵の数とキーを管理、同上
	private Set<Integer> targetSetEnemy;
	
	//表示するログを管理
	private List<String> mesageList = new ArrayList<>();
	
	//グループ攻撃用のセット
	private List<String> enemyNameList;
	private List<String> allyNameList;
	
	Random random = new Random();
	
	
	public BattleSupportRecovery( Battle battle ) {
		this.partyMap = battle.getPartyMap();
		this.monsterDataMap = battle.getMonsterDataMap();
		this.targetMap = battle.getTargetMap();
		this.targetSetAlly = battle.getTargetSetAlly();
		this.targetSetEnemy = battle.getTargetSetEnemy();
		this.enemyNameList = battle.getEnemyNameList();
		this.allyNameList = battle.getAllyNameList();
	}
	
	
	//------------------------------------------------------------------------------
	//---------------行動選択処理(選択中の行動と対象者の表示に必要)-----------------
	//------------------------------------------------------------------------------
	
	//味方への魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Integer key , Magic magic ) {
		Target target = new Target ( partyMap.get( key ) , myKeys , key , magic );
		targetMap.put( myKeys , target );
	}
	
	//味方への全体魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Magic magic ) {
		//最後の引数はオーバーロード用のダミー
		Target target = new Target ( partyMap , targetSetAlly , myKeys ,  magic , 1 );
		targetMap.put( myKeys , target );
	}
	
	//味方への特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Integer key , Skill skill ) {
		Target target = new Target ( partyMap.get( key ) , myKeys , key , skill );
		targetMap.put( myKeys , target );
	}
	
	//味方への全体特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Skill skill ) {
		//最後の引数はオーバーロード用のダミー
		Target target = new Target ( partyMap , targetSetAlly , myKeys ,  skill , 1 );
		targetMap.put( myKeys , target );
	}
	
	//--------------------------------------------------------------------------------------
	
	
	
	public boolean magicOrSkillRecovery( AllyData allyData , Magic magic , Skill skill , Integer  target , Integer key ) {
		
		boolean isMpEmpty = false;
		
		//回復or補助or蘇生の魔法か特技か判定して該当オブジェクトを生成
		TargetAllyAction targetAllyAction = SortingRecoveryAction.sortingCreateRecoveryAction( allyData , magic , skill );
			
		//行動を宣言
		this.mesageList.add( targetAllyAction.getStratMessage() );
			
		//MP判定 MPが足りないとtureが返る。
		if( targetAllyAction.isNotEnoughMp() ){
			this.mesageList.add( targetAllyAction.getNotEnoughMpMessage() );
			isMpEmpty = true;
				
		//MP判定OK
		}else{
				
			//魔法特技の指定回数分の処理
			for( int i = 0 ; i < SortingRecoveryAction.actions ; i++ ){
					
				//無差別回復
				if( SortingRecoveryAction.targetRandom ) {
					List<Integer> targetList = new ArrayList<Integer>( targetSetAlly );
					target = random.nextInt( targetList.size() );
					this.singleSupport( targetAllyAction , target , key );
					
				//蘇生
				}else if( SortingRecoveryAction.isResuscitation ) {
						
					//全体蘇生魔法の処理
					if( targetMap.get( key ).getTargetSetAlly() != null ) {
						this.resuscitationMagicExecution( targetAllyAction , -1 , key );
							
					//単体蘇生魔法の処理
					}else{
						this.resuscitationMagicExecution( targetAllyAction , target , key );
					}
						
					//全体回復魔法の処理
					}else if( targetMap.get( key ).getTargetSetAlly() != null ) {
						this.generalSupport( targetAllyAction , key );
						
					//単体回復魔法の処理
					}else{
						this.singleSupport( targetAllyAction , target , key );
				}
			}
		}
			
		return isMpEmpty;
	}
	
	
	//単体回復・補助のメソッド
	public void singleSupport( TargetAllyAction targetAllyAction , Integer target , Integer key ) {
		
		//対象の味方キャラクターのオブジェクトを取得
		AllyData receptionAllyData = partyMap.get( target );
		
		//対象がターン中に死亡している場合は、別の生存対象へ処理対象を変更
		if( receptionAllyData.getSurvival() == 0 ) {
			target = targetSetAlly.stream().findAny().orElse( 0 );
			this.selectionAllyMagic( key , target , targetMap.get( key ).getExecutionMagic() );
		}
		
		//回復・補助魔法の処理と結果の格納
		receptionAllyData = targetAllyAction.action( partyMap.get( target ) );
		partyMap.put( target , receptionAllyData );
		
		//回復効果があれば表示に追加
		if( targetAllyAction.getRecoveryMessage() != null ) {
			this.mesageList.add( targetAllyAction.getRecoveryMessage() );
		}
		
		//状態異常の治癒があれば結果に追加
		if( targetAllyAction.getResultMessage() != null ) {
			this.mesageList.add( targetAllyAction.getResultMessage() );
		}
		
	}
	
	
	//全体回復・補助のメソッド
	public void generalSupport( TargetAllyAction targetAllyAction , Integer key) {
		
		//ターゲットを全体に変更
		for( Integer target : targetSetAlly ) {
			
			//処理結果後のデータを取得して格納
			AllyData receptionAllyData = targetAllyAction.action( partyMap.get( target ) );
			partyMap.put( target , receptionAllyData );
			
			//回復効果があれば表示に追加
			if( targetAllyAction.getRecoveryMessage() != null ) {
				this.mesageList.add( targetAllyAction.getRecoveryMessage() );
			}
			
			//状態異常の治癒があれば結果に追加
			if( targetAllyAction.getResultMessage() != null ) {
				this.mesageList.add( targetAllyAction.getResultMessage() );
			}
		}
	}
	
	
	//蘇生魔法の処理メソッド
	public void resuscitationMagicExecution( TargetAllyAction targetAllyAction , Integer target , Integer key ) {
		
		//全体魔法の処理
		if( target < 0 ) {
			
			//生死問わず、全体に作用するようにリストを生成
			List<Integer> list = new ArrayList<>(partyMap.keySet());
			
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
				this.mesageList.add( targetAllyAction.getRecoveryMessage() );
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
			this.mesageList.add( targetAllyAction.getRecoveryMessage() );
		}
	}
}
