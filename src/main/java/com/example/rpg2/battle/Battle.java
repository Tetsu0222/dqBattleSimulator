package com.example.rpg2.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.example.rpg2.action.endskill.SortingEndSkill;
import com.example.rpg2.action.startskill.SortingStartSkill;
import com.example.rpg2.battle.support.BattleSupportAttack;
import com.example.rpg2.battle.support.BattleSupportEnemy;
import com.example.rpg2.battle.support.BattleSupportRecovery;
import com.example.rpg2.battle.support.BattleSupportStatus;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.process.BadStatusBefore;
import com.example.rpg2.process.CancelDefense;
import com.example.rpg2.process.ChoiceDefense;
import com.example.rpg2.process.ConsumptionMP;
import com.example.rpg2.process.IsEndSkillStop;
import com.example.rpg2.process.IsStartSkillStop;
import com.example.rpg2.process.TurnOrderCreate;
import com.example.rpg2.status.Confusion;

import lombok.Data;

@Data
public class Battle {
	
	
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
	
	//キーは敵味方混合、値は乱数補正後の素早さ。素早さ順で降順ソートしたリスト
	private List<Entry<Integer, Integer>> turnList;
	
	//グループ攻撃用のセット
	private List<String> enemyNameList;
	private List<String> allyNameList;
	
	Random random = new Random();
	
	private BattleSupportAttack   battleSupportAttack;
	private BattleSupportRecovery battleSupportRecovery;
	private BattleSupportStatus   battleSupportStatus;
	private BattleSupportEnemy    battleSupportEnemy;
	
	//コンストラクタ(戦闘不能と蘇生の関係で、複数のコレクションで敵味方の座標とオブジェクトを管理)
	public Battle( Set<AllyData> partySet , Set<MonsterData> monsterDataSet , List<String> allyNameList , List<String> enemyNameList ) {
		
		List<AllyData>    partyList       = new ArrayList<>( partySet );
		List<MonsterData> monsterDataList = new ArrayList<>( monsterDataSet );
		
		//プレイアブルメンバーを生成
		this.partyMap = IntStream.range( 0 , partyList.size() )
							.boxed()
							.collect( Collectors.toMap( s -> s , s -> partyList.get( s ) ));
		
		//エネミーデータを生成
		this.monsterDataMap = IntStream.range( 4 , monsterDataList.size() + 4 )
				.boxed()
				.collect( Collectors.toMap( s -> s , s -> monsterDataList.get( s - 4 ) ));
		
		//プレイアブルメンバーの初期行動を最初のエネミーへの通常攻撃で設定（例外対策）
		this.targetMap = IntStream.range( 0 , partyList.size() )
								.boxed()
								.collect( Collectors.toMap( s -> s ,
										s -> new Target( monsterDataMap.get( 4 )  , s  , 4 )));
		
		//味方と敵の座標リストをそれぞれ生成(各マップのキー数字とリンク）
		this.targetSetEnemy = new TreeSet<>( monsterDataMap.keySet() );
		this.targetSetAlly  = new TreeSet<>( partyMap.keySet() );
		
		//グループ攻撃用のセットを設定
		this.allyNameList  = allyNameList;
		this.enemyNameList = enemyNameList;
		
	}
	
	//サポートクラスの生成
	public void createSupport () {
		this.battleSupportAttack   = new BattleSupportAttack  ( this );
		this.battleSupportRecovery = new BattleSupportRecovery( this );
		this.battleSupportStatus   = new BattleSupportStatus  ( this );
		this.battleSupportEnemy    = new BattleSupportEnemy   ( this );
	}
	
	
	//---------------行動選択処理(選択中の行動と対象者の表示に必要)-----------------
	//通常攻撃が選択された場合の事前処理
	public void selectionAttack( Integer myKeys , Integer key ) {
		battleSupportAttack.selectionAttack( myKeys , key );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//味方への魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Integer key , Magic magic ) {
		battleSupportRecovery.selectionAllyMagic( myKeys , key , magic );
		this.targetMap = battleSupportRecovery.getTargetMap();
	}
	
