package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class StoneMovePending extends AbstractAction<StoneMovePending.StoneMovePendingArgs> {

    public StoneMovePending(Long boardId, String playerId, Integer playerNumber) {
        super(Action.STONEMOVEPENDING, new StoneMovePendingArgs(boardId, playerId, playerNumber));
    }

    public static class StoneMovePendingArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer playerNumber;

        public StoneMovePendingArgs(Long boardId, String playerId, Integer playerNumber) {
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
