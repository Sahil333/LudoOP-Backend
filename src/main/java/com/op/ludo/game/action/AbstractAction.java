package com.op.ludo.game.action;

public abstract class AbstractAction<A> {

  protected Action action;
  protected A parameters;

  public AbstractAction(Action action, A parameters) {
    this.action = action;
    this.parameters = parameters;
  }

  public Action getAction() {
    return action;
  }

  public A getArgs() {
    return parameters;
  }
}
