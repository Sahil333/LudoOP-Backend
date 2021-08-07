package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.GameStarted;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import com.op.ludo.service.CoinService;
import java.util.List;

public class CoinDeductHandler extends ActionHandler {

    private final CoinService coinService;

    public CoinDeductHandler(ActionHandler next, CoinService coinService) {
        super(next);
        this.coinService = coinService;
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof GameStarted)) {
            // in future, may be able to handle different types of deduction
            throw new IllegalArgumentException("Only handles game started action");
        }

        for (PlayerState player : boardState.getPlayers()) {
            coinService.deductCoins(player.getPlayerId(), boardState.getBid().longValue());
        }

        if (next != null) next.handleAction(boardState, action, output);
    }
}
