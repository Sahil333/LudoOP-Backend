package com.op.ludo.controllers.websocket;

import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.service.CommunicationService;
import com.op.ludo.service.GamePlayService;
import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class UserActionController {

    @Autowired CommunicationService communicationService;

    @Autowired GamePlayService gameService;

    @MessageMapping("/game/action/start")
    public void startGame(Principal principal, @Payload GameStartDto startReq) {
        List<AbstractAction> actions =
                gameService.startGame(startReq.getBoardId(), principal.getName());
        communicationService.sendActions(startReq.getBoardId(), actions);
    }
}
