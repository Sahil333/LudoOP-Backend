package com.op.ludo.helper;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;

public class LobbyHelper {

    public static BoardState initializeNewBoard(Long playerId){
        long currentTime = System.currentTimeMillis()/1000l;
        BoardState boardState = new BoardState(playerId,false,false,currentTime,-1l,-1,false,
                false, currentTime,1,playerId.toString(),1,5,"random",100,currentTime);
        return boardState;
    }

    public static PlayerState intializeNewPlayer(Long playerId, Long boardId,Integer playerNumber){
        PlayerState playerState = new PlayerState(playerId,boardId,-11,-12,-13,-14,0,
                -1,true,0,playerNumber,"human","theme");
        return playerState;
    }
}
