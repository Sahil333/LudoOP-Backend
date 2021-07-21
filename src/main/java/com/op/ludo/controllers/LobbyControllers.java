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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LobbyControllers {

    @Autowired
    LobbyService lobbyService;

    @Autowired
    PlayerQueueService playerQueueService;

    @PostMapping(value = "lobby/friend/create")
    public ResponseEntity<Board> createFriendLobby(@RequestBody BoardRequest request){
        if(!request.getType().equals(BoardRequest.Type.FRIEND)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        BoardState boardState = lobbyService.handleBoardRequest(request);
        return new ResponseEntity<>(new Board(boardState.getBoardId()),HttpStatus.OK);
    }

    @PostMapping(value = "lobby/friend/join")
    public ResponseEntity<Map<String,String>> joinFriendLobby(@RequestBody JoinBoard joinBoard){
        lobbyService.joinBoard(joinBoard.getPlayerId(), joinBoard.getBoardId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "lobby/online/join")
    public ResponseEntity<Map<String,String>> joinOnlineLobby(@RequestBody BoardRequest request){
        if(!request.getType().equals(BoardRequest.Type.ONLINE)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        lobbyService.handleBoardRequest(request);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "lobby/online/poll")
    public ResponseEntity<Board> lobbyOnlineStatus(@RequestParam @NonNull String playerId){
        if(playerQueueService.isPlayerInQueue(playerId)){
            return new ResponseEntity<>(HttpStatus.PROCESSING);
        } else if(lobbyService.isPlayerAlreadyPartOfGame(playerId)){
            Map<String,String> mp = new HashMap<>();
            BoardState boardState =  lobbyService.getCurrentActiveGame(playerId);
            return new ResponseEntity<>(new Board(boardState.getBoardId()),HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
