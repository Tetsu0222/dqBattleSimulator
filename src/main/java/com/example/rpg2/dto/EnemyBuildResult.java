package com.example.rpg2.dto;

import java.util.List;
import java.util.Set;

import com.example.rpg2.battle.MonsterData;

public record EnemyBuildResult( Set<MonsterData> monsterDataSet , List<String> nameListEnemy ) {
}
