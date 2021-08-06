package com.op.ludo.game.handlers;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import java.util.List;

public abstract class ActionHandler {

    protected ActionHandler next;

    public ActionHandler(ActionHandler next) {
        this.next = next;
    }

    public abstract void handleAction(
            BoardState boardState, AbstractAction action, List<AbstractAction> output);
}
