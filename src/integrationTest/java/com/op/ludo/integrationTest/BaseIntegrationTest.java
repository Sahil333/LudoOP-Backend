package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.integrationTest.helper.AppObjectMapper;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.integrationTest.helper.FirebaseTokenProvider;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public abstract class BaseIntegrationTest {

    private static final String socketEndpoint = "ws://localhost:%s/v1/join";

    @LocalServerPort private Integer port;

    protected BoardStompClients boardClients;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired FirebaseTokenProvider tokenProvider;

    @AfterEach
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        if (boardClients != null) boardClients.stopClients();
        boardStateRepo.deleteAll();
    }

    @SneakyThrows
    protected BoardStompClients setupBoardStompClients(Long boardId) {
        List<BoardStompClients.UserCredentials> credentials = DataReader.getCredentialsList();
        String endpoint = String.format(socketEndpoint, port);
        boardClients =
                new BoardStompClients(
                        boardId,
                        credentials,
                        endpoint,
                        tokenProvider,
                        AppObjectMapper.objectMapper());
        // To let all subscription to be finished first, sleep
        Thread.sleep(500L);
        return boardClients;
    }

    protected void setPlayerState(BoardState board) {
        for (PlayerState player : board.getPlayers()) {
            player.setBoardState(board);
        }
    }

    protected <T> T checkAndReturnActions(Class<T> targetClass) {
        return checkAndReturnActions(targetClass, 500);
    }

    protected <T> T checkAndReturnActions(Class<T> targetClass, long timeout) {
        T actions = boardClients.getMessage(0, timeout, targetClass);

        T actions2 = boardClients.getMessage(1, timeout, targetClass);

        T actions3 = boardClients.getMessage(2, timeout, targetClass);

        T actions4 = boardClients.getMessage(3, timeout, targetClass);

        assertThat(actions, equalTo(actions2));
        assertThat(actions, equalTo(actions3));
        assertThat(actions, equalTo(actions4));

        return actions;
    }

    protected BoardState setupAStartedBoard() {
        BoardState boardState = DataReader.getStartedBoard();
        setPlayerState(boardState);
        boardStateRepo.save(boardState);

        setupBoardStompClients(boardState.getBoardId());
        return boardState;
    }
}
