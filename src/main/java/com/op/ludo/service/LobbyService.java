package com.op.ludo.service;

import com.op.ludo.controllers.dto.LobbyRequest;
import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.BoardNotFoundException;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class LobbyService {
    @Autowired LobbyHelper lobbyHelper;

    @PersistenceContext EntityManager em;

    @Autowired PlayerStateRepo playerStateRepo;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired PlayerQueueService playerQueueService;

    // TODO: should check if already part of game and has left the game. Essentially, the condition
    //  is a player can be part of one game at a time. If player/client is already part of a game,
    //  it should be able to leave that game.
    public Boolean isPlayerAlreadyPartOfGame(String playerId) {
        return playerStateRepo.existsById(playerId);
    }

    public BoardState getCurrentActiveGame(String playerId) {
        try {
            return em.getReference(PlayerState.class, playerId).getBoardState();
        } catch (EntityNotFoundException ex) {
            log.error("No player state found for playerId={}", playerId);
            return null;
        }
    }

    public BoardState joinBoard(String playerId, Long boardId) {
        BoardState boardState = em.getReference(BoardState.class, boardId);
        return joinBoard(playerId, boardState);
    }

    private BoardState joinBoard(String playerId, BoardState boardState) {
        if (!canJoinBoard(playerId, boardState)) {
            throw new IllegalStateException(
                    "playerId=" + playerId + " can not join board=" + boardState.getBoardId());
        }
        int currentCount = boardState.getPlayerCount();
        boardState.setPlayerCount(currentCount + 1);
        PlayerState playerState;
        if (currentCount
                == 1) { // the 2nd player should be on second 3rd place and 3rd should be on 2nd
            // place
            playerState = lobbyHelper.initializeNewPlayer(playerId, boardState, 3);
        } else if (currentCount == 2) {
            playerState = lobbyHelper.initializeNewPlayer(playerId, boardState, 2);
        } else {
            playerState = lobbyHelper.initializeNewPlayer(playerId, boardState, currentCount + 1);
        }
        playerStateRepo.save(playerState);
        boardStateRepo.save(boardState);
        return boardState;
    }

    private Boolean canJoinBoard(String playerId, BoardState boardState) {
        if (!isPlayerAlreadyPartOfGame(playerId)) {
            return boardState.getPlayerCount() < 4;
        }
        return false;
    }

    public BoardState createBoardWithPlayers(List<String> playerIds) {
        Long boardId = generateBoardId();
        BoardState boardState = createNewBoard(boardId, playerIds.get(0));
        for (String playerId : playerIds) {
            joinBoard(playerId, boardState);
        }
        return boardState;
    }

    public BoardState handleBoardRequest(LobbyRequest request) {
        if (!canCreateLobby(request.getBid(), request.getPlayerId())) {
            throw new IllegalArgumentException("Invalid board request");
        }
        switch (request.getType()) {
            case FRIEND:
                return handleFriendBoardRequest(request);
            case ONLINE:
                return handleOnlineBoardRequest(request);
            default:
                throw new InvalidBoardRequest(
                        "boardType=" + request.getType() + "is not supported");
        }
    }

    public BoardState getBoardState(Long boardId) {
        return boardStateRepo
                .findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("boardId=" + boardId + " not found"));
    }

    private Boolean canCreateLobby(Integer bid, String playerId) {
        return !(bid != 100 || isPlayerAlreadyPartOfGame(playerId));
    }

    private BoardState handleOnlineBoardRequest(LobbyRequest request) {
        playerQueueService.addToPlayerQueue(request.getPlayerId());
        return null;
    }

    private BoardState handleFriendBoardRequest(LobbyRequest request) {
        Long boardId = generateBoardId();
        BoardState boardState = createNewBoard(boardId, request.getPlayerId());
        joinBoard(request.getPlayerId(), boardState);
        return boardState;
    }

    private BoardState createNewBoard(Long boardId, String playerId) {
        BoardState boardState = lobbyHelper.initializeNewBoard(boardId, playerId);
        boardStateRepo.save(boardState);
        return boardState;
    }

    private Long generateBoardId() {
        Long maxVal = 100000000l;
        Long minVal = 10000000l;
        Long boardId;
        do {
            boardId = Math.round(Math.random() * (maxVal - minVal + 1) + minVal);
        } while (isBoardPresent(boardId));
        return boardId;
    }

    private Boolean isBoardPresent(Long boardId) {
        return boardStateRepo.existsById(boardId);
    }
}
