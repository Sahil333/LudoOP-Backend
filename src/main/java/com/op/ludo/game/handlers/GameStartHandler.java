package com.op.ludo.game.handlers;

import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.GameStarted;
import com.op.ludo.model.BoardState;
import com.op.ludo.util.DateTimeUtil;
import java.util.List;

public class GameStartHandler extends ActionHandler {

    public GameStartHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof GameStarted)) {
            throw new IllegalArgumentException("Only handles game started action");
        }
        GameStarted gameStarted = (GameStarted) action;

        if (boardState.canStartGame(gameStarted.getArgs().getByPlayer())) {
            doStartGame(boardState);
            output.add(gameStarted);

            boardState.setRollPending(true);
            boardState.setMovePending(false);
            AbstractAction playerTurn =
                    new DiceRollPending(boardState.getBoardId(), boardState.getWhoseTurn());
            output.add(playerTurn);
            if (next != null) next.handleAction(boardState, action, output);
        } else {
            throw new InvalidBoardRequest("Cannot start Game.");
        }
    }

    private void doStartGame(BoardState boardState) {
        Long startTime = DateTimeUtil.nowEpoch();
        boardState.setStartTime(startTime);
        boardState.setLastActionTime(startTime);
        boardState.setStarted(true);
    }
}
