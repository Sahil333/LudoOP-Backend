package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class StoneMove extends AbstractAction<StoneMove.StoneMoveArgs> {

    public StoneMove(
            Long boardId,
            String playerId,
            Integer stoneNumber,
            Integer initialPosition,
            Integer finalPosition) {
        this(
                Action.STONEMOVE,
                new StoneMoveArgs(boardId, playerId, stoneNumber, initialPosition, finalPosition));
    }

    @JsonCreator
    public StoneMove(
            @JsonProperty("action") Action action, @JsonProperty("args") StoneMoveArgs args) {
        super(action, args);
        Assert.isTrue(Action.STONEMOVE.equals(action), "Action should be STONEMOVE");
    }

    @Getter
    @EqualsAndHashCode
    public static class StoneMoveArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer stoneNumber;
        private final Integer initialPosition;
        private final Integer finalPosition;

        @JsonCreator
        StoneMoveArgs(
                @JsonProperty("boardId") Long boardId,
                @JsonProperty("playerId") String playerId,
                @JsonProperty("stoneMove") Integer stoneNumber,
                @JsonProperty("initialPosition") Integer initialPosition,
                @JsonProperty("finalPosition") Integer finalPosition) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.stoneNumber = stoneNumber;
            this.initialPosition = initialPosition;
            this.finalPosition = finalPosition;
        }
    }
}
