package com.op.ludo.controllers.rest;

import com.op.ludo.auth.facade.IAuthenticationFacade;
import com.op.ludo.controllers.dto.JoinLobby;
import com.op.ludo.controllers.dto.Lobby;
import com.op.ludo.controllers.dto.LobbyRequest;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import com.op.ludo.service.GamePlayService;
import com.op.ludo.service.LobbyService;
import com.op.ludo.service.PlayerQueueService;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired GamePlayService gamePlayService;

    @PostMapping(value = "lobby/friend/create")
    public Lobby createFriendLobby(@RequestBody LobbyRequest request) {
        if (!request.getType().equals(LobbyRequest.Type.FRIEND)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        request.setPlayerId(auth.getPrincipal().getUsername());
        BoardState boardState = lobbyService.handleBoardRequest(request);
        return new Lobby(boardState.getBoardId());
    }

    @PostMapping(value = "lobby/friend/join")
    public void joinFriendLobby(@RequestBody JoinLobby joinLobby, HttpServletResponse response) {
        lobbyService.joinBoard(auth.getPrincipal().getUsername(), joinLobby.getBoardId());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @PostMapping(value = "lobby/online/join")
    public void joinOnlineLobby(@RequestBody LobbyRequest request, HttpServletResponse response) {
        if (!request.getType().equals(LobbyRequest.Type.ONLINE)) {
            throw new InvalidBoardRequest("Invalid board type in request");
        }
        request.setPlayerId(auth.getPrincipal().getUsername());
        lobbyService.handleBoardRequest(request);
        response.setStatus(HttpStatus.ACCEPTED.value());
    }

    @GetMapping(value = "lobby/poll")
    public Lobby lobbyOnlineStatus(HttpServletResponse response) {
        String playerId = auth.getPrincipal().getUsername();
        if (playerQueueService.isPlayerInQueue(playerId)) {
            //  setting the status PROCESSING will make the client to wait further response
            //  but we are not sending any further response. Setting it to NO_CONTENT
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else if (lobbyService.isPlayerAlreadyPartOfGame(playerId)) {
            BoardState boardState = lobbyService.getCurrentActiveGame(playerId);
            return new Lobby(boardState.getBoardId());
        } else {
            throw new IllegalArgumentException(
                    "playerId=" + playerId + " is not part of the queue");
        }
        return null;
    }
    /* WARNING */
    /* Below Code is only for testing and will be removed*/

    @PostMapping(value = "test/mymove")
    public List<AbstractAction> testMymoveController() {
        return new ArrayList<>();
    }
}
