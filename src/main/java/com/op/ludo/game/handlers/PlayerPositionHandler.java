package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import com.op.ludo.service.CoinService;
import java.util.List;

public class PlayerPositionHandler extends ActionHandler {

    private final CoinService coinService;

    public PlayerPositionHandler(ActionHandler next, CoinService coinService) {
        super(next);
        this.coinService = coinService;
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        StoneMove stoneMove = (StoneMove) action;
        PlayerState playerState = boardState.getPlayerState(stoneMove.getArgs().getPlayerId());
        if (playerState.getHomeCount() == 4) {
            playerState.setPlayerPosition(boardState.getNextPlayerPosition());
            issueCoins(
                    playerState.getPlayerId(),
                    playerState.getPlayerPosition(),
                    boardState.getBid());
        }
        if (next != null) next.handleAction(boardState, action, output);
    }

    private void issueCoins(String playerId, Integer playerPosition, int bid) {
        // TODO: for different player counts there will be different percentages.
        if (playerPosition == 1) coinService.issueCoin(playerId, bid * 2L);
        else if (playerPosition == 2) coinService.issueCoin(playerId, (long) (bid * 1.2));
        else if (playerPosition == 3) coinService.issueCoin(playerId, (long) (bid * 0.4));
    }
}
