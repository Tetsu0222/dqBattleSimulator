package com.example.rpg2.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.repository.AllyRepository;
import com.example.rpg2.repository.MagicRepository;
import com.example.rpg2.repository.MonsterPatternRepository;
import com.example.rpg2.repository.MonsterRepository;
import com.example.rpg2.repository.SkillRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCharacterSet {
	
	@Getter
	public List<String> nameList = new ArrayList<>();
	
	@Getter
	public List<String> nameListEnemy = new ArrayList<>();
	
	
	public String abc[] = { "A" , "B" , "C" , "D" };
	
	private final MagicRepository magicRepository;
	private final AllyRepository allyRepository;
	private final MonsterRepository monsterRepository;
	private final MonsterPatternRepository monsterPatternRepository;
	private final SkillRepository skillRepository;
	
	
	//プレイアブルキャラクターを生成、名前の加工を行うメソッド
	public Set<AllyData> createPartySet( List<Integer> repositoryIdList ){
		
		//名前区別用のコレクション
		Set<String> duplicationNameSet = new HashSet<>();
		
		//生成プレイアブルキャラクターを格納するセット
		Set<AllyData> partySet = new HashSet<>();
		
		//プレイアブルキャラクターの生成
		for( int i = 0 ; i < repositoryIdList.size() ; i++ ) {
			Integer allyId = i;
			Integer repositoryId = repositoryIdList.get( i );
			AllyData allyData = new AllyData( allyRepository.findById( repositoryId ).orElseThrow() ,
					magicRepository ,
					skillRepository ,
					allyId );
			
			String name = allyData.getName();
			
			//重複している名前か確認
			int count = (int)nameList.stream()
					.filter( s -> s.equals( name ))
					.count();
			
			//重複している名前であればセットへ格納
			if( count > 0 ) {
				duplicationNameSet.add( name );
			}
			
			//重複した名前か確認するリストへnameを格納
			nameList.add( name );
			
			//プレイアブルキャラクターのセットへ一時格納
			partySet.add( allyData );
		}
		
		//重複している名前の加工処理（ABCを付与)
		for( String name : duplicationNameSet ) {
			
			//プレイアブルキャラクターのセットを名前が重複しているオブジェクトに絞ったリストへ変換
			List<AllyData> duplicationNameList = partySet.stream()
					.filter( s -> s.getName().equals( name ) )
					.toList();
			
			//名前の加工処理
			for( int i = 0 ; i < duplicationNameList.size() ; i++ ) {
				AllyData allyData = duplicationNameList.get( i );
				
				//加工後の名前を設定
				allyData.setName( name + abc[i] );
				
				//プレイアブルキャラクターのセットへ再格納
				partySet.add( allyData );
			}
		}
		
		return partySet;
	}
	
	
	
	//エネミーキャラクターの生成と名前の加工を行うメソッド
	public Set<MonsterData> createEnemySet( List<Integer> repositoryEnemyIdList ){
		
		
		//名前区別用のコレクション
		Set<String> duplicationEnemyNameSet = new HashSet<>();
		
		//生成したエネミーキャラクターを格納するセット
		Set<MonsterData> monsterDataSet = new HashSet<>();
		
		//エネミーキャラクターの生成
		for( int i = 4 ; i < repositoryEnemyIdList.size() + 4 ; i++ ) {
			Integer enemyId = i;
			Integer repositoryEnemyId = repositoryEnemyIdList.get( i - 4  );
			MonsterData monsterData = new MonsterData( monsterRepository.findById( repositoryEnemyId ).orElseThrow() ,
					monsterPatternRepository , enemyId );
			
			String name = monsterData.getName();
			monsterData.setOriginalName( name );
			
			//重複している名前か確認
			int count = (int)nameListEnemy.stream()
					.filter( s -> s.equals( name ))
					.count();
			
			//重複している名前であればセットへ格納
			if( count > 0 ) {
				duplicationEnemyNameSet.add( name );
			}
			
			//重複した名前か確認するリストへnameを格納
			nameListEnemy.add( name );
			
			//エネミーキャラクターのセットへ一時格納
			monsterDataSet.add( monsterData );
		}
		
		//重複している名前の加工処理（ABCを付与)
		for( String name : duplicationEnemyNameSet ) {
			
			//エネミーキャラクターのセットを名前が重複しているオブジェクトに絞ったリストへ変換
			List<MonsterData> duplicationEnemyNameList = monsterDataSet.stream()
					.filter( s -> s.getName().equals( name ) )
					.toList();
			
			//名前の加工処理
			for( int i = 0 ; i < duplicationEnemyNameList.size() ; i++ ) {
				MonsterData monsterData = duplicationEnemyNameList.get( i );
				
				//加工後の名前を設定
				monsterData.setName( name + abc[i] );
				
				//エネミーキャラクターのセットへ再格納
				monsterDataSet.add( monsterData );
			}
		}
		
		return monsterDataSet;
	}
	
	
	//フィールドを初期化するメソッド
	public void initialize() {
		nameList = new ArrayList<>();
		nameListEnemy = new ArrayList<>();
	}
	
	
	
}