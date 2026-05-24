package com.example.rpg2.dto.request;

import java.util.List;
import java.util.stream.Stream;

public record BattleStartRequest(
    Integer PID1, Integer PID2, Integer PID3, Integer PID4,
    Integer MID1, Integer MID2, Integer MID3, Integer MID4
) {
    public List<Integer> partyIds() { 
        return Stream.of(PID1, PID2, PID3, PID4)
        .filter(s -> s != null && s > 0)
        .toList();
    }
    public List<Integer> enemyIds() { 
        return Stream.of(MID1, MID2, MID3, MID4)
        .filter(s -> s != null && s > 0)
        .toList();
     }
}