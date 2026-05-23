package com.example.rpg2.dto;

import java.util.List;
import java.util.Set;

import com.example.rpg2.battle.AllyData;

public record PartyBuildResult( Set<AllyData> partySet , List<String> nameList ) {
}
