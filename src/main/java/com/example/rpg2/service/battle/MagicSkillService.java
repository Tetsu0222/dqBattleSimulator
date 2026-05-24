package com.example.rpg2.service.battle;

import java.util.List;

import org.springframework.stereotype.Service;
import com.example.rpg2.domain.MagicSkillType;
import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.entity.Castable;

@Service
public class MagicSkillService {

    @SuppressWarnings("unchecked")
    public <T> List<T> getMagicOrSkillList(MagicSkillType type , BattleRecord battleRecord, int myKey) {
        switch (type) {
            case MAGIC -> {
                return (List<T>) battleRecord.partyMap().get(myKey).getMagicList();
            }
            case SKILL -> {
                return (List<T>) battleRecord.partyMap().get(myKey).getSkillList();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAttackMagicOrSkillList(MagicSkillType type , BattleRecord battleRecord, int myKey) {
        List<? extends Castable> source = getMagicOrSkillList(type, battleRecord, myKey);
        return (List<T>) source.stream()
            .filter(s -> s.getCategory().equals("targetenemy"))
            .filter(s -> s.getBuffcategory().equals("no"))
            .toList();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getRecoveryMagicOrSkillList(MagicSkillType type , BattleRecord battleRecord, int myKey) {
        List<? extends Castable> source = getMagicOrSkillList(type, battleRecord, myKey);
        return (List<T>) source.stream()
                .filter(s -> s.getCategory().equals("targetally") || s.getCategory().equals("resuscitationmagic"))
                .filter(s -> s.getBuffcategory().equals("no"))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getBuffMagicOrSkillList(MagicSkillType type , BattleRecord battleRecord, int myKey) {
        List<? extends Castable> source = getMagicOrSkillList(type, battleRecord, myKey);
        return (List<T>) source.stream()
                .filter(s -> s.getCategory().equals("targetally"))
                .filter(s -> !s.getBuffcategory().equals("no"))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getDebuffMagicOrSkillList(MagicSkillType type , BattleRecord battleRecord, int myKey) {
        List<? extends Castable> source = getMagicOrSkillList(type, battleRecord, myKey);
        return (List<T>) source.stream()
                .filter(s -> s.getCategory().equals("targetenemy"))
                .filter(s -> !s.getBuffcategory().equals("no"))
                .toList();
    }
}
