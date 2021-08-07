package com.op.ludo.game.handlers.chain;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.handlers.ActionHandler;
import com.op.ludo.game.handlers.CoinDeductHandler;
import com.op.ludo.game.handlers.GameStartHandler;
import com.op.ludo.game.handlers.TimerHandler;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.CoinService;
import com.op.ludo.service.TimerService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GameStartChain extends ActionHandler {

    private ActionHandler first;

    public GameStartChain(CoinService coinService, TimerService timerService) {
        super(null);
        TimerHandler timerHandler = new TimerHandler(null, timerService);
        CoinDeductHandler coinDeductHandler = new CoinDeductHandler(timerHandler, coinService);
        this.first = new GameStartHandler(coinDeductHandler);
    }

    @Override
    public void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output) {
        this.first.handleAction(boardState, action, output);
    }
}
