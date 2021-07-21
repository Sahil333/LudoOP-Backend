package com.op.ludo.controllers;

import com.op.ludo.controllers.dto.Board;
import com.op.ludo.controllers.dto.BoardRequest;
import com.op.ludo.controllers.dto.JoinBoard;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.LobbyService;
import com.op.ludo.service.PlayerQueueService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LobbyControllers {

    @Autowired
    LobbyService lobbyService;

    @Autowired
    PlayerQueueService playerQueueService;

    @PostMapping(value = "lobby/friend/create")
    public Board createFriendLobby(@RequestBody BoardRequest request){
        if(!request.getType().equals(BoardRequest.Type.FRIEND)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        BoardState boardState = lobbyService.handleBoardRequest(request);
        return new Board(boardState.getBoardId());
    }

    @PostMapping(value = "lobby/friend/join")
    public void joinFriendLobby(@RequestBody JoinBoard joinBoard, HttpServletResponse response){
        lobbyService.joinBoard(joinBoard.getPlayerId(), joinBoard.getBoardId());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @PostMapping(value = "lobby/online/join")
    public void joinOnlineLobby(@RequestBody BoardRequest request, HttpServletResponse response){
        if(!request.getType().equals(BoardRequest.Type.ONLINE)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        lobbyService.handleBoardRequest(request);
        response.setStatus(HttpStatus.ACCEPTED.value());
    }

    @GetMapping(value = "lobby/online/poll")
    public Board lobbyOnlineStatus(@RequestParam @NonNull String playerId, HttpServletResponse response){
        if(playerQueueService.isPlayerInQueue(playerId)){
            //  setting the status PROCESSING will make the client to wait further response
            //  but we are not sending any further response. Setting it to NO_CONTENT
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else if(lobbyService.isPlayerAlreadyPartOfGame(playerId)){
            BoardState boardState =  lobbyService.getCurrentActiveGame(playerId);
            return new Board(boardState.getBoardId());
        } else {
            throw new IllegalArgumentException("playerId=" + playerId + " is not part of the queue");
        }
        return null;
    }

}
