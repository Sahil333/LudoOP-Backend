package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class DiceRoll extends AbstractAction<DiceRoll.DiceRollArgs> {

    public DiceRoll(String playerId, Long boardId) {
        super(Action.DICEROLL, new DiceRollArgs(playerId, boardId));
    }

    public static class DiceRollArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer diceRoll;

        public DiceRollArgs(String playerId, Long boardId) {
            this.playerId = playerId;
            this.boardId = boardId;
            this.diceRoll = diceRollGenerator();
        }

        public Long getBoardId() {
            return boardId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public Integer getDiceRoll() {
            return diceRoll;
        }

        private Integer diceRollGenerator() {
            return Math.toIntExact(Math.round(Math.random() * (6 - 1 + 1) + 1));
        }
    }
}
