package com.op.ludo.game.handlers;

import static com.op.ludo.model.BoardState.getStoneBaseValue;
import static com.op.ludo.model.BoardState.isSafePosition;

import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;

public class StoneCutHandler extends ActionHandler {

    public StoneCutHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        StoneMove stoneMove = (StoneMove) action;
        if (!isSafePosition(stoneMove.getArgs().getFinalPosition())
                && getFinalPositionStoneCount(stoneMove, boardState) == 1) {
            StoneMove cutStoneMove = getFinalPositionCutStoneMove(stoneMove, boardState);
            output.add(cutStoneMove);
            boardState.updatePlayerStateWithNewPosition(
                    cutStoneMove.getArgs().getPlayerId(),
                    cutStoneMove.getArgs().getStoneNumber(),
                    cutStoneMove.getArgs().getFinalPosition());
        }
        if (next != null) next.handleAction(boardState, stoneMove, output);
    }

    private StoneMove getFinalPositionCutStoneMove(StoneMove stoneMove, BoardState boardState) {
        List<PlayerState> playerStates = boardState.getPlayers();
        for (PlayerState playerState : playerStates) {
            if (playerState.getStone1().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        1,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 1));
            }
            if (playerState.getStone2().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        2,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 2));
            }
            if (playerState.getStone3().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        3,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 3));
            }
            if (playerState.getStone4().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        4,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 4));
            }
        }
        throw new InvalidPlayerMoveException("Invalid move.");
    }

    private Integer getFinalPositionStoneCount(StoneMove stoneMove, BoardState boardState) {
        List<PlayerState> playerStates = boardState.getPlayers();
        Integer count = 0;
        for (PlayerState playerState : playerStates) {
            if (playerState.getPlayerId().equals(stoneMove.getArgs().getPlayerId())) {
                continue;
            }
            if (playerState.getStone1().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone2().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone3().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone4().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
        }
        return count;
    }
}
