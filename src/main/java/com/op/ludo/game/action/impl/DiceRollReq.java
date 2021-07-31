package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class DiceRollReq extends AbstractAction<DiceRollReq.DiceRollReqArgs> {
    public DiceRollReq(Long boardId, String playerId) {
        super(Action.DICEROLLREQ, new DiceRollReqArgs(boardId, playerId));
    }

    public static class DiceRollReqArgs {
        private final Long boardId;
        private final String playerId;

        public Long getBoardId() {
            return boardId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public DiceRollReqArgs(Long boardId, String playerId) {
            this.boardId = boardId;
            this.playerId = playerId;
        }
    }
}
