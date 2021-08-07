package com.op.ludo.game.handlers;

import static com.op.ludo.model.BoardState.getNewStonePosition;

import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;

public class StoneMoveHandler extends ActionHandler {

    public StoneMoveHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        StoneMove stoneMove = (StoneMove) action;
        if (!isStoneMoveValid(stoneMove, boardState)) {
            throw new InvalidPlayerMoveException(
                    "Move is not valid for stone=" + stoneMove.getArgs().getStoneNumber());
        } else {
            output.add(stoneMove);
            boardState.updatePlayerStateWithNewPosition(
                    stoneMove.getArgs().getPlayerId(),
                    stoneMove.getArgs().getStoneNumber(),
                    stoneMove.getArgs().getFinalPosition());
            if (next != null) next.handleAction(boardState, action, output);
        }
    }

    public Boolean isStoneMoveValid(StoneMove stoneMove, BoardState boardState)
            throws InvalidPlayerMoveException {
        if (!boardState.isPlayerActive(stoneMove.getArgs().getPlayerId())) {
            throw new InvalidPlayerMoveException("Player is not actively part of the game.");
        } else if (stoneMove.getArgs().getStoneNumber() < 1
                || stoneMove.getArgs().getStoneNumber() > 4) {
            throw new InvalidPlayerMoveException("Invalid stone number.");
        }
        PlayerState playerState = boardState.getPlayerState(stoneMove.getArgs().getPlayerId());
        Integer currentDBPosition =
                getDatabaseStonePosition(playerState, stoneMove.getArgs().getStoneNumber());
        if (!playerState.getPlayerId().equals(boardState.getWhoseTurn())
                || !boardState.isMovePending()
                || !boardState.getBoardId().equals(stoneMove.getArgs().getBoardId())) {
            throw new InvalidPlayerMoveException("Turn not valid.");
        } else if (!currentDBPosition.equals(stoneMove.getArgs().getInitialPosition())) {
            throw new InvalidPlayerMoveException("Invalid initial position");
        }
        return getNewStonePosition(
                        currentDBPosition,
                        boardState.getLastDiceRoll(),
                        playerState.getPlayerNumber())
                .equals(stoneMove.getArgs().getFinalPosition());
    }

    private Integer getDatabaseStonePosition(PlayerState playerState, Integer stoneNumber) {
        if (stoneNumber == 1) {
            return playerState.getStone1();
        } else if (stoneNumber == 2) {
            return playerState.getStone2();
        } else if (stoneNumber == 3) {
            return playerState.getStone3();
        }
        return playerState.getStone4();
    }
}
