package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.StoneMovePending;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.TimerService;
import com.op.ludo.util.DateTimeUtil;
import java.util.List;

public class TimerHandler extends ActionHandler {

    TimerService timerService;

    public TimerHandler(ActionHandler next, TimerService timerService) {
        super(next);
        this.timerService = timerService;
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        String playerId = getPendingActionPlayer(output);
        if (playerId == null) throw new IllegalStateException("no pending action found");
        Long actionTime = DateTimeUtil.nowEpoch();
        boardState.setLastActionTime(actionTime);
        timerService.scheduleActionCheck(boardState.getBoardId(), playerId, actionTime);
    }

    private String getPendingActionPlayer(List<AbstractAction> output) {
        for (AbstractAction action : output) {
            if (action instanceof DiceRollPending) {
                return ((DiceRollPending) action).getArgs().getPlayerId();
            } else if (action instanceof StoneMovePending) {
                return ((StoneMovePending) action).getArgs().getPlayerId();
            }
        }
        return null;
    }
}
