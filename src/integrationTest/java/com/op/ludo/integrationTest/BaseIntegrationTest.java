package com.op.ludo.integrationTest;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.integrationTest.helper.AppObjectMapper;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.integrationTest.helper.FirebaseTokenProvider;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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

    protected BoardStompClients setupBoardStompClients(Long boardId)
            throws InterruptedException, ExecutionException, TimeoutException {
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
}
