package com.example.rpg2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.rpg2.battle.Target;

import lombok.Data;

//戦闘進行中に変化する可変DTO
@Data
public class BattleState {

	//プレイアブルメンバーの行動選択を管理
	private Map<Integer, Target> targetMap;

	//味方の数とキーを管理、戦闘不能・蘇生で変動
	private Set<Integer> targetSetAlly;

	//敵の数とキーを管理、撃破で変動
	private Set<Integer> targetSetEnemy;

	//表示するログを管理
	private List<String> mesageList = new ArrayList<>();

	//キーは敵味方混合、値は乱数補正後の素早さ。素早さ順で降順ソートしたリスト
	private List<Entry<Integer, Integer>> turnList;

	//味方が敵をグループ単位で攻撃する行動が存在するため、グループ全滅時に要素削除が発生する
	//対となる allyNameList は不変（BattleRecord 側）
	private List<String> enemyNameList;

	//経過ターン数
	private int turnCount = 1;
}
