package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class DiceRollPending extends AbstractAction<DiceRollPending.DiceRollPendingArgs> {

    public DiceRollPending(Long boardId, String playerId, Integer playerNumber) {
        super(Action.DICEROLLPENDING, new DiceRollPendingArgs(boardId, playerId, playerNumber));
    }

    public static class DiceRollPendingArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer playerNumber;

        public DiceRollPendingArgs(Long boardId, String playerId, Integer playerNumber) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.playerNumber = playerNumber;
        }

        public Long getBoardId() {
            return boardId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public Integer getPlayerNumber() {
            return playerNumber;
        }
    }
}
