package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
public class LobbyService {
    @PersistenceContext
    EntityManager em;

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

    public BoardState createNewBoard(Long playerId,Long boardId){
        BoardState boardState = LobbyHelper.initializeNewBoard(boardId);
        PlayerState playerState = LobbyHelper.intializeNewPlayer(playerId,boardState,1);
        boardStateRepo.save(boardState);
        playerStateRepo.save(playerState);
        return boardState;
    }

    public BoardState joinBoard(Long playerId,Long boardId){
        BoardState boardState = em.getReference(BoardState.class,boardId);
        Integer currentCount =  boardState.getPlayerCount();
        boardState.setPlayerCount(currentCount+1);
        PlayerState playerState = LobbyHelper.intializeNewPlayer(playerId,boardState,currentCount+1);
        playerStateRepo.save(playerState);
        List<PlayerState> playerStateList = playerStateRepo.findByBoardId(boardState);
        return boardState;
    }



}
