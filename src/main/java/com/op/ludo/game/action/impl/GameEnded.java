package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import java.util.List;

public class GameEnded extends AbstractAction<GameEnded.GameEndedArgs> {

    public GameEnded(List<PlayerPosition> playerPositions) {
        super(Action.ENDED, new GameEndedArgs(playerPositions));
    }

    public static class PlayerPosition {
        private final String playerId;
        private final Integer position;
        private final Integer coinWinnings;

        public PlayerPosition(String playerId, Integer position, Integer coinWinnings) {
            this.playerId = playerId;
            this.position = position;
            this.coinWinnings = coinWinnings;
        }

        public String getPlayerId() {
            return playerId;
        }

        public Integer getPosition() {
            return position;
        }

        public Integer getCoinWinnings() {
            return coinWinnings;
        }
    }

    public static class GameEndedArgs {
        private final List<PlayerPosition> playerPositions;

        public GameEndedArgs(List<PlayerPosition> positions) {
            this.playerPositions = positions;
        }

        public List<PlayerPosition> getPositions() {
            return playerPositions;
        }
    }
}
