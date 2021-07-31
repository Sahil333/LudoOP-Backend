package com.op.ludo.game.action.impl;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;

public class StoneMove extends AbstractAction<StoneMove.StoneMoveArgs> {

    public StoneMove(
            Long boardId,
            String playerId,
            Integer stoneNumber,
            Integer initialPosition,
            Integer finalPosition) {
        super(
                Action.STONEMOVE,
                new StoneMoveArgs(boardId, playerId, stoneNumber, initialPosition, finalPosition));
    }

    public static class StoneMoveArgs {
        private final Long boardId;
        private final String playerId;
        private final Integer stoneNumber;
        private final Integer initialPosition;
        private final Integer finalPosition;

        StoneMoveArgs(
                Long boardId,
                String playerId,
                Integer stoneNumber,
                Integer initialPosition,
                Integer finalPosition) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.stoneNumber = stoneNumber;
            this.initialPosition = initialPosition;
            this.finalPosition = finalPosition;
        }

        public Long getBoardId() {
            return boardId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public Integer getStoneNumber() {
            return stoneNumber;
        }

        public Integer getInitialPosition() {
            return initialPosition;
        }

        public Integer getFinalPosition() {
            return finalPosition;
        }
    }
}
