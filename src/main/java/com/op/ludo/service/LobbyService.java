package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerQueueRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.LobbyPlayerQueque;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerQueue;
import com.op.ludo.model.PlayerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Optional;

@Service
public class LobbyService {
    @PersistenceContext
    EntityManager em;

    @Autowired
    PlayerStateRepo playerStateRepo;

    @Autowired
    BoardStateRepo boardStateRepo;

    @Autowired
    PlayerQueueRepo playerQueueRepo;

    @Autowired
    LobbyPlayerQueque lobbyPlayerQueque;

    public Boolean canCreateLobby(Integer bid, Long playerId){
        return !(bid == null || playerId == null || bid != 100 || isAlreadyPartOfGame(playerId));
    }

    public Boolean isAlreadyPartOfGame(Long playerId){
        Optional<PlayerState> playerState = playerStateRepo.findById(playerId);
        return playerState.isPresent();
    }

    public Long generateBoardId(){
        Long maxVal = 100000000l;
        Long minVal = 10000000l;
        Long boardId;
        do {
            boardId = Math.round(Math.random()*(maxVal-minVal+1)+minVal);
        } while(isBoardPresent(boardId));
        return boardId;
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
        return boardState;
    }

    public Boolean canJoinBoard(Long playerId,Long boardId){
        if(isBoardPresent(boardId) && !isAlreadyPartOfGame(playerId)){
            BoardState boardState = em.getReference(BoardState.class,boardId);
            return boardState.getPlayerCount() < 4;
        }
        return false;
    }

    public void addToPlayerQueue(Long playerId) throws IOException {
        lobbyPlayerQueque.insertInQueue(playerId.toString());
        PlayerQueue playerQueue = LobbyHelper.intializePlayerInQueue(playerId);
        playerQueueRepo.save(playerQueue);
        System.out.println(lobbyPlayerQueque.peekQueue());
    }

    @Scheduled(fixedDelay = 5000l)
    public void queueHandeler() throws IOException {
        if(!lobbyPlayerQueque.isQueueEmpty()){
            System.out.println(lobbyPlayerQueque.peekQueue());
        }
    }
}
