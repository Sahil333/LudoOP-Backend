package com.op.ludo.service;

import com.op.ludo.controllers.dto.converters.BoardStateToBoardDto;
import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.BoardDto;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CommunicationService {

    @Autowired SimpMessagingTemplate messagingTemplate;

    @Autowired LobbyService lobbyService;

    public void sendActions(Long boardId, List<AbstractAction> actions) {
        if (!CollectionUtils.isEmpty(actions)) {
            BoardState board = lobbyService.getBoardState(boardId);
            BoardDto boardDto = BoardStateToBoardDto.convertToDto(board);
            messagingTemplate.convertAndSend(
                    "/topic/game/" + boardId, new ActionsWithBoardState(actions, boardDto));
        }
    }
}
