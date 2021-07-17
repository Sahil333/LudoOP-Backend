package com.op.ludo.helper;

import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class LobbyHelper {
    @Autowired
    PlayerStateRepo playerStateRepo;

    public Boolean isAlreadyPartOfGame(Long playerId){
        Optional<PlayerState> playerState = playerStateRepo.findById(playerId);
        return playerState.isPresent();
    }

    public BoardState initializeNewBoard(Long playerId){
        long currentTime = System.currentTimeMillis()/1000l;
        BoardState boardState = new BoardState(playerId,false,false,currentTime,-1l,-1,false,
                false, currentTime,1,playerId.toString(),1,5,"random",100,currentTime);
        return boardState;
    }
}
