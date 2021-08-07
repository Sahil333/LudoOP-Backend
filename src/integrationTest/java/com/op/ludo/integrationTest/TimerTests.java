package com.op.ludo.integrationTest;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import com.op.ludo.controllers.dto.websocket.GameStartDto;
import com.op.ludo.model.BoardState;
import java.util.concurrent.TimeUnit;
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
        final Long lastActionTime = boardState.getLastActionTime();

        // act
        boardClients.send("/app/game/action/start", new GameStartDto(boardState.getBoardId()), 0);

        // verify
        await().ignoreExceptions()
                .atMost(5, TimeUnit.SECONDS)
                .until(
                        () -> {
                            BoardState boardStateActual =
                                    boardStateRepo.findById(boardState.getBoardId()).get();
                            assertThat(boardStateActual.isRollPending(), equalTo(false));
                            assertThat(boardStateActual.isMovePending(), equalTo(true));
                            assertThat(
                                    boardStateActual.getLastActionTime(),
                                    greaterThan(lastActionTime));
                            assertThat(boardStateActual.getLastDiceRoll(), not(equalTo(-1)));
                            assertThat(
                                    boardStateActual
                                            .getPlayerState(boardClients.getPlayerId(0))
                                            .getTurnsMissed(),
                                    equalTo(1));
                            return true;
                        });

        BoardState boardStateActual = boardStateRepo.findById(boardState.getBoardId()).get();
        final Long lastActionTime2 = boardStateActual.getLastActionTime();

        await().ignoreExceptions()
                .atMost(5, TimeUnit.SECONDS)
                .until(
                        () -> {
                            BoardState boardStateActual2 =
                                    boardStateRepo.findById(boardState.getBoardId()).get();
                            assertThat(
                                    boardStateActual2.getWhoseTurn(),
                                    not(equalTo(boardClients.getPlayerId(0))));
                            assertThat(
                                    boardStateActual2.getWhoseTurn(),
                                    equalTo(boardClients.getPlayerId(1)));
                            assertThat(boardStateActual2.isRollPending(), equalTo(true));
                            assertThat(boardStateActual2.isMovePending(), equalTo(false));
                            assertThat(
                                    boardStateActual2.getLastActionTime(),
                                    greaterThan(lastActionTime2));
                            assertThat(boardStateActual2.getLastDiceRoll(), not(equalTo(-1)));
                            assertThat(
                                    boardStateActual2
                                            .getPlayerState(boardClients.getPlayerId(0))
                                            .getTurnsMissed(),
                                    equalTo(2));
                            return true;
                        });
    }
}
