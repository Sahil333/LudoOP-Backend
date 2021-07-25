package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class DiceRoll extends AbstractAction<DiceRoll.DiceRollArgs> {

    public DiceRoll(String playerId) {
        super(Action.DICEROLL, new DiceRollArgs(playerId));
    }

    public static class DiceRollArgs {
        private final String playerId;
        private final Integer diceRoll;

        public DiceRollArgs(String playerId) {
            this.playerId = playerId;
            this.diceRoll = diceRollGenerator();
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
