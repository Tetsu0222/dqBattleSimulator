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
public class DefenseController {

    // 定数
    private final String BattleScreen = "battle";
    private final String BattleObject = "battle";

    // 防御を選択
    @GetMapping("/defense/{myKey}")
    public ModelAndView defense(@PathVariable int myKey,
                                ModelAndView mv, HttpSession session) {
        mv.setViewName(BattleScreen);
        Battle battle = (Battle) session.getAttribute(BattleObject);

        battle.selectionDefense(myKey);

        session.setAttribute(BattleObject, battle);
        return mv;
    }
}
