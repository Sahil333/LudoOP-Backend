package com.op.ludo.helper;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerQueue;
import com.op.ludo.model.PlayerState;
import com.op.ludo.util.DateTimeUtil;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LobbyHelper {


    @Value("${gameconfig.turn-time-limit}")
    Long turnTimeLimit;

    public BoardState initializeNewBoard(Long boardId, String playerId) {
        long currentTime = DateTimeUtil.nowEpoch();
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
                        playerId,
                        turnTimeLimit,

                        "random",
                        100,
                        currentTime);
        return boardState;
    }

    public PlayerState initializeNewPlayer(
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
        long currentTime = DateTimeUtil.nowEpoch();
        PlayerQueue playerQueue = new PlayerQueue(playerId, currentTime);
        return playerQueue;
    }

    public static Integer getRandomNumberInRange(Integer min, Integer max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + 1;
    }
}
