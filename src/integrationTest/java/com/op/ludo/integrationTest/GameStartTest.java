package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.integrationTest.helper.FirebaseTokenProvider;
import com.op.ludo.integrationTest.helper.JsonStringMessageConverter;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class GameStartTest {

  @LocalServerPort private Integer port;

  private WebSocketStompClient stompClient;

  private StompSession stompSession;
  private CompletableFuture<String> startedActionFuture;

  @Autowired BoardStateRepo boardStateRepo;

  @Autowired FirebaseTokenProvider tokenProvider;

  @BeforeEach
  public void setup() throws ExecutionException, InterruptedException, TimeoutException {
    startedActionFuture = new CompletableFuture<>();
    this.stompClient =
        new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new JsonStringMessageConverter());
    String url = String.format("ws://localhost:%s/v1/join", port);
    StompSessionHandler handler =
        new StompSessionHandlerAdapter() {
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
            log.error("exception tr ", exception);
          }
        };
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.add("Authorization", "Bearer " + tokenProvider.getToken());
    this.stompSession = stompClient.connect(url, headers, handler).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void startTheGame() throws ExecutionException, InterruptedException, TimeoutException {
    BoardState boardState = DataReader.getReadyToStartBoard();
    setPlayerState(boardState);
    boardStateRepo.save(boardState);
    this.stompSession.subscribe(
        "/topic/game/" + boardState.getBoardId(), new StartGameStompFrameHandler());
    StompHeaders headers = new StompHeaders();
    headers.add("boardId", String.valueOf(boardState.getBoardId()));
    headers.add("destination", "/app/game/action/start");
    Thread.sleep(1000L);
    this.stompSession.send(headers, null);

    String action = this.startedActionFuture.get(10, TimeUnit.SECONDS);

    String expectedAction = DataReader.getStartedAction();

    assertThat(expectedAction, equalTo(action));
    Optional<BoardState> boardStateActual = boardStateRepo.findById(boardState.getBoardId());
    assertThat(true, equalTo(boardStateActual.isPresent()));

    assertThat(true, equalTo(boardStateActual.get().isStarted()));
  }

  private class StartGameStompFrameHandler implements StompFrameHandler {
    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return String.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      startedActionFuture.complete((String) o);
    }
  }

  private void setPlayerState(BoardState board) {
    for (PlayerState player : board.getPlayers()) {
      player.setBoardState(board);
    }
  }
}
