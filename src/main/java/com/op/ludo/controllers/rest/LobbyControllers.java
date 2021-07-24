package com.op.ludo.controllers.rest;

import com.op.ludo.auth.facade.IAuthenticationFacade;
import com.op.ludo.controllers.dto.Board;
import com.op.ludo.controllers.dto.BoardRequest;
import com.op.ludo.controllers.dto.JoinBoard;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.LobbyService;
import com.op.ludo.service.PlayerQueueService;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/")
public class LobbyControllers {

    @Autowired LobbyService lobbyService;

    @Autowired PlayerQueueService playerQueueService;

    @Autowired IAuthenticationFacade auth;

    @PostMapping(value = "lobby/friend/create")
    public Board createFriendLobby(@RequestBody BoardRequest request) {
        if (!request.getType().equals(BoardRequest.Type.FRIEND)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        request.setPlayerId(auth.getPrincipal().getUsername());
        BoardState boardState = lobbyService.handleBoardRequest(request);
        return new Board(boardState.getBoardId());
    }

    @PostMapping(value = "lobby/friend/join")
    public void joinFriendLobby(@RequestBody JoinBoard joinBoard, HttpServletResponse response) {
        lobbyService.joinBoard(auth.getPrincipal().getUsername(), joinBoard.getBoardId());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @PostMapping(value = "lobby/online/join")
    public void joinOnlineLobby(@RequestBody BoardRequest request, HttpServletResponse response) {
        if (!request.getType().equals(BoardRequest.Type.ONLINE)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        request.setPlayerId(auth.getPrincipal().getUsername());
        lobbyService.handleBoardRequest(request);
        response.setStatus(HttpStatus.ACCEPTED.value());
    }

    @GetMapping(value = "lobby/poll")
    public Board lobbyOnlineStatus(HttpServletResponse response) {
        String playerId = auth.getPrincipal().getUsername();
        if (playerQueueService.isPlayerInQueue(playerId)) {
            //  setting the status PROCESSING will make the client to wait further response
            //  but we are not sending any further response. Setting it to NO_CONTENT
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else if (lobbyService.isPlayerAlreadyPartOfGame(playerId)) {
            BoardState boardState = lobbyService.getCurrentActiveGame(playerId);
            return new Board(boardState.getBoardId());
        } else {
            throw new IllegalArgumentException(
                    "playerId=" + playerId + " is not part of the queue");
        }
        return null;
    }
}
