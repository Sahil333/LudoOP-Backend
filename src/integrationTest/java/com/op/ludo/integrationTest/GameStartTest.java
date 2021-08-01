package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.GameStarted;
import com.op.ludo.model.BoardState;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class GameStartTest extends BaseIntegrationTest {

    @Test
    public void startTheGame() {
        // setup
        BoardState boardState = setupAReadyBoard();

        // act
        boardClients.send("/app/game/action/start", new GameStartDto(boardState.getBoardId()), 0);

        // verify
        // user1
        ActionsWithBoardState action = checkAndReturnActions(ActionsWithBoardState.class);
        assertThat(action.getActions().size(), equalTo(2));

        assertThat(
                action.getActions().get(0),
                equalTo(new GameStarted(boardState.getBoardId(), boardClients.getPlayerId(0))));

        assertThat(
                action.getActions().get(1),
                equalTo(new DiceRollPending(boardState.getBoardId(), boardClients.getPlayerId(0))));

        Optional<BoardState> boardStateActual = boardStateRepo.findById(boardState.getBoardId());
        assertThat(boardStateActual.isPresent(), equalTo(true));
        assertThat(boardStateActual.get().isStarted(), equalTo(true));
    }
}
