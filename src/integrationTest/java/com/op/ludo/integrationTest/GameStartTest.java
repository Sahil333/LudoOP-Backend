package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.model.BoardState;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class GameStartTest extends BaseIntegrationTest {

    @Test
    public void startTheGame() {
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
        ActionsWithBoardState action = checkAndReturnActions(ActionsWithBoardState.class);
        assertThat(action, equalTo(expectedAction));

        Optional<BoardState> boardStateActual = boardStateRepo.findById(boardState.getBoardId());
        assertThat(boardStateActual.isPresent(), equalTo(true));
        assertThat(boardStateActual.get().isStarted(), equalTo(true));
    }
}
