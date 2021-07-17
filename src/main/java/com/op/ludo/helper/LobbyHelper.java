package com.op.ludo.helper;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;

public class LobbyHelper {

    public static BoardState initializeNewBoard(Long boardId){
        long currentTime = System.currentTimeMillis()/1000l;
        BoardState boardState = new BoardState(boardId,false,false,currentTime,-1l,-1,false,
                false, currentTime,1,1,5,"random",100,currentTime);
        return boardState;
    }

    public static PlayerState intializeNewPlayer(Long playerId, BoardState boardState,Integer playerNumber){
        PlayerState playerState = new PlayerState(playerId,boardState,-11,-12,-13,-14,0,
                -1,true,0,playerNumber,"human","theme");
        return playerState;
    }
}
