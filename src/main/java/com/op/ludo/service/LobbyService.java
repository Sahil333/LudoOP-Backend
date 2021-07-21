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
import javax.transaction.Transactional;
import java.io.IOException;

@Service
@Transactional
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
        return !(bid == null || playerId == null || bid != 100 || isPlayerAlreadyPartOfGame(playerId));
    }

    public Boolean isPlayerAlreadyPartOfGame(Long playerId){
        return playerStateRepo.existsById(playerId);
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
        return boardStateRepo.existsById(boardId);
    }

    public BoardState createNewBoard(Long boardId){
        BoardState boardState = LobbyHelper.initializeNewBoard(boardId);
        boardStateRepo.save(boardState);
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
        if(isBoardPresent(boardId) && !isPlayerAlreadyPartOfGame(playerId)){
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
        System.out.println("Handler Running");
        while(lobbyPlayerQueque.getQueueSize() > 3){
            queueBoardCreator();
        }
        if(!lobbyPlayerQueque.isQueueEmpty()){
            System.out.println(lobbyPlayerQueque.getQueueSize()+"is the queue Size");
        }
    }

    public PlayerState getPlayerState(Long playerId){
        return em.getReference(PlayerState.class,playerId);
    }

    public Boolean isPlayerInQueue(Long playerId){
        return playerQueueRepo.existsById(playerId);
    }

    private void queueBoardCreator() throws IOException {
        Long newBoardId = generateBoardId();
        createNewBoard(newBoardId);
        for(int i=0;i<4;i++){
            Long queuePlayerId = Long.valueOf(lobbyPlayerQueque.peekQueue());
            System.out.println(queuePlayerId+"is the player in queue");
            joinBoard(queuePlayerId,newBoardId);
            lobbyPlayerQueque.dequeueQueue();
            if(playerQueueRepo.existsById(queuePlayerId)){
                playerQueueRepo.deleteById(queuePlayerId);
            }
        }
    }
}
