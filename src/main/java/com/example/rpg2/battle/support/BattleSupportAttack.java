package com.example.rpg2.battle.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.rpg2.action.Attack;
import com.example.rpg2.action.SortingAttackAction;
import com.example.rpg2.action.TaregetEnemyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.battle.Target;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.entity.Skill;

import lombok.Data;


@Data
public class BattleSupportAttack {
	
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
	
	
	public BattleSupportAttack( Battle battle ) {
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
	
	//通常攻撃が選択された場合の事前処理
	public void selectionAttack( Integer myKeys , Integer key ) {
		Target target = new Target( monsterDataMap.get( key ) , myKeys , key );
		targetMap.put( myKeys , target );
	}
	
	//敵への魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Integer key , Magic magic ) {
		Target target = new Target( monsterDataMap.get( key ) , myKeys , key , magic );
		targetMap.put( myKeys , target );
	}
	
	//敵へのグループ攻撃魔法が選択された時の処理
	public void selectionMonsterMagic( String name , Integer myKeys , Magic magic ) {
		Target target = new Target( name , myKeys , magic );
		targetMap.put( myKeys , target );
	}
	
	//敵への全体魔法が選択された場合の事前処理
	public void selectionMonsterMagic( Integer myKeys , Magic magic ) {
		Target target = new Target( monsterDataMap , targetSetEnemy , myKeys ,  magic );
		targetMap.put( myKeys , target );
	}
	
