package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.GameEnded;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import com.op.ludo.util.DateTimeUtil;
import java.util.ArrayList;
import java.util.List;

public class GameEndHandler extends ActionHandler {

    public GameEndHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        List<AbstractAction> endGameActions = getEndGameActions(boardState);
        if (endGameActions.size() > 0) {
            output.addAll(endGameActions);
        } else if (next != null) {
            next.handleAction(boardState, action, output);
        }
    }

    List<AbstractAction> getEndGameActions(BoardState boardState) {
        List<AbstractAction> abstractActions = new ArrayList<>();
        if (boardState.hasGameFinished()) {
            GameEnded gameEnded = new GameEnded(getPlayerPositions(boardState));
            abstractActions.add(gameEnded);
            boardState.setEnded(true);
            boardState.setEndTime(DateTimeUtil.nowEpoch());
        }
        return abstractActions;
    }

    private List<GameEnded.PlayerPosition> getPlayerPositions(BoardState boardState) {
        List<PlayerState> playerStates = boardState.getPlayers();
        List<GameEnded.PlayerPosition> playerPositions = new ArrayList<>();
        for (PlayerState playerState : playerStates) {
            GameEnded.PlayerPosition playerPosition =
                    new GameEnded.PlayerPosition(
                            playerState.getPlayerId(),
                            playerState.getPlayerPosition(),
                            boardState.getBid());
            playerPositions.add(playerPosition);
        }
        return playerPositions;
    }
}
