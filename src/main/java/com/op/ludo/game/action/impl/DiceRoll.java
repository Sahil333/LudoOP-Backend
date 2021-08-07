package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import java.util.concurrent.ThreadLocalRandom;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class DiceRoll extends AbstractAction<DiceRoll.DiceRollArgs> {

    public DiceRoll(String playerId, Long boardId) {
        this(Action.DICEROLL, new DiceRollArgs(playerId, boardId));
    }

    @JsonCreator
    public DiceRoll(
            @JsonProperty("action") Action action, @JsonProperty("args") DiceRollArgs args) {
        super(action, args);
        Assert.isTrue(Action.DICEROLL.equals(action), "Action should be DICEROLL");
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class DiceRollArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer diceRoll;

        @JsonCreator
        public DiceRollArgs(
                @JsonProperty("playerId") String playerId, @JsonProperty("boardId") Long boardId) {
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
            return ThreadLocalRandom.current().nextInt(1, 7);
        }
    }
}
