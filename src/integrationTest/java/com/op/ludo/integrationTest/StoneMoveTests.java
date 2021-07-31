package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.StoneMoveDto;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.integrationTest.helper.DataReader;
import com.op.ludo.model.BoardState;
import com.op.ludo.util.DateTimeUtil;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class StoneMoveTests extends BaseIntegrationTest {

    @Test
    public void firstMove() throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        BoardState boardState = DataReader.getStartedBoard();
        setPlayerState(boardState);
        boardState.setLastDiceRoll(6);
        boardState.setWhoseTurn(1);
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        setupBoardStompClients(boardState.getBoardId());

        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 3, -13, 1),
                0);

        ActionsWithBoardState actions =
                boardClients.getMessage(0, 300000, ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(2));
        assertThat(
                actions.getActions().get(0),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(0), 3, -13, 1)));

        assertThat(
                actions.getActions().get(1),
                equalTo(
                        new DiceRollPending(
                                boardState.getBoardId(), boardClients.getPlayerId(0), 1)));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.isRollPending(), equalTo(true));
        assertThat(boardState.isMovePending(), equalTo(false));
        assertThat(boardState.getWhoseTurn(), equalTo(1));
        assertThat(boardState.getPlayerState(1).getStone3(), equalTo(1));

        assertThat(actions.getBoard().isRollPending(), equalTo(true));
        assertThat(actions.getBoard().isMovePending(), equalTo(false));
        assertThat(actions.getBoard().getWhoseTurn(), equalTo(1));
        assertThat(actions.getBoard().getPlayerState(1).getStone3(), equalTo(1));
    }
}
