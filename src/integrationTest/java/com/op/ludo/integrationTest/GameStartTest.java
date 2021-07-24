package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.integrationTest.helper.FirebaseTokenProvider;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class GameStartTest {

    private static final String socketEndpoint = "ws://localhost:%s/v1/join";

    @LocalServerPort private Integer port;

    private BoardStompClients boardClients;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired FirebaseTokenProvider tokenProvider;

    @AfterEach
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        if (boardClients != null) boardClients.stopClients();
    }

    @Test
    public void startTheGame() throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        BoardState boardState = DataReader.getReadyToStartBoard();
        setPlayerState(boardState);
        boardStateRepo.save(boardState);

        List<BoardStompClients.UserCredentials> credentials = DataReader.getCredentialsList();
        String endpoint = String.format(socketEndpoint, port);
        boardClients =
                new BoardStompClients(
                        boardState.getBoardId(), credentials, endpoint, tokenProvider);
        // To let all subscription to be finished first, sleep
        Thread.sleep(500L);

        // act
        StompHeaders headers = new StompHeaders();
        headers.add("boardId", String.valueOf(boardState.getBoardId()));
        headers.add("destination", "/app/game/action/start");
        boardClients.send(headers, null, 0);

        // verify
        String expectedAction = DataReader.getStartedAction();
        // user1
        String action = boardClients.getMessage(0, 300);
        assertThat(action, equalTo(expectedAction));

        // user2
        action = boardClients.getMessage(1, 300);
        assertThat(action, equalTo(expectedAction));

        // user3
        action = boardClients.getMessage(2, 300);
        assertThat(action, equalTo(expectedAction));

        // user4
        action = boardClients.getMessage(3, 300);
        assertThat(action, equalTo(expectedAction));

        Optional<BoardState> boardStateActual = boardStateRepo.findById(boardState.getBoardId());
        assertThat(boardStateActual.isPresent(), equalTo(true));
        assertThat(boardStateActual.get().isStarted(), equalTo(true));
    }

    private void setPlayerState(BoardState board) {
        for (PlayerState player : board.getPlayers()) {
            player.setBoardState(board);
        }
    }
}
