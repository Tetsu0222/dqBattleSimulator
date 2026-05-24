package com.example.rpg2.domain;

import java.util.List;
import java.util.Set;

public record EnemyBuildResult( Set<MonsterData> monsterDataSet , List<String> nameListEnemy ) {
}
