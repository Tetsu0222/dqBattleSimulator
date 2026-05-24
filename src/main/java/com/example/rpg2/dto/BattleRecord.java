package com.example.rpg2.dto;

import java.util.List;
import java.util.Map;

import com.example.rpg2.battle.AllyData;
import com.example.rpg2.battle.MonsterData;

//戦闘の固定情報を保持する不変DTO
public record BattleRecord (
	Map<Integer,AllyData> partyMap            //プレイアブルメンバーを管理
	, Map<Integer,MonsterData> monsterDataMap //エネミーメンバーを管理
	//味方への単体・全体行動はあっても、味方が味方をグループ単位で対象に取る行動はゲーム仕様上存在しないため、味方名リストは不変として扱う
	, List<String> allyNameList               //グループ攻撃用の味方名リスト（不変）
){
}
