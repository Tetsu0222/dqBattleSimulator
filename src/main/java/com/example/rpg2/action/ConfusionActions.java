package com.example.rpg2.action;

import java.util.Random;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;
import com.example.rpg2.process.Funeral;

public class ConfusionActions {
	
	public static String message;
	
	public static AllyData action( AllyData allyData , AllyData targetAllyData ,  Random random ) {
		
		message = null;
		
		//混乱中の行動、実質行動不能
		int actionConfusion = random.nextInt( 6 );
		
		if( actionConfusion == 0 ) {
			message = allyData.getName() + "は、お日様を浴びている";
		
		//味方への攻撃
		}else if( actionConfusion == 1 || actionConfusion == 5) {
			
			int damage = allyData.getCurrentATK() / 10;
			
			if( damage < 0 ) {
				damage = 0;
				message = allyData.getName() + "は" + targetAllyData.getName() + "に攻撃したがダメージを与えられない。";
			}
			
			int HP = targetAllyData.getCurrentHp();
			HP = HP - damage;
			
			if( HP < 0 ) {
				Funeral.execution( targetAllyData );
				message = allyData.getName() + "は" + targetAllyData.getName() + "に攻撃!! " + damage + "のダメージを与えた。 " + targetAllyData.getName() + "は死んでしまった…";
				
			}else{
				targetAllyData.setCurrentHp( HP );
				message = allyData.getName() + "は" + targetAllyData.getName() + "に攻撃!! " + damage + "のダメージを与えてしまった…";
			}
		
		//自分自身にダメージ
		}else if( actionConfusion == 2 ) {
			
			int damage = allyData.getCurrentATK() / 10;
			
			if( damage < 0 ) {
				damage = 0;
				message = allyData.getName() + "は勢いよく走り出し、激しく転倒したがダメージはないようだ";
			}
			
			int HP = allyData.getCurrentHp();
			HP = HP - damage;
			
			if( HP < 0 ) {
				Funeral.execution( allyData );
				message = allyData.getName() + "は勢いよく走り出し、激しく転倒した " + damage + "のダメージを受けた。" + allyData.getName() + "は死んでしまった…";
				
			}else{
				allyData.setCurrentHp( HP );
				message = allyData.getName() + "は勢いよく走り出し、激しく転倒した " + damage + "のダメージを受けた。";
			}
			
			targetAllyData = allyData;
		
		//実質行動不能
		}else if( actionConfusion == 3 ) {
			message = allyData.getName() + "は歌い始めた。「ラララ～♪ の ラ～♪」";
		
		//有益行動
		}else if( actionConfusion == 4 ) {
			
			int recovery = targetAllyData.getCurrentHp() / 5;
			int HP = targetAllyData.getCurrentHp();
			
			HP += recovery;
			
			if( targetAllyData.getMaxHP() < HP ) {
				HP = targetAllyData.getMaxHP();
			}
			
			targetAllyData.setCurrentHp( HP );
			message = allyData.getName() + "は地面を掘り始めた…!!何と!!地面から薬草が見つかり、それを近くの" 
			+ targetAllyData.getName() + "へ投げつけた!!" + targetAllyData.getName() + "は" + recovery + "回復した";
		
		}
		
		return targetAllyData;
	}

	
	public static MonsterData action( AllyData allyData , MonsterData monsterData ,  Random random ) {
		
		message = null;
		
		//混乱中の行動、実質行動不能
		int actionConfusion = random.nextInt( 6 );
		
		if( actionConfusion == 0 ) {
			message = allyData.getName() + "は、お日様を浴びている";
		
		//味方への攻撃
		}else if( actionConfusion == 1 || actionConfusion == 5 || actionConfusion == 2 ) {
			
			int damage = allyData.getCurrentATK() / 10;
			
			if( damage < 0 ) {
				damage = 0;
				message = allyData.getName() + "は" + monsterData.getName() + "に攻撃したがダメージを与えられない。";
			}
			
			int HP = monsterData.getCurrentHp();
			HP = HP - damage;
			
			if( HP < 0 ) {
				Funeral.execution( monsterData );
				message = allyData.getName() + "は" + monsterData.getName() + "に攻撃!! " + damage + "のダメージを与えた!!  " + monsterData.getName() + "を倒した!!";
				
			}else{
				monsterData.setCurrentHp( HP );
				message = allyData.getName() + "は" + monsterData.getName() + "に攻撃!! " + damage + "のダメージを与えた!!";
			}
		
		//実質行動不能
		}else if( actionConfusion == 3 ) {
			message = allyData.getName() + "は歌い始めた。「ラララ～♪ の ラ～♪」";
		
		//有害行動
		}else if( actionConfusion == 4 ) {
			
			int recovery = monsterData.getCurrentHp() / 100;
			int HP = monsterData.getCurrentHp();
			
			HP += recovery;
			
			if( monsterData.getMaxHP() < HP ) {
				HP = monsterData.getMaxHP();
			}
			
			monsterData.setCurrentHp( HP );
			message = allyData.getName() + "は地面を掘り始めた…!!何と!!地面から薬草が見つかり、それを近くの" 
			+ monsterData.getName() + "へ投げつけた!!" + monsterData.getName() + "は" + recovery + "回復した";
		
		}
		
		return monsterData;
	}
}
