package com.op.ludo.controllers.websocket;

import com.op.ludo.exceptions.GameException;
import com.op.ludo.exceptions.GameStartException;
import com.op.ludo.game.action.GameStarted;
import com.op.ludo.service.GameService;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class UserActionController {

  @Autowired SimpMessagingTemplate messagingTemplate;

  @Autowired GameService gameService;

  @MessageMapping("/game/action/start")
  public void startGame(Principal principal, @Header("boardId") Long boardId) {
    try {
      gameService.startGame(principal.getName(), boardId);
      messagingTemplate.convertAndSend(
          "/topic/game/" + boardId, new GameStarted(boardId, principal.getName()));
    } catch (Exception ex) {
      throw new GameStartException("Game start failed.", ex);
    }
  }

  @MessageExceptionHandler
  @SendToUser(destinations = "/queue/errors", broadcast = false)
  public GameException handleGameException(Exception ex) {
    return new GameException(ex);
  }
}
