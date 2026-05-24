package com.example.rpg2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.domain.BattleState;
import com.example.rpg2.service.battle.BattleManagementService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DefenseController {

    private final BattleManagementService battleManagementService;

    // 定数
    private final String BattleScreen    = "battle";
    private final String BattleStateKey  = "battleState";

    // 防御を選択
    @GetMapping("/defense/{myKey}")
    public ModelAndView defense(@PathVariable int myKey,
                                ModelAndView mv, HttpSession session) {
        mv.setViewName(BattleScreen);
        BattleState battleState = (BattleState) session.getAttribute(BattleStateKey);

        battleManagementService.selectionDefense(myKey, battleState);

        session.setAttribute(BattleStateKey, battleState);
        return mv;
    }
}
