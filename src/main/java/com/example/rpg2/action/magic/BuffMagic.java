package com.example.rpg2.action.magic;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.rpg2.action.TargetAllyAction;
import com.example.rpg2.battle.AllyData;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.status.HolyBarrier;
import com.example.rpg2.status.Normal;
import com.example.rpg2.status.Poison;
import com.example.rpg2.status.Status;

import lombok.Data;

@Data
public class BuffMagic implements TargetAllyAction{
	
	private Integer recovery;
	private String  stratMessage;
	private String  notEnoughMpMessage;
	private String  recoveryMessage;
	private AllyData allyData;
	private Magic    magic;
	private String resultMessage;
	
	
	//コンストラクタ 必要な情報を設定
	public BuffMagic( AllyData allyData , Magic magic ) {
		this.allyData = allyData;
		this.magic = magic;
		this.stratMessage =  allyData.getName() + "は" + magic.getName() + "を放った!!";
	}
	
	//MP判定
	public boolean isNotEnoughMp() {
		boolean check = magic.getMp() <= allyData.getCurrentMp();
		
		if( !check ) {
			this.notEnoughMpMessage = "しかしMPが足りない･･･" ;
		}
		
		return !check;
	}
	
	//正常からへバフ状態（期限付き）へ変化
	public Set<Status> goodStatus( AllyData receptionAllyData ){
		
		Set<Status> statusSet = receptionAllyData.getStatusSet()
				.stream()
				.filter( s -> !s.getName().equals( "正常" ) )
				.collect( Collectors.toSet() );
		
		return statusSet;
	}
	
	public AllyData action( AllyData receptionAllyData ) {
		
		double buffPoint = 0.0;
		Set<Status> statusSet = null;
		
		switch( magic.getBuffcategory() ) {
			
			//守備力のバフ
			case "DEF":
				double def = receptionAllyData.getCurrentDEF();
				
				//上昇上限チェック
				if( def >= receptionAllyData.getDefaultDEF() * 2 ) {
					this.resultMessage = receptionAllyData.getName() + "は、これ以上は守備力が上がらなかった･･･";
					break;
				}
				
				//上限未達
				buffPoint = magic.getPercentage();
				def = def * buffPoint;
					
				//補正値が上限を上回らないように再分岐
				if( def > receptionAllyData.getDefaultDEF() * 2 ) {
					def = receptionAllyData.getDefaultDEF() * 2 ;
				}
				
				//バフをキャラクターへ反映
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
				buffPoint = magic.getPercentage();
				atk = atk * buffPoint;
					
				//補正値が上限を上回らないように再分岐
				if( atk > receptionAllyData.getDefaultATK() * 2 ) {
					atk = receptionAllyData.getDefaultATK() * 2;
				}
				
				//バフをキャラクターへ反映
				receptionAllyData.setCurrentATK( (int) atk );
				
				//メッセージを設定
				this.resultMessage = receptionAllyData.getName() + "の攻撃力が大きく上がった!!";
				
				break;
				
				
			//聖なる守り	
			case "holybarrier":
				
				//対象の優性状態異常を取得
				statusSet = this.goodStatus( receptionAllyData );
				
				//聖なる守りを上書きで処理
				statusSet.add( new HolyBarrier( receptionAllyData ) );
				
				//バフをキャラクターへ反映
				receptionAllyData.setStatusSet( statusSet );
				receptionAllyData.setSurvival( 2 );
				
				//メッセージを設定
				this.resultMessage = receptionAllyData.getName() + "は聖なる守りに包まれる。";
				
				break;
				
			
			//毒治療
			case "poison":
				//対象キャラクターの状態異常を取得
				statusSet = receptionAllyData.getStatusSet();
				Poison poison = new Poison();
				
				//毒状態の有無で処理を分岐
				if( !statusSet.contains( poison )) {
					this.resultMessage = receptionAllyData.getName() + "に効果はなかった…";
					break;
				}
				
				//毒状態であれば治療処理
				statusSet.remove( poison );
				this.resultMessage = receptionAllyData.getName() + "の毒が治った!!";
					
				//状態異常が毒のみであった場合、ステータスを正常へ設定
				if( statusSet.size() == 0 ) {
					statusSet.add( new Normal() );
				}
				
				//キャラクターへ反映
				receptionAllyData.setStatusSet( statusSet );
				
				break;
				
			
			//キアリク
			case "tingle":
				this.resultMessage = receptionAllyData.getName() + "は暖かい光に包まれる";
				
				//対象キャラクターの状態異常を取得
				statusSet = receptionAllyData.getStatusSet();
				
				//セットを再変換して治癒対応
				statusSet = allyData.getStatusSet()
						.stream()
						.filter( s -> !s.getName().equals( "睡眠" ) )
						.filter( s -> !s.getName().equals( "麻痺" ) )
						.collect( Collectors.toSet() );
				
				//状態異常がすべて完治した場合、正常状態へと戻す。
				if( statusSet.size() == 0 ) {
					statusSet.add( new Normal() );
				}
				
				//キャラクターへ反映
				receptionAllyData.setStatusSet( statusSet );
				
				break;
		}
		
		return receptionAllyData;
	}
	
}
