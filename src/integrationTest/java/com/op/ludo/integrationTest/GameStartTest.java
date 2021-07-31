package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.model.BoardState;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class GameStartTest extends BaseIntegrationTest {

    @Test
    public void startTheGame() throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        BoardState boardState = DataReader.getReadyToStartBoard();
        setPlayerState(boardState);
        boardStateRepo.save(boardState);

        setupBoardStompClients(boardState.getBoardId());

        // act
        boardClients.send("/app/game/action/start", new GameStartDto(boardState.getBoardId()), 0);

        // verify
        ActionsWithBoardState expectedAction = DataReader.getStartedAction();
        // user1
        ActionsWithBoardState action = boardClients.getMessage(0, 300, ActionsWithBoardState.class);
        assertThat(action, equalTo(expectedAction));

        // user2
        action = boardClients.getMessage(1, 300, ActionsWithBoardState.class);
        assertThat(action, equalTo(expectedAction));

        // user3
        action = boardClients.getMessage(2, 300, ActionsWithBoardState.class);
        assertThat(action, equalTo(expectedAction));

        // user4
        action = boardClients.getMessage(3, 300, ActionsWithBoardState.class);
        assertThat(action, equalTo(expectedAction));

        Optional<BoardState> boardStateActual = boardStateRepo.findById(boardState.getBoardId());
        assertThat(boardStateActual.isPresent(), equalTo(true));
        assertThat(boardStateActual.get().isStarted(), equalTo(true));
    }
}
