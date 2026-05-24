package com.example.rpg2.service.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.rpg2.action.Attack;
import com.example.rpg2.action.SortingAttackAction;
import com.example.rpg2.action.TaregetEnemyAction;
import com.example.rpg2.domain.AllyData;
import com.example.rpg2.domain.MonsterData;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BattleAttackService {

	private final BattleManagementService battleManagementService;

	private final Random random = new Random();

	//通常攻撃の処理
	public void normalAttack( Integer target , Integer key , Magic magic , Skill skill , AllyData allyData , BattleRecord battleRecord , BattleState battleState ) {

		//通常攻撃を生成
		TaregetEnemyAction at = new Attack( allyData );

		//通常攻撃を実施
		battleState.getMesageList().add( at.getStratMessage() );
		this.singleAttack( at , target , key , magic , skill , battleRecord , battleState );
	}


	//攻撃魔法か特技の処理
	public boolean magicOrSkillAttack( AllyData allyData , Magic magic , Skill skill , Integer target , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		boolean isMpEmpty = false;

		//行動用のオブジェクトと攻撃回数を生成(特技か魔法を判定して合致するものを生成)
		TaregetEnemyAction taregetEnemyAction = SortingAttackAction.sortingCreateAttackAction( allyData , magic , skill );

		//行動を宣言
		battleState.getMesageList().add( taregetEnemyAction.getStratMessage() );

		//MP判定 MPが足りないとtureが返る。
		if( taregetEnemyAction.isNotEnoughMp() ){
			battleState.getMesageList().add( taregetEnemyAction.getNotEnoughMpMessage() );
			isMpEmpty = true;

		//MP判定OK
		}else{

			Set<Integer> targetSetEnemy = battleState.getTargetSetEnemy();

			//魔法特技の攻撃回数分の処理
			for( int i = 0 ; i < SortingAttackAction.actions ; i++ ){

				//対象撃破時のターゲット自動変更のために再生成
				taregetEnemyAction = SortingAttackAction.sortingRegenerationAttackAction( allyData , magic , skill );

				//無差別攻撃
				if( SortingAttackAction.targetRandom ) {
					List<Integer> targetList = new ArrayList<Integer>( targetSetEnemy );

					//連続攻撃中に敵が全滅していた場合は、処理終了
					if( targetList.size() == 0 ) {
						break;
					}

					//ターゲットを無差別に選択、前述のif分で例外は発生しない。
					target = random.nextInt( targetList.size() ) + 4;
					this.singleAttack( taregetEnemyAction , target , key , magic , skill , battleRecord , battleState );

				//全体攻撃の処理
				}else if( battleState.getTargetMap().get( key ).getTargetSetEnemy() != null ) {
					this.generalAttack( taregetEnemyAction , key , battleRecord , battleState );

				//グループ攻撃の処理
				}else if( battleState.getTargetMap().get( key ).getGroupName() != null ) {
					this.groupAttack( taregetEnemyAction , key , magic , skill , battleRecord , battleState );

				//単体攻撃の処理
				}else{
					this.singleAttack( taregetEnemyAction , target , key , magic , skill , battleRecord , battleState );
				}
			}
		}

		return isMpEmpty;
	}

	//単体攻撃のメソッド
	public void singleAttack( TaregetEnemyAction taregetEnemyAction , Integer target , Integer key , Magic magic , Skill skill , BattleRecord battleRecord , BattleState battleState ) {

		Set<Integer> targetSetEnemy = battleState.getTargetSetEnemy();

		//攻撃対象のオブジェクトを取得
		MonsterData monsterData = battleRecord.monsterDataMap().get( target );

		//対象がターン中に死亡している場合は、別の生存対象へ処理対象を変更
		if( monsterData.getSurvival() == 0 ) {
			if( targetSetEnemy.isEmpty() ) {
				return; // 攻撃対象が全滅しているので処理終了
			}
			target = targetSetEnemy.stream().findAny().orElse( 0 );
			monsterData = battleRecord.monsterDataMap().get( target );
		}


		//攻撃処理と結果の格納
		monsterData = taregetEnemyAction.action( monsterData );
		battleRecord.monsterDataMap().put( target , monsterData );

		//ダメージがあれば表示に追加
		if( taregetEnemyAction.getDamageMessage() != null ) {
			battleState.getMesageList().add( taregetEnemyAction.getDamageMessage() );
		}

		//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
		if( taregetEnemyAction.getResultMessage() != null ) {
			battleState.getMesageList().add( taregetEnemyAction.getResultMessage() );
		}

		//攻撃で対象を倒した場合の処理
		if( monsterData.getCurrentHp() == 0 ) {

			//敵リストから対象を削除
			targetSetEnemy.remove( target );

			//敵が全滅していなければ、別対象へターゲットを変更しておく。
        	if( targetSetEnemy.size() != 0 ) {

        		//ターゲット座標を取得
        		target = targetSetEnemy.stream().findAny().orElseThrow();

        		//ターゲットを再設定
        		if( magic != null ) {
        			battleManagementService.selectionMonsterMagic( key , target , magic , battleRecord , battleState );

        		}else if( skill != null ) {
        			battleManagementService.selectionMonsterSkill( key , target , skill , battleRecord , battleState );

        		}else{
        			battleManagementService.selectionAttack( key , target , battleRecord , battleState );
        		}
        	}
		}
	}

	//グループ攻撃のメソッド
	public void groupAttack( TaregetEnemyAction taregetEnemyAction , Integer key , Magic magic , Skill skill , BattleRecord battleRecord , BattleState battleState ) {

		List<String> enemyNameList     = battleState.getEnemyNameList();
		Set<Integer> targetSetEnemy    = battleState.getTargetSetEnemy();

		//グループ攻撃の対象を取得
		String target = battleState.getTargetMap().get( key ).getGroupName();

		//対象グループが先に全滅していた場合は、対象者を変更
		if( !enemyNameList.contains( target )) {
			target = enemyNameList.get( 0 );
		}

		//実質的にfinalとするため再定義
		String mainTarget = target;

		//マップからエネミーキャラクターの一覧をリストとして取得
		List<MonsterData> targetList = new ArrayList<>( battleRecord.monsterDataMap().values() );

		//取得したリストから攻撃対象のオブジェクトのみを再抽出
		targetList = targetList.stream()
				.filter( s -> s.getOriginalName().equals( mainTarget ))
				.toList();

		//処理実行
		for( MonsterData monsterData : targetList) {

			//処理結果を取得
			monsterData = taregetEnemyAction.action( monsterData );

			//処理結果を格納
			battleRecord.monsterDataMap().put( monsterData.getEnemyId() , monsterData );

			//ダメージがあれば表示に追加
			if( taregetEnemyAction.getDamageMessage() != null ) {
				battleState.getMesageList().add( taregetEnemyAction.getDamageMessage() );
			}

			//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
			if( taregetEnemyAction.getResultMessage() != null ) {
				battleState.getMesageList().add( taregetEnemyAction.getResultMessage() );
			}

			//攻撃で対象を倒した場合の処理
			if( monsterData.getCurrentHp() == 0 ) {

				//敵リストから対象を削除
				targetSetEnemy.remove( monsterData.getEnemyId() );

				//グループ内の残存勢力をチェック
				targetList = new ArrayList<>( battleRecord.monsterDataMap().values() );
				targetList = targetList.stream().filter( s -> s.getOriginalName().equals( mainTarget )).toList();
				Long count = targetList.stream().filter( s -> s.getSurvival() > 0 ).count();


				//グループが全滅していれば、リストから対象者を削除
				if( count == 0 ) {
					enemyNameList = enemyNameList.stream()
							.filter( s -> !s.equals( mainTarget ))
							.toList();
					battleState.setEnemyNameList( enemyNameList );

					//ターゲットの自動変更
					if( targetSetEnemy.size() != 0 ) {

	        			//生存しているエネミーの名前を取得して再定義
	        			target = enemyNameList.get( 0 );

	        			//魔法攻撃の時のターゲット変更
		        		if( magic != null ) {
		        			battleManagementService.selectionMonsterMagic( target , key , magic , battleState );

		        		//特技
		        		}else if( skill != null ) {
		        			battleManagementService.selectionMonsterSkill( target , key , skill , battleState );

		        		//通常攻撃（未実装）
		        		}else{

		        		}
					}
				}
			}
		}
	}

	//全体攻撃
	public void generalAttack( TaregetEnemyAction taregetEnemyAction , Integer key , BattleRecord battleRecord , BattleState battleState ) {

		Set<Integer> targetSetEnemy = battleState.getTargetSetEnemy();

		//targetを敵全体へ変更
		for( Integer target : targetSetEnemy ) {

			//撃破メッセージを初期化
			taregetEnemyAction.setResultMessage();

			//処理結果後のデータを取得し格納
			MonsterData monsterData = taregetEnemyAction.action( battleRecord.monsterDataMap().get( target ) );
			battleRecord.monsterDataMap().put( target , monsterData );

			//ダメージがあれば表示に追加
			if( taregetEnemyAction.getDamageMessage() != null ) {
				battleState.getMesageList().add( taregetEnemyAction.getDamageMessage() );
			}

			//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
			if( taregetEnemyAction.getResultMessage() != null ) {
				battleState.getMesageList().add( taregetEnemyAction.getResultMessage() );
			}
		}

		//全体攻撃後、敵対象の生存チェック
		List<Integer> deathList = targetSetEnemy.stream()	//remove()の特性上、別リストへ置換
						.filter( s -> battleRecord.monsterDataMap().get( s ).getSurvival() == 0 )
						.collect( Collectors.toList() );

		//残存勢力のみに置換
		deathList.stream().forEach( s -> targetSetEnemy.remove( s ) );

		//グループ攻撃用のリストを再生成
		List<String> enemyNameList = targetSetEnemy.stream()
											.map( id -> battleRecord.monsterDataMap().get( id ) )
											.map( monsterData -> monsterData.getOriginalName() )
											.distinct()
											.toList();
		battleState.setEnemyNameList( enemyNameList );
	}
}
