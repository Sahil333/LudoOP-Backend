package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class DiceRollPending extends AbstractAction<DiceRollPending.DiceRollPendingArgs> {

    public DiceRollPending(Long boardId, String playerId, Integer playerNumber) {
        this(Action.DICEROLLPENDING, new DiceRollPendingArgs(boardId, playerId, playerNumber));
    }

    @JsonCreator
    public DiceRollPending(
            @JsonProperty("action") Action action, @JsonProperty("args") DiceRollPendingArgs args) {
        super(action, args);
        Assert.isTrue(Action.DICEROLLPENDING.equals(action), "Action should be DiceRollPending");
    }

    @Getter
    @EqualsAndHashCode
    public static class DiceRollPendingArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer playerNumber;

        @JsonCreator
        public DiceRollPendingArgs(
                @JsonProperty("boardId") Long boardId,
                @JsonProperty("playerId") String playerId,
                @JsonProperty("playerNumber") Integer playerNumber) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.playerNumber = playerNumber;
        }
    }
}
