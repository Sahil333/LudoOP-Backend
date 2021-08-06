package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;

public class GamePositionHandler extends ActionHandler {

    public GamePositionHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        StoneMove stoneMove = (StoneMove) action;
        PlayerState playerState = boardState.getPlayerState(stoneMove.getArgs().getPlayerId());
        if (playerState.getHomeCount() == 4) {
            playerState.setPlayerPosition(boardState.getNextPlayerPosition());
        }
        if (next != null) next.handleAction(boardState, action, output);
    }
}