	//味方への全体魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Magic magic ) {
		battleSupportRecovery.selectionAllyMagic( myKeys , magic );
		this.targetMap = battleSupportRecovery.getTargetMap();
	}
	
	//敵への魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Integer key , Magic magic ) {
		battleSupportAttack.selectionMonsterMagic( myKeys , key , magic );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//敵へのグループ攻撃魔法が選択された時の処理
	public void selectionMonsterMagic( String name , Integer myKeys , Magic magic ) {
		battleSupportAttack.selectionMonsterMagic( name , myKeys , magic );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//敵への全体魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Magic magic ) {
		battleSupportAttack.selectionMonsterMagic( myKeys , magic );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//味方への特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Integer key , Skill skill ) {
		battleSupportRecovery.selectionAllySkill( myKeys , key , skill );
		this.targetMap = battleSupportRecovery.getTargetMap();
	}
	
	//味方への全体特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Skill skill ) {
		battleSupportRecovery.selectionAllySkill( myKeys , skill );
		this.targetMap = battleSupportRecovery.getTargetMap();
	}
	
	//敵への特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Integer key , Skill skill ) {
		battleSupportAttack.selectionMonsterSkill( myKeys , key , skill );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//敵へのグループ攻撃特技が選択された時の処理
	public void selectionMonsterSkill( String name , Integer myKeys , Skill skill ) {
		battleSupportAttack.selectionMonsterSkill( name , myKeys , skill );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//敵への全体特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Skill skill ) {
		battleSupportAttack.selectionMonsterSkill( myKeys ,  skill );
		this.targetMap = battleSupportAttack.getTargetMap();
	}
	
	//防御を選択
	public void selectionDefense( Integer myKeys ) {
		Target target = new Target( myKeys , "防御" );
		targetMap.put( myKeys , target );
	}
	
	//--------------------------------------------------------------------------------------
	
	
	//行動順を決定
	public void turn() {
		this.turnList = TurnOrderCreate.create( partyMap , monsterDataMap , targetSetAlly , targetSetEnemy );
	}
	
	
	//戦闘処理
	public void startBattle( Integer key ) {
		
        //------------------味方側の処理----------------------
		if( partyMap.get( key ) != null ) {
    		AllyData allyData = partyMap.get( key );
    		Integer  target	  = targetMap.get( key ).getSelectionId();
    		String   movementPattern = targetMap.get( key ).getCategory();
			Skill 	 skill    = targetMap.get( key ).getExecutionSkill();
			Magic 	 magic    = targetMap.get( key ).getExecutionMagic();
			boolean isMpEmpty = false;

    		//ターン中に死亡している場合は、処理を中断(カウンターなどを想定)
    		if( allyData.getSurvival() == 0 ) {
    			movementPattern = "";
    		}
    			
			//行動不能系の状態異常の所持数をチェック
    		BadStatusBefore badStatusBefore = new BadStatusBefore();
    		Integer juds = badStatusBefore.execution( allyData );
    			
    		//行動不能の状態異常があれば、そのメッセージを格納
    		if( badStatusBefore.getMessage() != null ) {
    			this.mesageList.add( badStatusBefore.getMessage() );
    		}
    			
    		//行動不能と判定された状態異常が1つ以上あれば処理中断
    		if( juds > 0 ) {
    			battleSupportStatus.badStatusAfter( allyData, key );
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
	    			
					//サポートクラスとデータを同期
					this.battleSupportAttack = new BattleSupportAttack( this );
					
					//通常攻撃の処理実行
					battleSupportAttack.normalAttack( target , key , magic , skill , allyData );
	
					//サポートクラスから処理結果を取得
					this.result();
					
					break;
				
					
				//回復・補助魔法や特技の処理
	    		case "targetally" :
	    		case "resuscitationmagic":
	    		case "resuscitationskill":
	    			
					//サポートクラスとデータを同期
					this.battleSupportRecovery = new BattleSupportRecovery( this );
					
					//回復・補助・蘇生の魔法か特技の処理、MPが足りていたかどうかの結果が返る。
					isMpEmpty = battleSupportRecovery.magicOrSkillRecovery( allyData , magic , skill , target , key );
					
					//処理結果を格納
					this.resultRecovery();
					
					break;
					
					
				//攻撃・妨害魔法や特技の処理
	    		case "targetenemy" :
	    			
					//サポートクラスとデータを同期
					this.battleSupportAttack = new BattleSupportAttack( this );
					
					//攻撃魔法か特技を発動、MPが足りていたかどうかの結果が返る。
					isMpEmpty = battleSupportAttack.magicOrSkillAttack( allyData , magic , skill , target , key );
					
					//処理結果をサポートクラスから取得
					this.result();
					
					break;
					
					
				//防御中
	    		case "defense" :
	    			this.mesageList.add( allyData.getName() + "は防御している" );
	    			
	    			break;
	    		
	    			
	    		//混乱中
	    		case "confusion":
	    			
					//サポートクラスとデータを同期
					this.battleSupportStatus = new BattleSupportStatus( this );
					
					//混乱の行動を処理
					battleSupportStatus.confusion( allyData );
					
					//処理結果をサポートクラスから取得
					this.resultStatus();
					
					break;
    		}
			
			//MP消費処理
			if( !isMpEmpty ) {
				allyData = ConsumptionMP.consumptionMP( allyData , magic , skill );
				partyMap.put( key , allyData );
			}
			
			//サポートクラスとデータを同期
			this.battleSupportStatus = new BattleSupportStatus( this );

			//行動終了後に作用する状態異常の処理
			battleSupportStatus.badStatusAfter( allyData, key );
			
			//行動終了後の状態異常の処理結果を取得
			this.resultStatus();
			
				
		//------------------敵側の処理------------------------
		}else if( monsterDataMap.get( key ) != null ){
			
			//サポートクラスとデータを同期
			this.battleSupportEnemy = new BattleSupportEnemy( this );
            
			//敵の行動を処理
			this.battleSupportEnemy.enemyAction( key , battleSupportStatus );
			
			//敵の行動結果を取得
    		this.resultEnemy();
    		
    		//サポートクラスとデータを同期
    		this.battleSupportStatus = new BattleSupportStatus( this );
			
    		//行動終了後の状態異常を処理
    		battleSupportStatus.badStatusAfter( monsterDataMap.get( key ) , key );
    		
    		//行動終了後の状態異常の結果を取得
    		this.resultStatus();
    	}
	}
	
	//-----------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------戦闘処理の結果を格納するメソッド群--------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------
	
	//攻撃の結果取得メソッド
	public void result() {
		this.targetSetAlly  = battleSupportAttack.getTargetSetAlly();
		this.targetSetEnemy = battleSupportAttack.getTargetSetEnemy();
		this.monsterDataMap = battleSupportAttack.getMonsterDataMap();
		this.mesageList     = battleSupportAttack.getMesageList();
		this.targetMap      = battleSupportAttack.getTargetMap();
		this.enemyNameList  = battleSupportAttack.getEnemyNameList();
		this.partyMap		= battleSupportAttack.getPartyMap();
	}
	
	
	//回復補助蘇生の結果取得メソッド
	public void resultRecovery() {
		this.targetSetAlly  = battleSupportRecovery.getTargetSetAlly();
		this.targetSetEnemy = battleSupportRecovery.getTargetSetEnemy();
		this.monsterDataMap = battleSupportRecovery.getMonsterDataMap();
		this.mesageList     = battleSupportRecovery.getMesageList();
		this.targetMap      = battleSupportRecovery.getTargetMap();
		this.enemyNameList  = battleSupportRecovery.getEnemyNameList();
		this.partyMap		= battleSupportRecovery.getPartyMap();
	}
	
	
	//状態異常の結果取得メソッド
	public void resultStatus() {
		this.targetSetAlly  = battleSupportStatus.getTargetSetAlly();
		this.targetSetEnemy = battleSupportStatus.getTargetSetEnemy();
		this.monsterDataMap = battleSupportStatus.getMonsterDataMap();
		this.targetMap      = battleSupportStatus.getTargetMap();
		this.enemyNameList  = battleSupportStatus.getEnemyNameList();
		this.partyMap		= battleSupportStatus.getPartyMap();
		
		for( String message : battleSupportStatus.getMesageList() ) {
			this.mesageList.add( message );
		}
	}
	
	
	//敵の行動結果を取得するメソッド
	public void resultEnemy() {
		this.targetSetAlly  = battleSupportEnemy.getTargetSetAlly();
		this.targetSetEnemy = battleSupportEnemy.getTargetSetEnemy();
		this.monsterDataMap = battleSupportEnemy.getMonsterDataMap();
		this.mesageList     = battleSupportEnemy.getMesageList();
		this.targetMap      = battleSupportEnemy.getTargetMap();
		this.enemyNameList  = battleSupportEnemy.getEnemyNameList();
		this.partyMap		= battleSupportEnemy.getPartyMap();
	}
	
	//-----------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------ターンの開始と終了処理のメソッド群--------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------
	
	//ターンスタート時の処理
	public void startSkill() {
		
		for( int index : targetSetAlly ) {
			
			Target   target   = targetMap.get( index );
			AllyData allyData = partyMap.get( index );
			
			//防御の発動処理
			if( target.getSkillName().equals( "防御" )) {
				allyData = ChoiceDefense.choiceDefense( allyData );
				partyMap.put( index , allyData );
			}
			
			//スタートスキルを所持しつつ行動不能系の状態異常がなければ続行
			if( IsStartSkillStop.isStartSkillStop( allyData ) && allyData.getTurnStartSkillSet() != null) {
				AllyData allyData2 = partyMap.get( index ); //実質的にファイナルとするため再初期化
				allyData.getTurnStartSkillSet().stream()
				.map( s -> SortingStartSkill.sortingSkill( s ))
				.map( s -> s.action( allyData2 ) )
				.peek( s -> partyMap.put( index , allyData2 ))
				.filter( s -> s.getStartSkillMessage() != null )
				.peek( s -> this.mesageList.add( s.getStartSkillMessage() ))
				.forEach( s -> s.setStartSkillMessage( null ));
			}
		}
	}
	
	
	//ターンエンド時の処理
	public void endSkill() {
		
		for( int index : targetSetAlly ) {
			
			//防御状態の解除（行動不能でも実行）
			AllyData allyData = partyMap.get( index );
			allyData = CancelDefense.cancelDefense( allyData );
			partyMap.put( index , allyData );
			
			//エンドスキルを所持しつつ行動不能系の状態異常がなければ続行
			if( IsEndSkillStop.isEndSkillStop( allyData ) && allyData.getTurnEndSkillSet() != null ) {
				AllyData allyData2 = partyMap.get( index ); //実質的にファイナルとするため再初期化
				allyData2.getTurnEndSkillSet().stream()
				.map( s -> SortingEndSkill.sortingSkill( s ))
				.map( s -> s.action( allyData2 ) )
				.peek( s -> partyMap.put( index , allyData2 ))
				.filter( s -> s.getEndSkillMessage() != null )
				.peek( s -> this.mesageList.add( s.getEndSkillMessage() ))
				.forEach( s -> s.setEndSkillMessage( null ));
			}
		}
	}
	
}
