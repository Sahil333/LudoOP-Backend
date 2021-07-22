package com.op.ludo.controllers.websocket;

import com.op.ludo.game.action.impl.GameStarted;
import com.op.ludo.service.GameService;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class UserActionController {

  @Autowired SimpMessagingTemplate messagingTemplate;

  @Autowired GameService gameService;

  @MessageMapping("/game/action/start")
  public void startGame(Principal principal, @Header("boardId") Long boardId) {
    gameService.startGame(principal.getName(), boardId);
    messagingTemplate.convertAndSend(
        "/topic/game/" + boardId, new GameStarted(principal.getName()));
  }
}