	//敵への特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Integer key , Skill skill ) {
		Target target = new Target( monsterDataMap.get( key ) , myKeys , key , skill );
		targetMap.put( myKeys , target );
	}
	
	//敵へのグループ攻撃特技が選択された時の処理
	public void selectionMonsterSkill( String name , Integer myKeys , Skill skill ) {
		Target target = new Target( name , myKeys , skill );
		targetMap.put( myKeys , target );
	}
	
	//敵への全体特技が選択された場合の事前処理
	public void selectionMonsterSkill( Integer myKeys , Skill skill ) {
		Target target = new Target( monsterDataMap , targetSetEnemy , myKeys ,  skill );
		targetMap.put( myKeys , target );
	}
	
	//--------------------------------------------------------------------------------------
	
	
	//通常攻撃の処理
	public void normalAttack( Integer target , Integer key , Magic magic , Skill skill , AllyData allyData ) {
		
		//通常攻撃を生成
		TaregetEnemyAction at = new Attack( allyData );
			
		//通常攻撃を実施
		this.mesageList.add( at.getStratMessage() );
		this.singleAttack( at,  target , key , magic , skill );
	}
	
	
	//攻撃魔法か特技の処理
	public boolean magicOrSkillAttack( AllyData allyData , Magic magic , Skill skill , Integer  target , Integer key ) {
		
		boolean isMpEmpty = false;
		
		//行動用のオブジェクトと攻撃回数を生成(特技か魔法を判定して合致するものを生成)
		TaregetEnemyAction taregetEnemyAction = SortingAttackAction.sortingCreateAttackAction( allyData , magic , skill );
			
		//行動を宣言
		this.mesageList.add( taregetEnemyAction.getStratMessage() );
			
		//MP判定 MPが足りないとtureが返る。
		if( taregetEnemyAction.isNotEnoughMp() ){
			this.mesageList.add( taregetEnemyAction.getNotEnoughMpMessage() );
			isMpEmpty = true;
				
		//MP判定OK
		}else{
				
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
					this.singleAttack( taregetEnemyAction , target , key , magic , skill );
						
				//全体攻撃の処理
				}else if( targetMap.get( key ).getTargetSetEnemy() != null ) {
					this.generalAttack( taregetEnemyAction , key );
				
				//グループ攻撃の処理
				}else if( targetMap.get( key ).getGroupName() != null ) {
					this.groupAttack( taregetEnemyAction , key , magic , skill );

				//単体攻撃の処理
				}else{
					this.singleAttack( taregetEnemyAction , target , key , magic , skill );
				}
			}
		}
		
		return isMpEmpty;
	}
	
	
	
	//--------------------------------------------------------------------------------------
	
	//単体攻撃のメソッド
	public void singleAttack( TaregetEnemyAction taregetEnemyAction , Integer target , Integer key , Magic magic , Skill skill ) {
		
		//攻撃対象のオブジェクトを取得
		MonsterData monsterData = monsterDataMap.get( target );
		
		//対象がターン中に死亡している場合は、別の生存対象へ処理対象を変更
		if( monsterData.getSurvival() == 0 ) {
			//生存している敵エネミーセットから座標を取得
			target = targetSetEnemy.stream().findAny().orElse( 0 );
			monsterData = monsterDataMap.get( target );
		}
		
		//攻撃処理と結果の格納
		monsterData = taregetEnemyAction.action( monsterData );
		this.monsterDataMap.put( target , monsterData );
		
		//ダメージがあれば表示に追加
		if( taregetEnemyAction.getDamageMessage() != null ) {
			this.mesageList.add( taregetEnemyAction.getDamageMessage() );
		}
		
		//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
		if( taregetEnemyAction.getResultMessage() != null ) {
			this.mesageList.add( taregetEnemyAction.getResultMessage() );
		}
		
		//攻撃で対象を倒した場合の処理
		if( monsterData.getCurrentHp() == 0 ) {
			
			//敵リストから対象を削除
			this.targetSetEnemy.remove( target );
			
			//敵が全滅していなければ、別対象へターゲットを変更しておく。
        	if( targetSetEnemy.size() != 0 ) {
        		
        		//ターゲット座標を取得
        		target = targetSetEnemy.stream().findAny().orElseThrow();
        		
        		//ターゲットを再設定
        		if( magic != null ) {
        			this.selectionMonsterMagic( key , target , magic );
        			
        		}else if( skill != null ) {
        			this.selectionMonsterSkill( key , target , skill );
        			
        		}else{
        			this.selectionAttack( key , target );
        		}
        	}
		}
	}
	
	
	//グループ攻撃のメソッド
	public void groupAttack( TaregetEnemyAction taregetEnemyAction , Integer key , Magic magic , Skill skill ) {
		
		//グループ攻撃の対象を取得
		String target = targetMap.get( key ).getGroupName();
		
		//対象グループが先に全滅していた場合は、対象者を変更
		if( !enemyNameList.contains( target )) {
			target = enemyNameList.get( 0 );
		}
		
		//実質的にfinalとするため再定義
		String mainTarget = target;
		
		//マップからエネミーキャラクターの一覧をリストとして取得
		List<MonsterData> targetList = new ArrayList<>( monsterDataMap.values() );
		
		//取得したリストから攻撃対象のオブジェクトのみを再抽出
		targetList = targetList.stream()
				.filter( s -> s.getOriginalName().equals( mainTarget ))
				.toList();
		
		//処理実行
		for( MonsterData monsterData : targetList) {
			
			//処理結果を取得
			monsterData = taregetEnemyAction.action( monsterData );
			
			//処理結果を格納
			this.monsterDataMap.put( monsterData.getEnemyId() , monsterData );
			
			//ダメージがあれば表示に追加
			if( taregetEnemyAction.getDamageMessage() != null ) {
				this.mesageList.add( taregetEnemyAction.getDamageMessage() );
			}
			
			//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
			if( taregetEnemyAction.getResultMessage() != null ) {
				this.mesageList.add( taregetEnemyAction.getResultMessage() );
			}
			
			//攻撃で対象を倒した場合の処理
			if( monsterData.getCurrentHp() == 0 ) {
				
				//敵リストから対象を削除
				this.targetSetEnemy.remove( monsterData.getEnemyId() );
				
				//グループ内の残存勢力をチェック
				targetList = new ArrayList<>( monsterDataMap.values() );
				targetList = targetList.stream().filter( s -> s.getOriginalName().equals( mainTarget )).toList();
				Long count = targetList.stream().filter( s -> s.getSurvival() > 0 ).count();
				
				
				//グループが全滅していれば、リストから対象者を削除
				if( count == 0 ) {
					this.enemyNameList = enemyNameList.stream()
							.filter( s -> !s.equals( mainTarget ))
							.toList();
					
					//ターゲットの自動変更
					if( targetSetEnemy.size() != 0 ) {
						
	        			//生存しているエネミーの名前を取得して再定義
	        			target = enemyNameList.get( 0 );
	        			
	        			//魔法攻撃の時のターゲット変更
		        		if( magic != null ) {
		        			this.selectionMonsterMagic( target , key , magic );
		        			
		        		//特技
		        		}else if( skill != null ) {
		        			this.selectionMonsterSkill( target , key , skill );
		        		
		        		//通常攻撃（未実装）
		        		}else{

		        		}
					}
				}
			}
		}
	}
	
	
	//全体攻撃
	public void generalAttack( TaregetEnemyAction taregetEnemyAction , Integer key ) {
		
		//targetを敵全体へ変更
		for( Integer target : targetSetEnemy ) {
			
			//撃破メッセージを初期化
			taregetEnemyAction.setResultMessage();
			
			//処理結果後のデータを取得し格納
			MonsterData monsterData = taregetEnemyAction.action( monsterDataMap.get( target ) );
			this.monsterDataMap.put( target , monsterData );
			
			//ダメージがあれば表示に追加
			if( taregetEnemyAction.getDamageMessage() != null ) {
				this.mesageList.add( taregetEnemyAction.getDamageMessage() );
			}
			
			//状態異常が伴う場合か対象を倒した場合は、結果を表示に追加
			if( taregetEnemyAction.getResultMessage() != null ) {
				this.mesageList.add( taregetEnemyAction.getResultMessage() );
			}
		}
		
		//全体攻撃後、敵対象の生存チェック
		List<Integer> deathList = targetSetEnemy.stream()	//remove()の特性上、別リストへ置換
						.filter( s -> monsterDataMap.get( s ).getSurvival() == 0 )
						.collect( Collectors.toList() );
		
		//残存勢力のみに置換
		deathList.stream().forEach( s -> this.targetSetEnemy.remove( s ) );
		
		//グループ攻撃用のリストを再生成
		this.enemyNameList = targetSetEnemy.stream()
											.map( id -> monsterDataMap.get( id ) )
											.map( monsterData -> monsterData.getOriginalName() )
											.distinct()
											.toList();
		
	}

}
