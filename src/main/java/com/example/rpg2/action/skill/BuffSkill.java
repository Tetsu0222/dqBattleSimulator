package com.example.rpg2.action.skill;

import java.util.Set;

import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.status.HolyBarrier;
import com.example.rpg2.status.Hubaha;
import com.example.rpg2.status.MagicBarrier;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Poison;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class BuffSkill implements TargetAllyAction{
	
	private Integer recovery;
	private String  stratMessage;
	private String  notEnoughMpMessage;
	private String  recoveryMessage;
	private AllyData allyData;
	private Skill skill;
	private String resultMessage;
	
	
	
	//コンストラクタ 必要な情報を設定
	public BuffSkill( AllyData allyData , Skill skill ) {
		this.allyData = allyData;
		this.skill = skill;
		this.stratMessage =  allyData.getName() + "は" + skill.getName() + "を放った!!";
	}
	
	//MP判定
	public boolean isNotEnoughMp() {
		boolean check = skill.getMp() <= allyData.getCurrentMp();
		
		if( !check ) {
			this.notEnoughMpMessage = "しかしMPが足りない･･･" ;
		}
		
		return !check;
	}
	
	//味方への補助魔法
		public AllyData action( AllyData receptionAllyData ) {
			
			double buffPoint = 0.0;
			Set<Status> statusSet = null;
			
			switch( skill.getBuffcategory() ) {
			
				//守備力のバフ
				case "DEF":
				
					double def = receptionAllyData.getCurrentDEF();
					
					//上昇上限チェック
					if( def >= receptionAllyData.getDefaultDEF() * 2 ) {
						this.resultMessage = receptionAllyData.getName() + "は、これ以上は守備力が上がらなかった･･･";
						break;
					}
					
					//上限未達
					buffPoint = skill.getPercentage();
					def = def * buffPoint;
						
					//補正値が上限を上回らないように再分岐
					if( def > receptionAllyData.getDefaultDEF() * 2 ) {
						def = receptionAllyData.getDefaultDEF() * 2 ;
					}
					
					//キャラクターへ反映
					receptionAllyData.setCurrentDEF( (int) def );
					
					//メッセージを設定
					this.resultMessage = receptionAllyData.getName() + "の守備力が上がった!!";
					
					break;
				
				//攻撃力のバフ
				case "ATK":
					double atk = receptionAllyData.getCurrentATK();
					
					//上昇上限チェック
					if( atk > receptionAllyData.getDefaultATK() * 2 ) {
						this.resultMessage = receptionAllyData.getName() + "は、これ以上は攻撃力が上がらなかった･･･";
						break;
					}
					//上限未達
					buffPoint = skill.getPercentage();
					atk = atk * buffPoint;
						
					//補正値が上限を上回らないように再分岐
					if( atk > receptionAllyData.getDefaultATK() * 2 ) {
						atk = receptionAllyData.getDefaultATK() * 2;
					}
					
					//キャラクターへ反映
					receptionAllyData.setCurrentDEF( (int) atk );
					
					//メッセージを設定
					this.resultMessage = receptionAllyData.getName() + "の攻撃力が大きく上がった!!";
					
					break;
				
				//聖なる守りを付与
				case "holybarrier":
					
					//対象者の状態を取得
					statusSet = this.goodStatus( receptionAllyData );
					
					//聖なる守りを上書きで付与
					statusSet.remove( new HolyBarrier() );
					statusSet.add( new HolyBarrier( receptionAllyData ) );
					
					//キャラクターへ反映
					receptionAllyData.setStatusSet( statusSet );
					receptionAllyData.setSurvival( 2 );
					
					//メッセージを設定
					this.resultMessage = receptionAllyData.getName() + "は聖なる守りに包まれる。";
					
					break;
				
					
				//マジックバリアを付与
				case "magicbarrier":
					statusSet = this.goodStatus( receptionAllyData );
					statusSet.remove( new MagicBarrier() );
					statusSet.add( new MagicBarrier( receptionAllyData ) );
					receptionAllyData.setStatusSet( statusSet );
					this.resultMessage = receptionAllyData.getName() + "は魔法に強くなった!!";
					
					break;
				
					
				//フバーハを付与
				case "fubaha":
					statusSet = this.goodStatus( receptionAllyData );
					statusSet.remove( new Hubaha() );
					statusSet.add( new Hubaha( receptionAllyData ) );
					receptionAllyData.setStatusSet( statusSet );
					this.resultMessage = receptionAllyData.getName() + "を優しい衣が包み込む!!";
					
					break;
					
				
				//毒を治す特技
				case "poison":
					
					//対象キャラクターの状態異常を取得
					statusSet = receptionAllyData.getStatusSet();
					
					//毒状態かチェック
					if( !statusSet.contains( new Poison() )) {
						this.resultMessage = receptionAllyData.getName() + "に効果はなかった…";
						break;
					}
					
					//毒以外にも状態異常を持っているかチェック
					int size = statusSet.size();
					
					//状態異常が毒のみ
					if( size == 1 ) {
						statusSet.clear();
						statusSet.add( new Normal() );
						
					//状態異常が毒以外にもある。
					}else if( size > 1 ) {
						statusSet.remove( new Poison() );
					}
					
					receptionAllyData.setStatusSet( statusSet );
					this.resultMessage = receptionAllyData.getName() + "の毒が治った!!";
					
					break;
			}
			
			return receptionAllyData;
		}
		
		
		//正常からへバフ状態（期限付き）へ変化
		public Set<Status> goodStatus( AllyData receptionAllyData ){
			
			Set<Status> statusSet = receptionAllyData.getStatusSet();
			statusSet.remove( new Normal() );
			
			return statusSet;
		}
		

}
