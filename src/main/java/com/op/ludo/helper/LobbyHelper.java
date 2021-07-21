package com.op.ludo.helper;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerQueue;
import com.op.ludo.model.PlayerState;

public class LobbyHelper {

  public static BoardState initializeNewBoard(Long boardId) {
    long currentTime = System.currentTimeMillis() / 1000l;
    BoardState boardState =
        new BoardState(
            boardId,
            false,
            false,
            currentTime,
            -1l,
            -1,
            false,
            false,
            currentTime,
            0,
            1,
            5,
            "random",
            100,
            currentTime);
    return boardState;
  }

  public static PlayerState initializeNewPlayer(
      String playerId, BoardState boardState, Integer playerNumber) {
    Integer stone1 = (-10 * playerNumber) - 1,
        stone2 = stone1 - 1,
        stone3 = stone2 - 1,
        stone4 = stone3 - 1;
    PlayerState playerState =
        new PlayerState(
            playerId,
            boardState,
            stone1,
            stone2,
            stone3,
            stone4,
            0,
            -1,
            true,
            0,
            playerNumber,
            "human",
            "theme");
    return playerState;
  }

  public static PlayerQueue intializePlayerInQueue(String playerId) {
    long currentTime = System.currentTimeMillis() / 1000l;
    PlayerQueue playerQueue = new PlayerQueue(playerId, currentTime);
    return playerQueue;
  }
}
