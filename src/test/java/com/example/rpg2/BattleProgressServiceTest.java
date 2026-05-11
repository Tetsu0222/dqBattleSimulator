package com.example.rpg2;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;

import com.example.rpg2.battle.Battle;
import com.example.rpg2.battle.BattleProgressService;

public class BattleProgressServiceTest {

    private BattleProgressService service;
    private Battle battle;

    @BeforeEach
    void setUp() {
        service = new BattleProgressService();
        battle = mock(Battle.class);
    }
}
