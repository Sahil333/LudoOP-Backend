package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class GameStarted extends AbstractAction<GameStarted.GameStartArgs> {

    public GameStarted(String byPlayer) {
        super(Action.STARTED, new GameStartArgs(byPlayer));
    }

    protected static class GameStartArgs {

        private final String byPlayer;

        public GameStartArgs(String byPlayer) {
            this.byPlayer = byPlayer;
        }

        public String getByPlayer() {
            return byPlayer;
        }
    }
}
