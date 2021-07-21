package com.op.ludo.controllers;

import com.op.ludo.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Controller
public class LobbyControllers {

    @Autowired
    LobbyService lobbyService;

    @PostMapping(value = "lobby/friend/create")
    public ResponseEntity<Map<String,String>> createFriendLobby(Integer bid,Long playerId){
        Map<String,String> returnMap = new HashMap<>();
        if(!lobbyService.canCreateLobby(bid,playerId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Long boardId = lobbyService.generateBoardId();
        lobbyService.createNewBoard(boardId);
        lobbyService.joinBoard(playerId,boardId);
        returnMap.put("boardId",boardId.toString());
        return new ResponseEntity<>(returnMap,HttpStatus.OK);
    }

    @PostMapping(value = "lobby/friend/join")
    public ResponseEntity<Map<String,String>> joinFriendLobby(Long boardId,Long playerId){
        Map<String,String> returnMap = new HashMap<>();
        if(!lobbyService.canJoinBoard(boardId, playerId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        lobbyService.joinBoard(playerId, boardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "lobby/online/join")
    public ResponseEntity<Map<String,String>> joinOnlineLobby(Long playerId){
        Map<String,String> returnMap = null;
        if(playerId == null || lobbyService.isPlayerAlreadyPartOfGame(playerId)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if(lobbyService.isPlayerInQueue(playerId)){
            return new ResponseEntity<>(HttpStatus.PROCESSING);
        }
        try {
            lobbyService.addToPlayerQueue(playerId);
        } catch (IOException e) {
            returnMap = new HashMap<>();
            returnMap.put("error","Unable to join game. Please try again later.");
            return new ResponseEntity<>(returnMap,HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "lobby/online/poll")
    public ResponseEntity<Map<String,String>> lobbyOnlineStatus(Long playerId){
        if(playerId == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if(lobbyService.isPlayerInQueue(playerId)){
            return new ResponseEntity<>(HttpStatus.PROCESSING);
        } else if(lobbyService.isPlayerAlreadyPartOfGame(playerId)){
            Map<String,String> mp = new HashMap<>();
            Long boardId =  lobbyService.getPlayerState(playerId).getBoardState().getBoardId();
            mp.put("boardId",boardId.toString());
            return new ResponseEntity<>(mp,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
