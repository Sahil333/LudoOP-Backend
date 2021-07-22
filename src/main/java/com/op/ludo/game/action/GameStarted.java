package com.op.ludo.game.action;

import lombok.Data;

@Data
public class GameStarted {

  private final Long boardId;
  private final String byPlayer;

  public GameStarted(Long boardId, String byPlayer) {
    this.boardId = boardId;
    this.byPlayer = byPlayer;
  }
}
