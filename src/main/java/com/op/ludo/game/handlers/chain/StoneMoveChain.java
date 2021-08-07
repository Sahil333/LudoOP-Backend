package com.op.ludo.game.handlers.chain;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.game.handlers.ActionHandler;
import com.op.ludo.game.handlers.GameEndHandler;
import com.op.ludo.game.handlers.NextPlayerHandler;
import com.op.ludo.game.handlers.PlayerPositionHandler;
import com.op.ludo.game.handlers.StoneCutHandler;
import com.op.ludo.game.handlers.StoneMoveHandler;
import com.op.ludo.game.handlers.TimerHandler;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.CoinService;
import com.op.ludo.service.TimerService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoneMoveChain extends ActionHandler {

    private final ActionHandler first;

    StoneMoveChain(TimerService timerService, CoinService coinService) {
        super(null);
        ActionHandler timerHandler = new TimerHandler(null, timerService);
        ActionHandler nextPlayerHandler = new NextPlayerHandler(timerHandler);
        ActionHandler gameEndHandler = new GameEndHandler(nextPlayerHandler);
        ActionHandler playerPositionHandler =
                new PlayerPositionHandler(gameEndHandler, coinService);
        ActionHandler stoneCutHandler = new StoneCutHandler(playerPositionHandler);
        first = new StoneMoveHandler(stoneCutHandler);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        if (!(action instanceof StoneMove))
            throw new IllegalArgumentException("Only handles stone move action");
        first.handleAction(boardState, action, output);
    }
}
