package com.op.ludo.integrationTest;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/*
- should setup the stomp clients for the given user and the board
- should be able to send an action from a given user
- should be able to check if other users got the actions in their subscription
- should be able to check error in the sender queue
*/

@Slf4j
public class BoardStompClients {

  private List<WebSocketStompClient> boardClients;
  private Long boardId;
  private List<Principal> principals;

  public BoardStompClients(Long boardId, List<Principal> principals) {
    this.boardId = boardId;
    this.principals = principals;
    //        setUpClients();
  }

  //    private void setUpClients() {
  //        for()
  //    }
  //

  private static class BoardStompSessionHandler extends StompSessionHandlerAdapter {
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
      log.info("connected");
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return String.class;
    }

    @Override
    public void handleException(
        StompSession session,
        StompCommand command,
        StompHeaders headers,
        byte[] payload,
        Throwable exception) {
      log.error("exception ", exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
      log.error("exception in transport ", exception);
    }
  }
  ;
}
