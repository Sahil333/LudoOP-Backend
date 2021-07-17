package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LobbyService {
    @Autowired
    PlayerStateRepo playerStateRepo;

    @Autowired
    BoardStateRepo boardStateRepo;

    public Boolean isAlreadyPartOfGame(Long playerId){
        Optional<PlayerState> playerState = playerStateRepo.findById(playerId);
        return playerState.isPresent();
    }

    public Boolean isBoardPresent(Long boardId){
        Optional<BoardState> boardState = boardStateRepo.findById(boardId);
        return boardState.isPresent();
    }

    public BoardState createNewBoard(Long playerId){
        BoardState boardState = LobbyHelper.initializeNewBoard(playerId);
        PlayerState playerState = LobbyHelper.intializeNewPlayer(playerId,playerId,1);
        boardStateRepo.save(boardState);
        playerStateRepo.save(playerState);
        return boardState;
    }



}
