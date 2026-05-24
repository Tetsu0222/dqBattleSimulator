package com.example.rpg2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.battle.Battle;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AttackController {

    // 定数
    private final String BattleScreen = "battle";
    private final String BattleObject = "battle";
    private final String ScreenMode   = "mode";
    private final String BeforeTurn   = "log";
    private final String NormalAttack = "attackTargetMonster";

    // 通常攻撃を選択
    @GetMapping("/attack/{myKey}")
    public ModelAndView attack(@PathVariable int myKey,
                               ModelAndView mv, HttpSession session) {
        mv.setViewName(BattleScreen);
        mv.addObject("myKey", myKey);
        session.setAttribute(ScreenMode, NormalAttack);
        return mv;
    }

    // 通常攻撃のターゲット選択（敵）
    @GetMapping("/target/attack/monster/{myKey}/{targetKey}")
    public ModelAndView attackTargetMonster(@PathVariable int myKey,
                                            @PathVariable int targetKey,
                                            ModelAndView mv, HttpSession session) {
        mv.setViewName(BattleScreen);
        // TODO:Battleクラスのリファクタリング後、呼び出しメソッドを変える。
        Battle battle = (Battle) session.getAttribute(BattleObject);
        battle.selectionAttack(myKey, targetKey);

        session.setAttribute(BattleObject, battle);
        session.setAttribute(ScreenMode,   BeforeTurn);
        return mv;
    }
}
