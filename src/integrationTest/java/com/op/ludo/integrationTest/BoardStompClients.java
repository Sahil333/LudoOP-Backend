package com.op.ludo.integrationTest;

import static org.awaitility.Awaitility.await;

import com.op.ludo.integrationTest.helper.FirebaseTokenProvider;
import com.op.ludo.integrationTest.helper.JsonStringMessageConverter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/*
- should setup the stomp clients for the given user and the board
- should be able to send an action from a given user
- should be able to check if other users got the actions in their subscription
- should be able to check error in the sender queue
*/

@Slf4j
public class BoardStompClients {

    private final List<StompSession> boardClients;
    private final Long boardId;
    private final String socketEndpoint;
    private final List<UserCredentials> users;
    private final Map<String, BlockingQueue<String>> boardMessages;
    private final Map<String, BlockingQueue<String>> userErrorMessages;

    private final FirebaseTokenProvider tokenProvider;

    public BoardStompClients(
            Long boardId,
            List<UserCredentials> users,
            String socketEndpoint,
            FirebaseTokenProvider tokenProvider)
            throws ExecutionException, InterruptedException, TimeoutException {
        this.boardId = boardId;
        this.users = users;
        this.socketEndpoint = socketEndpoint;
        this.tokenProvider = tokenProvider;
        this.boardClients = new ArrayList<>();
        this.boardMessages = new HashMap<>();
        this.userErrorMessages = new HashMap<>();
        setUpClients();
    }

    private void setUpClients() throws ExecutionException, InterruptedException, TimeoutException {
        for (UserCredentials user : users) {
            Map<String, String> token = tokenProvider.getToken(user.getEmail(), user.getPassword());
            user.setUid(token.get("localId"));
            boardMessages.put(user.getUid(), new LinkedBlockingQueue<>());
            userErrorMessages.put(user.getUid(), new LinkedBlockingQueue<>());
            setupClient(token);
        }
    }

    private void setupClient(Map<String, String> token)
            throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        new SockJsClient(
                                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new JsonStringMessageConverter());
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + token.get("idToken"));
        StompSession session =
                stompClient
                        .connect(
                                socketEndpoint,
                                headers,
                                new BoardStompSessionHandler(token.get("localId")))
                        .get(1, TimeUnit.SECONDS);
        this.boardClients.add(session);
        session.subscribe(
                "/topic/game/" + boardId, new GameStompFrameHandler(token.get("localId")));
        session.subscribe(
                "/user/queue/errors", new GameErrorStompFrameHandler(token.get("localId")));
    }

    public void send(StompHeaders headers, Object payload, int userIndex) {
        StompSession session = this.boardClients.get(userIndex);
        session.send(headers, payload);
    }

    public void send(String destination, Object payload, int userIndex) {
        StompSession session = this.boardClients.get(userIndex);
        session.send(destination, payload);
    }

    public String getMessage(int userIndex, long timeout) {
        AtomicReference<String> message = new AtomicReference<>();
        await().atMost(timeout, TimeUnit.MILLISECONDS)
                .until(
                        () -> {
                            String m =
                                    boardMessages
                                            .get(users.get(userIndex).getUid())
                                            .poll(100, TimeUnit.MILLISECONDS);
                            message.set(m);
                            return m != null;
                        });
        return message.get();
    }

    public String getUserErrorMessage(int userIndex, long timeout) {
        AtomicReference<String> message = new AtomicReference<>();
        await().atMost(timeout, TimeUnit.MILLISECONDS)
                .until(
                        () -> {
                            String m =
                                    userErrorMessages
                                            .get(users.get(userIndex).getUid())
                                            .poll(100, TimeUnit.MILLISECONDS);
                            message.set(m);
                            return m != null;
                        });
        return message.get();
    }

    public void stopClients() {
        for (StompSession session : boardClients) {
            session.disconnect();
        }
    }

    @NoArgsConstructor
    @Getter
    public static class UserCredentials {
        private String email;
        private String password;
        @Setter private String uid;
    }

    private class BoardStompSessionHandler extends StompSessionHandlerAdapter {
        private String uid;

        public BoardStompSessionHandler(String uid) {
            this.uid = uid;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("connected {}", uid);
        }

        @Override
        public void handleException(
                StompSession session,
                StompCommand command,
                StompHeaders headers,
                byte[] payload,
                Throwable exception) {
            log.error("exception for {}", uid, exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            log.error("exception in transport for {}", uid, exception);
        }
    }

    private class GameStompFrameHandler implements StompFrameHandler {
        private String uid;

        public GameStompFrameHandler(String uid) {
            this.uid = uid;
        }

        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            boardMessages.get(uid).add((String) o);
        }
    }

    private class GameErrorStompFrameHandler implements StompFrameHandler {
        private String uid;

        public GameErrorStompFrameHandler(String uid) {
            this.uid = uid;
        }

        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            userErrorMessages.get(uid).add((String) o);
        }
    }
}
