package com.example.rpg2.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.battle.Battle;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.repository.MagicRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MagicController {
	
	private final MagicRepository magicRepository;
	private final HttpSession	  session;
	
	//行動する側の情報を管理
	private Integer myKeys;
	private Magic   magic;
	

	//すべての魔法の選択画面を表示
	@GetMapping( "/magic/{key}" )
	public ModelAndView magic( @PathVariable( name = "key" ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		
		myKeys = key;
		
		//発動可能な魔法一覧を表示
		List<Magic> magicList = battle.getPartyMap().get( key ).getMagicList();
		mv.addObject( "magicList" , magicList );
		mv.addObject( "key" , myKeys );
		session.setAttribute( "mode" , "magic" );
		
		System.out.println( magicList.get(0).getName() );
		
		return mv;
		
	}
	
	
	//攻撃魔法の選択画面を表示
	@GetMapping( "/magic/attack/{key}" )
	public ModelAndView magicA( @PathVariable( name = "key" ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		
		myKeys = key;
		
		//発動可能な魔法一覧を表示
		List<Magic> magicList = battle.getPartyMap().get( key ).getMagicList();
		List<Magic> magicListA = magicList.stream()
				.filter( s -> s.getCategory().equals( "targetenemy" ))
				.filter( s -> s.getBuffcategory().equals( "no" ) )
				.collect( Collectors.toList() );

		mv.addObject( "magicList" , magicListA );
		mv.addObject( "key" , myKeys );
		session.setAttribute( "mode" , "magic" );
		
		return mv;
		
	}
	
	//回復魔法の選択画面を表示
	@GetMapping( "/magic/recovery/{key}" )
	public ModelAndView magicR( @PathVariable( name = "key" ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		
		myKeys = key;
		
		//発動可能な魔法一覧を表示
		List<Magic> magicList = battle.getPartyMap().get( key ).getMagicList();
		List<Magic> magicListR = magicList.stream()
				.filter( s -> s.getCategory().equals( "targetally" ) || s.getCategory().equals( "resuscitationmagic" ) )
				.filter( s -> s.getBuffcategory().equals( "no" ) )
				.collect( Collectors.toList() );

		mv.addObject( "magicList" , magicListR );
		mv.addObject( "key" , myKeys );
		session.setAttribute( "mode" , "magic" );
		
		return mv;
		
	}
	
	//補助魔法の選択画面を表示
	@GetMapping( "/magic/buff/{key}" )
	public ModelAndView magicB( @PathVariable( name = "key" ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		
		myKeys = key;
		
		//発動可能な魔法一覧を表示
		List<Magic> magicList = battle.getPartyMap().get( key ).getMagicList();
		List<Magic> magicListB = magicList.stream()
				.filter( s -> s.getCategory().equals( "targetally" ) )
				.filter( s -> !s.getBuffcategory().equals( "no" ) )
				.collect( Collectors.toList() );

		mv.addObject( "magicList" , magicListB );
		mv.addObject( "key" , myKeys );
		session.setAttribute( "mode" , "magic" );
		
		return mv;
		
	}
	
	
	//妨害魔法の選択画面を表示
	@GetMapping( "/magic/debuff/{key}" )
	public ModelAndView magicD( @PathVariable( name = "key" ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		
		myKeys = key;
		
		//発動可能な魔法一覧を表示
		List<Magic> magicList = battle.getPartyMap().get( key ).getMagicList();
		List<Magic> magicListA = magicList.stream()
				.filter( s -> s.getCategory().equals( "targetenemy" ))
				.filter( s -> !s.getBuffcategory().equals( "no" ) )
				.collect( Collectors.toList() );

		mv.addObject( "magicList" , magicListA );
		mv.addObject( "key" , myKeys );
		session.setAttribute( "mode" , "magic" );
		
		return mv;
		
	}
	
	
	//魔法を選択
	@GetMapping( "/magic/add/{id}" )
	public ModelAndView magic2( @PathVariable( name = "id" ) int id , 
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		magic = magicRepository.findById( id ).get();
		
		
		//単体が対象の魔法を選択
		if( magic.getRange().equals( "single" ) ) {
			
			switch( magic.getCategory() ) {
			
				//味方が対象の魔法
				case "targetally":
					session.setAttribute( "mode" , "targetAllyMagic" );
					break;
				
				//敵が対象の魔法
				case "targetenemy":
					session.setAttribute( "mode" , "targetMonsterMagic" );
					break;
				
				//蘇生魔法
				case "resuscitationmagic":
					session.setAttribute( "mode" , "targetDeathAllyMagic" );
					break;
			}
			
			//選択した内容をオブジェクトに保存
			session.setAttribute( "battle" , battle );
			
			return mv;
			
		}
		
		
		//グループが対象の魔法（敵のみが対象
		if( magic.getRange().equals( "group" ) ) {
			
			session.setAttribute( "mode" , "targetGroupNameMonsterMagic" );
			
			//選択した内容をオブジェクトに保存
			session.setAttribute( "battle" , battle );
			
			return mv;
		}
		
		
		//対象が全体の魔法
		if( magic.getRange().equals( "whole" )) {
			
			switch( magic.getCategory() ) {
			
				//味方全体が対象の魔法
				case "targetally":
					battle.selectionAllyMagic( myKeys , magic );
					session.setAttribute( "mode" , "log" );
					
					break;
				
				//敵全体の魔法
				case "targetenemy":
					battle.selectionMonsterMagic( myKeys , magic );
					session.setAttribute( "mode" , "log" );	
					
					break;
					
				//全体蘇生魔法
				case "resuscitationmagic":
					battle.selectionAllyMagic( myKeys , magic );
					session.setAttribute( "mode" , "log" );
					break;
			}
					
			//選択した内容をオブジェクトに保存
			session.setAttribute( "battle" , battle );

			return mv;
			
		}

		return mv;
	}
	
	
	//ターゲット選択(味方への魔法）
	@GetMapping( "/target/magic/ally/{key}" )
	public ModelAndView targetAlly( @PathVariable( name = "key" ) int key ,
									ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		battle.selectionAllyMagic( myKeys , key , magic );
		
		session.setAttribute( "battle" , battle );
		session.setAttribute( "mode" , "log" );
		
		return mv;
	}
	
	
	//ターゲット選択(攻撃魔法)
	@GetMapping( "/target/magic/monster/{key}" )
	public ModelAndView magicTargetMonster( @PathVariable( name = "key" ) int key ,
											ModelAndView mv ) {

		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		battle.selectionMonsterMagic( myKeys , key , magic );
		
		session.setAttribute( "battle" , battle );
		session.setAttribute( "mode" , "log" );
		
		return mv;
	}
	
	
	//ターゲット選択(グループ攻撃魔法)
	@GetMapping( "/target/magic/monsterGroup/{name}" )
	public ModelAndView magicTargetMonsterGroup( @PathVariable( name = "name" ) String name ,
												 ModelAndView mv ) {

		mv.setViewName( "battle" );
		Battle battle = (Battle)session.getAttribute( "battle" );
		battle.selectionMonsterMagic( name , myKeys ,  magic );
		
		session.setAttribute( "battle" , battle );
		session.setAttribute( "mode" , "log" );
		
		return mv;
	}

}
