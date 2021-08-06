package com.op.ludo.game.handlers;

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

    private Integer getNewStonePosition(
            Integer currentPosition, Integer diceRoll, Integer playerNumber)
            throws InvalidPlayerMoveException {
        if (!isStoneMovePossible(currentPosition, diceRoll)) {
            throw new InvalidPlayerMoveException("Player move not possible");
        }
        if (currentPosition < 0 && diceRoll == 6) {
            if (playerNumber == 1) {
                return 1;
            } else if (playerNumber == 2) {
                return 14;
            } else if (playerNumber == 3) {
                return 27;
            } else {
                return 40;
            }
        }
        if (currentPosition < 99) {
            if (playerNumber == 1) {
                if (currentPosition + diceRoll > 51) {
                    return 510 + currentPosition + diceRoll - 51;
                } else {
                    return currentPosition + diceRoll;
                }
            } else if (playerNumber == 2) {
                if (currentPosition <= 12 && diceRoll + currentPosition > 12) {
                    return 120 + currentPosition + diceRoll - 12;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            } else if (playerNumber == 3) {
                if (currentPosition <= 25 && diceRoll + currentPosition > 25) {
                    return 250 + currentPosition + diceRoll - 25;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            } else {
                if (currentPosition <= 38 && diceRoll + currentPosition > 38) {
                    return 380 + currentPosition + diceRoll - 38;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            }
        } else {
            return currentPosition + diceRoll;
        }
    }

    private Boolean isStoneMovePossible(Integer currentPosition, Integer diceRoll) {
        if (diceRoll < 1 || diceRoll > 6) {
            return false;
        }
        if (currentPosition == 516
                || currentPosition == 126
                || currentPosition == 256
                || currentPosition == 386) {
            return false;
        }
        if (currentPosition < 0) {
            return diceRoll == 6;
        }
        if (currentPosition > 99) {
            return diceRoll <= 6 - currentPosition % 10;
        }
        return true;
    }
}
