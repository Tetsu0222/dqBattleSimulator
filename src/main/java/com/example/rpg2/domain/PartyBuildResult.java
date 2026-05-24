package com.example.rpg2.domain;

import java.util.List;
import java.util.Set;

public record PartyBuildResult( Set<AllyData> partySet , List<String> nameList ) {
}
