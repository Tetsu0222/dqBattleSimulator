package com.example.rpg2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.MonsterData;

import com.example.rpg2.entity.Ally;
import com.example.rpg2.entity.Monster;
import com.example.rpg2.process.CreateCharacterSet;
import com.example.rpg2.repository.AllyRepository;
import com.example.rpg2.repository.MonsterRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StartController {
	
	private final CreateCharacterSet createCharacterSet;
	private final AllyRepository allyRepository;
	private final MonsterRepository monsterRepository;
	private final HttpSession session;

	//定数
	private final String TopMenu = "index";
	private final String BattleScreen = "battle";
	private final String BattleObject = "battle";
	private final String PartyMember = "allyList";
	private final String EnemyMember = "enemyList";
	
	//行動する側の情報を管理
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
}