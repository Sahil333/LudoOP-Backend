package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class StoneMovePending extends AbstractAction<StoneMovePending.StoneMovePendingArgs> {

    public StoneMovePending(Long boardId, String playerId, Integer playerNumber) {
        this(Action.STONEMOVEPENDING, new StoneMovePendingArgs(boardId, playerId, playerNumber));
    }

    @JsonCreator
    public StoneMovePending(
            @JsonProperty("action") Action action,
            @JsonProperty("args") StoneMovePendingArgs args) {
        super(action, args);
        Assert.isTrue(Action.STONEMOVEPENDING.equals(action), "Action should be STONEMOVEPENDING");
    }

    @Getter
    @EqualsAndHashCode
    public static class StoneMovePendingArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer playerNumber;

        @JsonCreator
        public StoneMovePendingArgs(
                @JsonProperty("boardId") Long boardId,
                @JsonProperty("playerId") String playerId,
                @JsonProperty("playerNumber") Integer playerNumber) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.playerNumber = playerNumber;
        }
    }
}
