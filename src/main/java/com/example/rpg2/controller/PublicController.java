package com.example.rpg2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.BattleProgressService;
import com.example.rpg2.battle.MonsterData;

import com.example.rpg2.entity.Ally;
import com.example.rpg2.entity.Monster;
import com.example.rpg2.process.CreateCharacterSet;
import com.example.rpg2.process.TurnQueue;
import com.example.rpg2.repository.AllyRepository;
import com.example.rpg2.repository.MonsterRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PublicController {
	
	private final CreateCharacterSet createCharacterSet;
	private final AllyRepository allyRepository;
	private final MonsterRepository monsterRepository;
	private final HttpSession session;
	private final MessageSource messageSource;

	//定数
	private final String keys = "key";
	private final String TopMenu = "index";
	private final String BattleScreen = "battle";
	private final String BattleObject = "battle";
	private final String TurnProgression = "battle";
	private final String PartyMember = "allyList";
	private final String EnemyMember = "enemyList";
	private final String ScreenMode = "mode";
	private final String TurnEnd = "end";
	private final String BeforeTurn = "log";
	private final String BattleResult = "result";
	private final String NormalAttack = "attackTargetMonster";
	
	//行動する側の情報を管理
	private Integer myKeys;
	private Queue<Integer> turnqueue;
	private List<String> allyNameList = new ArrayList<>();
	private List<String> enemyNameList = new ArrayList<>();

	//TOP画面に対応
	@GetMapping( "/" )
	public ModelAndView Index( ModelAndView mv ) {

		mv.setViewName(TopMenu);

		//プレイアブルキャラクターとエネミーキャラクターの選択肢を提示
		List<Ally>    allyList    = allyRepository.findAll();
		List<Monster> monsterList = monsterRepository.findAll();

		mv.addObject( PartyMember , allyList    );
		mv.addObject( EnemyMember , monsterList );

		session.invalidate();
		createCharacterSet.initialize();

		return mv;
	}

	//バトルへ遷移
	@GetMapping( "/battle" )
	public ModelAndView battle( @RequestParam( name = "PLV1" ) Integer pid1 ,
								@RequestParam( name = "PLV2" ) Integer pid2 ,
								@RequestParam( name = "PLV3" ) Integer pid3 ,
								@RequestParam( name = "PLV4" ) Integer pid4 ,
								@RequestParam( name = "MLV1" ) Integer mid1 ,
								@RequestParam( name = "MLV2" ) Integer mid2 ,
								@RequestParam( name = "MLV3" ) Integer mid3 ,
								@RequestParam( name = "MLV4" ) Integer mid4 ,
								ModelAndView mv ) {
		
		mv.setViewName( BattleScreen );
			
		//選択に応じたプレイアブルキャラクターのIdを格納
		List<Integer> repositoryIdList = Stream.of( pid1 , pid2 , pid3 , pid4 )
				.filter( s -> s > 0 )
				.collect( Collectors.toList() );
		
		//生成プレイアブルキャラクターを格納するセットを生成
		Set<AllyData> partySet = createCharacterSet.createPartySet( repositoryIdList );
		
		//選択に応じたエネミーキャラクターのIdを格納
		List<Integer> repositoryEnemyIdList = Stream.of( mid1 , mid2 , mid3 , mid4 )
				.filter( s -> s > 0 )
				.collect( Collectors.toList() );
		
		//生成したエネミーキャラクターを格納するセットを生成
		Set<MonsterData> monsterDataSet = createCharacterSet.createEnemySet( repositoryEnemyIdList );
		
		//グループ攻撃用の重複要素を整理したリスト生成（順番を維持したいためリストにて生成）
		allyNameList  = createCharacterSet.getNameList().stream().distinct().toList();
		enemyNameList = createCharacterSet.getNameListEnemy().stream().distinct().toList();
	
		//戦闘処理用のオブジェクトを生成
		Battle battle = new Battle( partySet , monsterDataSet , allyNameList , enemyNameList );
		
		//戦闘処理をサポートするクラスを生成
		battle.createSupport();
		
		//戦闘画面用のデータをセッションスコープに保存
		session.setAttribute( BattleObject , battle );
		
		return mv;
	}

	//通常攻撃を選択
	@GetMapping( "/attack/{key}" )
	public ModelAndView attack( @PathVariable( name = keys ) int key ,
								ModelAndView mv ) {
		mv.setViewName( BattleScreen );
		myKeys = key;
		session.setAttribute( ScreenMode , NormalAttack );
		return mv;
		
	}

	//通常攻撃のターゲット選択(敵）
	@GetMapping( "/target/attack/monster/{key}" )
	public ModelAndView attackTargetMonster( @PathVariable( name = keys ) int key ,
											 ModelAndView mv ) {
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		battle.selectionAttack( myKeys , key );
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode , BeforeTurn );

		return mv;
	}

	//防御を選択
	@GetMapping( "/defense/{key}" )
	public ModelAndView defense( @PathVariable( name = keys ) int key ,
								 ModelAndView mv ) {
		
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		battle.selectionDefense( key );
		
		return mv;
	}

	//戦闘開始
	@GetMapping( "/start" )
	public ModelAndView start( ModelAndView mv , Locale locale ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		
		//前回までのログを消去
		battle.getMesageList().clear();
		
		//各キャラクターの行動順を規定
		battle.turn();
		
		//各キャラクターの座標を素早さが高い順（降順）でソートしたリストを取得
		List<Entry<Integer, Integer>> turnList = battle.getTurnList();
		
		//素早さで順でソートされたリストから、各キャラクターの座標だけ抽出してキューへ格納
		//このキューを用いて具体的な戦闘処理を実施する。
		this.turnqueue = TurnQueue.getTurnQueue( turnList );
		
		//ターンの最初に発動する効果を処理
		battle.startSkill();
		battle.getMesageList().add( battle.getTurnCount() + messageSource.getMessage( "turn.start" , null , locale ) );
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode   , TurnProgression );

		return mv;
	}

	//戦闘続行
	@GetMapping( "/next" )
	public ModelAndView next( ModelAndView mv , Locale locale ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
	
		//前回までのログを消去
		battle.getMesageList().clear();
		if( turnqueue.peek() == null ) {
			//ターン終了時に発動する処理
			battle.endSkill();
			battle.getMesageList().add( battle.getTurnCount() + messageSource.getMessage( "turn.end" , null , locale ) );
			battle.setTurnCount( battle.getTurnCount() + 1 );
			
			session.invalidate();
			session.setAttribute( BattleObject , battle );
			session.setAttribute( ScreenMode   , TurnEnd  );

			return mv;
		}

		//素早さ順に行動
		Integer actionObj = turnqueue.poll();
		BattleProgressService battleProgressService = new BattleProgressService();
		boolean possible = battleProgressService.turnAction( battle , locale , actionObj);

		//ターン終了判定
		if(possible){

			//判定結果trueであれば行動実行
			battle.startBattle( actionObj );
			
			//戦闘終了判定
			if( battle.getTargetSetAlly().size() == 0 ) {
				session.invalidate();
				battle.getMesageList().add( messageSource.getMessage( "lose.message" , null , locale ) );
				session.setAttribute( BattleObject , battle );
				session.setAttribute( ScreenMode , BattleResult );
			}else if( battle.getTargetSetEnemy().size() == 0 ) {
				session.invalidate();
				battle.getMesageList().add( messageSource.getMessage( "win.message" , null , locale ) );
				session.setAttribute( BattleObject , battle );
				session.setAttribute( ScreenMode , BattleResult );
			}else{
				session.invalidate();
				session.setAttribute( BattleObject , battle );
				session.setAttribute( ScreenMode , TurnProgression );
			}

		//全員の行動が終了
		}else{
			//ターン終了時に発動する処理
			battle.endSkill();
			battle.getMesageList().add( battle.getTurnCount() + messageSource.getMessage( "turn.end" , null , locale ) );
			battle.setTurnCount( battle.getTurnCount() + 1 );

			session.invalidate();
			session.setAttribute( BattleObject , battle );
			session.setAttribute( ScreenMode   , TurnEnd  );
		}
		return mv;
	}

	//ターン終了
	@GetMapping( "/end" )
	public ModelAndView end( ModelAndView mv ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		
		session.invalidate();
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode , BeforeTurn );
		
		return mv;
	}
}
