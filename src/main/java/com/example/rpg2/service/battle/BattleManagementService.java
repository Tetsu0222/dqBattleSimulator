package com.example.rpg2.service.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.domain.Target;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.util.battle.TurnOrderCreate;

@Service
public class BattleManagementService  {

	// 不変な戦闘構成を生成
	public BattleRecord createBattleRecord( Set<AllyData> partySet , Set<MonsterData> monsterDataSet , List<String> allyNameList ) {

		List<AllyData>    partyList       = new ArrayList<>( partySet );
		List<MonsterData> monsterDataList = new ArrayList<>( monsterDataSet );

		//プレイアブルメンバーを生成
		Map<Integer,AllyData> partyMap = IntStream.range( 0 , partyList.size() )
							.boxed()
							.collect( Collectors.toMap( s -> s , s -> partyList.get( s ) ));

		//エネミーデータを生成
		Map<Integer,MonsterData> monsterDataMap = IntStream.range( 4 , monsterDataList.size() + 4 )
				.boxed()
				.collect( Collectors.toMap( s -> s , s -> monsterDataList.get( s - 4 ) ));

		return new BattleRecord( partyMap , monsterDataMap , allyNameList );
	}

	// 可変な戦闘進行状態を生成
	public BattleState createBattleState( BattleRecord battleRecord , List<String> enemyNameList ) {

		Map<Integer,AllyData>    partyMap       = battleRecord.partyMap();
		Map<Integer,MonsterData> monsterDataMap = battleRecord.monsterDataMap();

		//プレイアブルメンバーの初期行動を最初のエネミーへの通常攻撃で設定（例外対策）
		Map<Integer,Target> targetMap = partyMap.keySet().stream()
								.collect( Collectors.toMap( s -> s ,
										s -> new Target( monsterDataMap.get( 4 ) , s , 4 )));

		//味方と敵の座標Setをそれぞれ生成(各マップのキー数字とリンク）
		Set<Integer> targetSetEnemy = new TreeSet<>( monsterDataMap.keySet() );
		Set<Integer> targetSetAlly  = new TreeSet<>( partyMap.keySet() );

		BattleState battleState = new BattleState();
		battleState.setTargetMap( targetMap );
		battleState.setTargetSetAlly( targetSetAlly );
		battleState.setTargetSetEnemy( targetSetEnemy );
		battleState.setTurnList( TurnOrderCreate.create( partyMap , monsterDataMap , targetSetAlly , targetSetEnemy ) );
		battleState.setEnemyNameList( enemyNameList );
		return battleState;
	}

	//通常攻撃が選択された場合の事前処理
	public void selectionAttack( Integer myKeys , Integer key , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target( battleRecord.monsterDataMap().get( key ) , myKeys , key );
		battleState.getTargetMap().put( myKeys , target );
	}

	//味方への魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Integer key , Magic magic , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target ( battleRecord.partyMap().get( key ) , myKeys , key , magic );
		battleState.getTargetMap().put( myKeys , target );
	}

	//味方への全体魔法が選択された場合の事前処理
	public void selectionAllyMagic( Integer myKeys , Magic magic , BattleRecord battleRecord , BattleState battleState ) {
		//最後の引数はオーバーロード用のダミー
		Target target = new Target ( battleRecord.partyMap() , battleState.getTargetSetAlly() , myKeys , magic , 1 );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵への魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Integer key , Magic magic , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target( battleRecord.monsterDataMap().get( key ) , myKeys , key , magic );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵へのグループ攻撃魔法が選択された時の処理
	public void selectionMonsterMagic( String name , Integer myKeys , Magic magic , BattleState battleState ) {
		Target target = new Target( name , myKeys , magic );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵への全体魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Magic magic , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target( battleRecord.monsterDataMap() , battleState.getTargetSetEnemy() , myKeys , magic );
		battleState.getTargetMap().put( myKeys , target );
	}

	//味方への特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Integer key , Skill skill , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target ( battleRecord.partyMap().get( key ) , myKeys , key , skill );
		battleState.getTargetMap().put( myKeys , target );
	}

	//味方への全体特技が選択された場合の事前処理
	public void selectionAllySkill( Integer myKeys , Skill skill , BattleRecord battleRecord , BattleState battleState ) {
		//最後の引数はオーバーロード用のダミー
		Target target = new Target ( battleRecord.partyMap() , battleState.getTargetSetAlly() , myKeys , skill , 1 );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵への特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Integer key , Skill skill , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target( battleRecord.monsterDataMap().get( key ) , myKeys , key , skill );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵へのグループ攻撃特技が選択された時の処理
	public void selectionMonsterSkill( String name , Integer myKeys , Skill skill , BattleState battleState ) {
		Target target = new Target( name , myKeys , skill );
		battleState.getTargetMap().put( myKeys , target );
	}

	//敵への全体特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Skill skill , BattleRecord battleRecord , BattleState battleState ) {
		Target target = new Target( battleRecord.monsterDataMap() , battleState.getTargetSetEnemy() , myKeys , skill );
		battleState.getTargetMap().put( myKeys , target );
	}

	//防御を選択
	public void selectionDefense( Integer myKeys , BattleState battleState ) {
		Target target = new Target( myKeys , "防御" );
		battleState.getTargetMap().put( myKeys , target );
	}
}
