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

    @PostMapping(value = "lobby/create/friend")
    public ResponseEntity<Map<String,String>> createLobbyFriend(Integer bid,Long playerId){
        if(bid == null || playerId == null || bid != 100 || lobbyService.isAlreadyPartOfGame(playerId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        lobbyService.createNewBoard(playerId);
        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("boardId",playerId.toString());
        return new ResponseEntity<>(returnMap,HttpStatus.OK);
    }



}
