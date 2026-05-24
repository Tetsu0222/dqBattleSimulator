package com.example.rpg2.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.rpg2.dto.response.BattleRecord;
import com.example.rpg2.domain.BattleState;
import com.example.rpg2.service.battle.BattleService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BattleController {

    private final MessageSource messageSource;
    private final BattleService battleService;

    // 定数
    private final String BattleScreen    = "battle";
    private final String BattleRecordKey = "battleRecord";
    private final String BattleStateKey  = "battleState";
    private final String TurnProgression = "battle";
    private final String ScreenMode      = "mode";
    private final String TurnEnd         = "end";
    private final String BeforeTurn      = "log";
    private final String BattleResult    = "result";

    // ロケールが null の場合はデフォルトロケールを使用してメッセージを取得
    @SuppressWarnings("null")
    private String getMessage(String code, Locale locale) {
        Locale resolved = locale != null ? locale : Locale.getDefault();
        return messageSource.getMessage(code, null, resolved);
    }

    // 戦闘開始
    @GetMapping("/start")
    public ModelAndView start(ModelAndView mv, Locale locale, HttpSession session) {
        // いつもの処理
        mv.setViewName(BattleScreen);
        BattleRecord battleRecord = (BattleRecord) session.getAttribute(BattleRecordKey);
        BattleState  battleState  = (BattleState)  session.getAttribute(BattleStateKey);

        battleService.initializeBattle(battleRecord, battleState);

        battleState.getMesageList().add(battleState.getTurnCount() + getMessage("turn.start", locale));

        session.setAttribute(BattleStateKey, battleState);
        session.setAttribute(ScreenMode,    TurnProgression);
        return mv;
    }

    // 戦闘続行
    @GetMapping("/next")
    public ModelAndView next(ModelAndView mv, Locale locale, HttpSession session) {
        // いつもの処理
        mv.setViewName(BattleScreen);
        BattleRecord battleRecord = (BattleRecord) session.getAttribute(BattleRecordKey);
        BattleState  battleState  = (BattleState)  session.getAttribute(BattleStateKey);

        boolean isTurnEnd = battleService.judgeTurnEnd(battleState);

        // ターン終了判定
        if (isTurnEnd) {
            battleService.turnEnd(battleRecord, battleState);
            battleState.getMesageList().add(battleState.getTurnCount() + getMessage("turn.end", locale));
            session.setAttribute(BattleStateKey, battleState);
            session.setAttribute(ScreenMode,    TurnEnd);
            return mv;
        }

        // 行動可能者が残っているか判定
        boolean isPossible = battleService.judgePossible(battleRecord, battleState);

        if (isPossible) {
            // 判定結果trueであれば行動実行
            battleService.startBattleSetting(battleRecord, battleState);

            // 戦闘終了判定
            if (battleState.getTargetSetAlly().size() == 0) {
                battleState.getMesageList().add(getMessage("lose.message", locale));
                session.setAttribute(BattleStateKey, battleState);
                session.setAttribute(ScreenMode,    BattleResult);
            } else if (battleState.getTargetSetEnemy().size() == 0) {
                battleState.getMesageList().add(getMessage("win.message", locale));
                session.setAttribute(BattleStateKey, battleState);
                session.setAttribute(ScreenMode,    BattleResult);
            } else {
                session.setAttribute(BattleStateKey, battleState);
                session.setAttribute(ScreenMode,    TurnProgression);
            }
        // 全員の行動が終了
        } else {
            battleService.turnEnd(battleRecord, battleState);
            battleState.getMesageList().add(battleState.getTurnCount() + getMessage("turn.end", locale));
            session.setAttribute(BattleStateKey, battleState);
            session.setAttribute(ScreenMode,    TurnEnd);
        }
        return mv;
    }

    // ターン終了
    @GetMapping("/end")
    public ModelAndView end(ModelAndView mv, HttpSession session) {
        // いつもの処理
        mv.setViewName(BattleScreen);
        session.setAttribute(ScreenMode, BeforeTurn);
        return mv;
    }
}
