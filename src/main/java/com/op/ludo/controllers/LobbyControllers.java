package com.op.ludo.controllers;

import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LobbyControllers {

    @Autowired
    LobbyService lobbyService;

    @PostMapping(value = "lobby/friend/create")
    public ResponseEntity<Map<String,String>> createLobbyFriend(Integer bid,Long playerId,Long boardId){
        Map<String,String> returnMap = new HashMap<>();
        if(bid == null || playerId == null || bid != 100 || lobbyService.isAlreadyPartOfGame(playerId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        lobbyService.createNewBoard(playerId,boardId);
        returnMap.put("boardId",playerId.toString());
        return new ResponseEntity<>(returnMap,HttpStatus.OK);
    }

    @PostMapping(value = "lobby/friend/join")
    public ResponseEntity<Map<String,String>> joinLobbyFriend(Long playerId,Long boardId){
        //we do not need this endpoint need to create a websocket here
        if(playerId == null || boardId == null || lobbyService.isAlreadyPartOfGame(playerId) || !lobbyService.isBoardPresent(boardId)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        lobbyService.joinBoard(playerId,boardId);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
