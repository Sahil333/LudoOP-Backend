package com.op.ludo.controllers.websocket;

import com.op.ludo.controllers.dto.websocket.DiceRollDto;
import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.controllers.dto.websocket.StoneMoveDto;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.DiceRollReq;
import com.op.ludo.game.action.impl.StoneMove;
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
                gameService.startFriendGame(startReq.getBoardId(), principal.getName());
        communicationService.sendActions(startReq.getBoardId(), actions);
    }

    @MessageMapping("/game/action/moveStone")
    public void moveStone(Principal principal, @Payload StoneMoveDto stoneMoveDto) {
        StoneMove stoneMove =
                new StoneMove(
                        stoneMoveDto.getBoardId(),
                        principal.getName(),
                        stoneMoveDto.getStoneNumber(),
                        stoneMoveDto.getInitialPosition(),
                        stoneMoveDto.getFinalPosition());
        List<AbstractAction> actions = gameService.updateBoardWithStoneMove(stoneMove);
        communicationService.sendActions(stoneMoveDto.getBoardId(), actions);
    }

    @MessageMapping("/game/action/diceRoll")
    public void diceRoll(Principal principal, @Payload DiceRollDto diceRollDto) {
        DiceRollReq diceRollReq = new DiceRollReq(diceRollDto.getBoardId(), principal.getName());
        List<AbstractAction> actions = gameService.rollDiceForPlayer(diceRollReq);
        communicationService.sendActions(diceRollDto.getBoardId(), actions);
    }
}
