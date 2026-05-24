package com.example.rpg2.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.entity.Magic;
import com.example.rpg2.repository.MagicRepository;
import com.example.rpg2.service.battle.BattleManagementService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MagicController {

    private final MagicRepository magicRepository;
    private final BattleManagementService battleManagementService;

    // すべての魔法の選択画面を表示
    @GetMapping("/magic/{myKey}")
    public ModelAndView magic(@PathVariable int myKey,
                              ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な魔法一覧を表示
        List<Magic> magicList = battleRecord.partyMap().get(myKey).getMagicList();

        mv.addObject("magicList", magicList);
        mv.addObject("mykey", myKey);
        session.setAttribute("mode", "magic");
        return mv;
    }

    // 攻撃魔法の選択画面を表示
    @GetMapping("/magic/attack/{myKey}")
    public ModelAndView magicA(@PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な魔法一覧を表示
        List<Magic> magicList = battleRecord.partyMap().get(myKey).getMagicList();
        List<Magic> magicListA = magicList.stream()
                .filter(s -> s.getCategory().equals("targetenemy"))
                .filter(s -> s.getBuffcategory().equals("no"))
                .collect(Collectors.toList());

        mv.addObject("magicList", magicListA);
        mv.addObject("mykey", myKey);
        session.setAttribute("mode", "magic");
        return mv;
    }

    // 回復魔法の選択画面を表示
    @GetMapping("/magic/recovery/{myKey}")
    public ModelAndView magicR(@PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な魔法一覧を表示
        List<Magic> magicList = battleRecord.partyMap().get(myKey).getMagicList();
        List<Magic> magicListR = magicList.stream()
                .filter(s -> s.getCategory().equals("targetally") || s.getCategory().equals("resuscitationmagic"))
                .filter(s -> s.getBuffcategory().equals("no"))
                .collect(Collectors.toList());

        mv.addObject("magicList", magicListR);
        mv.addObject("mykey", myKey);
        session.setAttribute("mode", "magic");
        return mv;
    }

    // 補助魔法の選択画面を表示
    @GetMapping("/magic/buff/{myKey}")
    public ModelAndView magicB(@PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な魔法一覧を表示
        List<Magic> magicList = battleRecord.partyMap().get(myKey).getMagicList();
        List<Magic> magicListB = magicList.stream()
                .filter(s -> s.getCategory().equals("targetally"))
                .filter(s -> !s.getBuffcategory().equals("no"))
                .collect(Collectors.toList());

        mv.addObject("magicList", magicListB);
        mv.addObject("mykey", myKey);
        session.setAttribute("mode", "magic");
        return mv;
    }

    // 妨害魔法の選択画面を表示
    @GetMapping("/magic/debuff/{myKey}")
    public ModelAndView magicD(@PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");

        // 発動可能な魔法一覧を表示
        List<Magic> magicList = battleRecord.partyMap().get(myKey).getMagicList();
        List<Magic> magicListA = magicList.stream()
                .filter(s -> s.getCategory().equals("targetenemy"))
                .filter(s -> !s.getBuffcategory().equals("no"))
                .collect(Collectors.toList());

        mv.addObject("magicList", magicListA);
        mv.addObject("mykey", myKey);
        session.setAttribute("mode", "magic");
        return mv;
    }

    // 魔法を選択
    @GetMapping("/magic/add/{id}/{myKey}")
    public ModelAndView magic2(@PathVariable int id,
                               @PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Magic magic = magicRepository.findById(id).get();

        // 単体が対象の魔法を選択
        if (magic.getRange().equals("single")) {
            switch (magic.getCategory()) {
                // 味方が対象の魔法
                case "targetally":
                    session.setAttribute("mode", "targetAllyMagic");
                    break;
                // 敵が対象の魔法
                case "targetenemy":
                    session.setAttribute("mode", "targetMonsterMagic");
                    break;
                // 蘇生魔法
                case "resuscitationmagic":
                    session.setAttribute("mode", "targetDeathAllyMagic");
                    break;
            }
            // 選択した内容をオブジェクトに保存
            session.setAttribute("magic", magic);
            return mv;
        }

        // グループが対象の魔法（敵のみが対象）
        if (magic.getRange().equals("group")) {
            session.setAttribute("mode", "targetGroupNameMonsterMagic");
            // 選択した内容をオブジェクトに保存
            session.setAttribute("magic", magic);
            return mv;
        }

        // 対象が全体の魔法
        if (magic.getRange().equals("whole")) {
            switch (magic.getCategory()) {
                // 味方全体が対象の魔法
                case "targetally":
                    battleManagementService.selectionAllyMagic(myKey, magic, battleRecord, battleState);
                    session.setAttribute("mode", "log");
                    break;
                // 敵全体の魔法
                case "targetenemy":
                    battleManagementService.selectionMonsterMagic(myKey, magic, battleRecord, battleState);
                    session.setAttribute("mode", "log");
                    break;
                // 全体蘇生魔法
                case "resuscitationmagic":
                    battleManagementService.selectionAllyMagic(myKey, magic, battleRecord, battleState);
                    session.setAttribute("mode", "log");
                    break;
            }
            // 選択した内容をオブジェクトに保存
            session.setAttribute("battleState", battleState);
            session.setAttribute("magic", magic);
            return mv;
        }
        return mv;
    }

    // ターゲット選択（味方への魔法）
    @GetMapping("/target/magic/ally/{myKey}/{targetKey}")
    public ModelAndView targetAlly(@PathVariable int myKey,
                                   @PathVariable int targetKey,
                                   ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Magic magic = (Magic) session.getAttribute("magic");

        battleManagementService.selectionAllyMagic(myKey, targetKey, magic, battleRecord, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }

    // ターゲット選択（攻撃魔法）
    @GetMapping("/target/magic/monster/{myKey}/{targetKey}")
    public ModelAndView magicTargetMonster(@PathVariable int myKey,
                                           @PathVariable int targetKey,
                                           ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleRecord battleRecord = (BattleRecord) session.getAttribute("battleRecord");
        BattleState  battleState  = (BattleState)  session.getAttribute("battleState");
        Magic magic = (Magic) session.getAttribute("magic");

        battleManagementService.selectionMonsterMagic(myKey, targetKey, magic, battleRecord, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }

    // ターゲット選択（グループ攻撃魔法）
    @GetMapping("/target/magic/monsterGroup/{name}/{myKey}")
    public ModelAndView magicTargetMonsterGroup(@PathVariable String name,
                                                @PathVariable int myKey,
                                                ModelAndView mv, HttpSession session) {
        mv.setViewName("battle");
        BattleState battleState = (BattleState) session.getAttribute("battleState");
        Magic magic = (Magic) session.getAttribute("magic");

        battleManagementService.selectionMonsterMagic(name, myKey, magic, battleState);

        session.setAttribute("battleState", battleState);
        session.setAttribute("mode", "log");
        return mv;
    }
}
