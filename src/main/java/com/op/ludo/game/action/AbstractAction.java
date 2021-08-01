package com.op.ludo.game.action;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public abstract class AbstractAction<A> {

    protected Action action;
    protected A args;

    public AbstractAction(Action action, A args) {
        this.action = action;
        this.args = args;
    }

    public Action getAction() {
        return action;
    }

    public A getArgs() {
        return args;
    }
}
