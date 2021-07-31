package com.op.ludo.game.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

@EqualsAndHashCode(callSuper = true)
public class GameStarted extends AbstractAction<GameStarted.GameStartArgs> {

    public GameStarted(Long boardId, String byPlayer) {
        this(Action.STARTED, new GameStartArgs(boardId, byPlayer));
    }

    @JsonCreator
    public GameStarted(
            @JsonProperty("action") Action action, @JsonProperty("args") GameStartArgs args) {
        super(action, args);
        Assert.isTrue(Action.STARTED.equals(action), "Action should be STARTED");
    }

    @EqualsAndHashCode
    @Getter
    public static class GameStartArgs {

        private final Long boardId;
        private final String byPlayer;

        @JsonCreator
        public GameStartArgs(
                @JsonProperty("boardId") Long boardId, @JsonProperty("byPlayer") String byPlayer) {
            this.boardId = boardId;
            this.byPlayer = byPlayer;
        }
    }
}
