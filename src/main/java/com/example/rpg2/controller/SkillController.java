package com.example.rpg2.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.domain.MagicSkillType;
import com.example.rpg2.entity.Skill;
import com.example.rpg2.repository.SkillRepository;
import com.example.rpg2.service.battle.BattleManagementService;
import com.example.rpg2.service.battle.MagicSkillService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;
    private final BattleManagementService battleManagementService;
    private final MagicSkillService magicSkillService;

    // すべての特技の選択画面を表示
    @GetMapping("/skill/{myKey}")
    public ModelAndView skill(@PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な特技一覧を表示
        List<Skill> skillList = magicSkillService.getMagicOrSkillList(MagicSkillType.SKILL, battleRecord, myKey);

        mv.addObject("skillList", skillList);
        mv.addObject("myKey", myKey);
        session.setAttribute("mode", "skill");
        return mv;
    }

    // 攻撃特技の選択画面を表示
    @GetMapping("/skill/attack/{myKey}")
    public ModelAndView skillA(@PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な特技一覧を表示
        List<Skill> skillList = magicSkillService.getAttackMagicOrSkillList(MagicSkillType.SKILL, battleRecord, myKey);

        mv.addObject("skillList", skillList);
        mv.addObject("myKey", myKey);
        session.setAttribute("mode", "skill");
        return mv;
    }

    // 回復特技の選択画面を表示
    @GetMapping("/skill/recovery/{myKey}")
    public ModelAndView skillR(@PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な特技一覧を表示
        List<Skill> skillList = magicSkillService.getRecoveryMagicOrSkillList(MagicSkillType.SKILL, battleRecord, myKey);

        mv.addObject("skillList", skillList);
        mv.addObject("myKey", myKey);
        session.setAttribute("mode", "skill");
        return mv;
    }

    // 補助特技の選択画面を表示
    @GetMapping("/skill/buff/{myKey}")
    public ModelAndView skillB(@PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な特技一覧を表示
        List<Skill> skillList = magicSkillService.getBuffMagicOrSkillList(MagicSkillType.SKILL, battleRecord, myKey);

        mv.addObject("skillList", skillList);
        mv.addObject("myKey", myKey);
        session.setAttribute("mode", "skill");
        return mv;
    }

    // 妨害特技の選択画面を表示
    @GetMapping("/skill/debuff/{myKey}")
    public ModelAndView skillD(@PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な特技一覧を表示
        List<Skill> skillList = magicSkillService.getDebuffMagicOrSkillList(MagicSkillType.SKILL, battleRecord, myKey);

        mv.addObject("skillList", skillList);
        mv.addObject("myKey", myKey);
        session.setAttribute("mode", "skill");
        return mv;
    }

    // 特技を選択
    @GetMapping("/skill/add/{id}/{myKey}")
    public ModelAndView skill2(@PathVariable int id, @PathVariable int myKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Skill skill = skillRepository.findById(id).get();

        // 単体かつ攻撃と妨害以外→対象選択の範囲を味方に指定
        if (skill.getRange().equals("single") && skill.getCategory().equals("targetally")) {
            session.setAttribute("mode", "targetAllySkill");
            session.setAttribute("skill", skill);

        // 単体特技かつ攻撃・妨害特技→対象選択の範囲を敵に指定
        } else if (skill.getRange().equals("single") && skill.getCategory().equals("targetenemy")) {
            session.setAttribute("mode", "targetMonsterSkill");
            session.setAttribute("skill", skill);

        // グループ攻撃の魔法
        } else if (skill.getRange().equals("group") && skill.getCategory().equals("targetenemy")) {
            session.setAttribute("mode", "targetGroupNameMonsterSkill");
            session.setAttribute("skill", skill);

        // 味方全体への特技
        } else if (!skill.getRange().equals("single") && skill.getCategory().equals("targetally")) {
            battleManagementService.selectionAllySkill(myKey, skill, battleRecord, battleState);
            session.setAttribute("mode", "log");
            session.setAttribute("skill", skill);

        // 敵全体への特技
        } else if (!skill.getRange().equals("single") && skill.getCategory().equals("targetenemy")) {
            battleManagementService.selectionMonsterSkill(myKey, skill, battleRecord, battleState);
            session.setAttribute("mode", "log");
            session.setAttribute("skill", skill);

        // 蘇生特技
        } else {
            if (skill.getRange().equals("single")) {
                session.setAttribute("mode", "targetDeathAllySkill");
                session.setAttribute("skill", skill);
            }
            battleManagementService.selectionAllySkill(myKey, skill, battleRecord, battleState);
            session.setAttribute("mode", "log");
            session.setAttribute("skill", skill);
        }

        session.setAttribute("battleState", battleState);
        return mv;
    }

    // ターゲット選択（味方への特技）
    @GetMapping("/target/skill/ally/{myKey}/{targetKey}")
    public ModelAndView targetAlly(@PathVariable int myKey, @PathVariable int targetKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Skill skill = (Skill) session.getAttribute("skill");

        battleManagementService.selectionAllySkill(myKey, targetKey, skill, battleRecord, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }

    // ターゲット選択（攻撃特技）
    @GetMapping("/target/skill/monster/{myKey}/{targetKey}")
    public ModelAndView skillTargetMonster(@PathVariable int myKey, @PathVariable int targetKey,ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Skill skill = (Skill) session.getAttribute("skill");

        battleManagementService.selectionMonsterSkill(myKey, targetKey, skill, battleRecord, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }

    // ターゲット選択（グループ攻撃魔法）
    @GetMapping("/target/skill/monsterGroup/{name}/{myKey}")
    public ModelAndView magicTargetMonsterGroup(@PathVariable String name,
                                                @PathVariable int myKey,
                                                ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleState battleState = (BattleState) session.getAttribute("battleState");
        Skill skill = (Skill) session.getAttribute("skill");

        battleManagementService.selectionMonsterSkill(name, myKey, skill, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }
}
