package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.model.BoardState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class TimerTests extends BaseIntegrationTest {

    @BeforeEach
    public void enableTimer() {
        timerService.setEnableTimer(true);
    }

    @AfterEach
    public void disableTimer() {
        timerService.setEnableTimer(false);
    }

    @Test
    public void startOfTheGame() throws InterruptedException {
        // setup
        // should enable timer at the start of the game and take turn if not taken already
        BoardState boardState = setupAReadyBoard();
        Long lastActionTime = boardState.getLastActionTime();

        // act
        boardClients.send("/app/game/action/start", new GameStartDto(boardState.getBoardId()), 0);
        Thread.sleep(1200);

        // verify
        BoardState boardStateActual = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardStateActual.isRollPending(), equalTo(false));
        assertThat(boardStateActual.isMovePending(), equalTo(true));
        assertThat(boardStateActual.getLastActionTime(), greaterThan(lastActionTime));
        assertThat(boardStateActual.getLastDiceRoll(), not(equalTo(-1)));
        assertThat(
                boardStateActual.getPlayerState(boardClients.getPlayerId(0)).getTurnsMissed(),
                equalTo(1));

        lastActionTime = boardStateActual.getLastActionTime();
        Thread.sleep(1200);

        boardStateActual = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardStateActual.getWhoseTurn(), not(equalTo(boardClients.getPlayerId(0))));
        assertThat(boardStateActual.getWhoseTurn(), equalTo(boardClients.getPlayerId(1)));
        assertThat(boardStateActual.isRollPending(), equalTo(true));
        assertThat(boardStateActual.isMovePending(), equalTo(false));
        assertThat(boardStateActual.getLastActionTime(), greaterThan(lastActionTime));
        assertThat(boardStateActual.getLastDiceRoll(), not(equalTo(-1)));
        assertThat(
                boardStateActual.getPlayerState(boardClients.getPlayerId(0)).getTurnsMissed(),
                equalTo(2));
    }
}
