package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class DiceRollReq extends AbstractAction<DiceRollReq.DiceRollReqArgs> {

    public DiceRollReq(Long boardId, String playerId) {
        this(Action.DICEROLLREQ, new DiceRollReqArgs(boardId, playerId));
    }

    @JsonCreator
    public DiceRollReq(
            @JsonProperty("action") Action action, @JsonProperty("args") DiceRollReqArgs args) {
        super(action, args);
        Assert.isTrue(Action.DICEROLLREQ.equals(action), "Action should be DICEROLLREQ");
    }

    @EqualsAndHashCode
    @Getter
    @ToString
    public static class DiceRollReqArgs {
        private final Long boardId;
        private final String playerId;

        @JsonCreator
        public DiceRollReqArgs(
                @JsonProperty("boardId") Long boardId, @JsonProperty("playerId") String playerId) {
            this.boardId = boardId;
            this.playerId = playerId;
        }
    }
}
