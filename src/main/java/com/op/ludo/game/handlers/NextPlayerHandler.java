package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;

public class NextPlayerHandler extends ActionHandler {

    public NextPlayerHandler(ActionHandler next) {
        super(next);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        StoneMove stoneMove = (StoneMove) action;
        PlayerState playerState = boardState.getPlayerState(stoneMove.getArgs().getPlayerId());
        boolean hasCutStone = checkOutputHasCutStones(output);
        DiceRollPending diceRollPending;
        String diceRollPlayerId;
        if ((boardState.getLastDiceRoll() == 6 || hasCutStone) && playerState.getHomeCount() < 4) {
            diceRollPlayerId = playerState.getPlayerId();
            diceRollPending =
                    new DiceRollPending(
                            stoneMove.getArgs().getBoardId(), stoneMove.getArgs().getPlayerId());
        } else {
            PlayerState nextPlayer = boardState.getNextPlayer(playerState);
            diceRollPlayerId = nextPlayer.getPlayerId();
            diceRollPending =
                    new DiceRollPending(boardState.getBoardId(), nextPlayer.getPlayerId());
            boardState.setWhoseTurn(nextPlayer.getPlayerId());
        }
        boardState.setRollPending(true);
        boardState.setMovePending(false);
        boardState.setWhoseTurn(diceRollPlayerId);
        output.add(diceRollPending);
    }

    private boolean checkOutputHasCutStones(List<AbstractAction> output) {
        int count = 0;
        for (AbstractAction action : output) {
            if (action instanceof StoneMove) ++count;
        }
        return count > 1;
    }
}
