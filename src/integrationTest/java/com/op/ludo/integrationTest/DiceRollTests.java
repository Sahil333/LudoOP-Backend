package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.op.ludo.controllers.advice.GameErrorResponse;
import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.DiceRollDto;
import com.op.ludo.game.action.impl.DiceRoll;
import com.op.ludo.game.action.impl.StoneMovePending;
import com.op.ludo.model.BoardState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DiceRollTests extends BaseIntegrationTest {

    @Test
    public void firstDiceRoll() {
        BoardState boardState = setupAStartedBoard();

        // action
        boardClients.send("/app/game/action/diceRoll", new DiceRollDto(boardState.getBoardId()), 0);

        // verify
        ActionsWithBoardState actions = checkAndReturnActions(ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(2));

        assertThat(actions.getActions().get(0), instanceOf(DiceRoll.class));
        DiceRoll diceRoll = (DiceRoll) actions.getActions().get(0);
        assertThat(diceRoll.getArgs().getPlayerId(), equalTo(boardClients.getPlayerId(0)));
        assertThat(diceRoll.getArgs().getDiceRoll(), is(both(greaterThan(0)).and(lessThan(7))));

        assertThat(
                actions.getActions().get(1),
                equalTo(
                        new StoneMovePending(
                                boardState.getBoardId(), boardClients.getPlayerId(0))));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.getLastDiceRoll(), equalTo(diceRoll.getArgs().getDiceRoll()));
        assertThat(boardState.isMovePending(), equalTo(true));
        assertThat(boardState.isRollPending(), equalTo(false));
        assertThat(boardState.getWhoseTurn(), equalTo(boardClients.getPlayerId(0)));

        assertThat(actions.getBoard().getLastDiceRoll(), equalTo(diceRoll.getArgs().getDiceRoll()));
        assertThat(actions.getBoard().isMovePending(), equalTo(true));
        assertThat(actions.getBoard().isRollPending(), equalTo(false));
        assertThat(actions.getBoard().getWhoseTurn(), equalTo(boardClients.getPlayerId(0)));
    }

    @Test
    public void errorCases() {
        // setup1
        // dice roll from a player who doesn't have the turn
        BoardState boardState = setupAStartedBoard();

        // action
        boardClients.send("/app/game/action/diceRoll", new DiceRollDto(boardState.getBoardId()), 1);

        // verify
        GameErrorResponse error = boardClients.getUserErrorMessage(1, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Invalid dice roll request."));
        assertThat(error.getDestination(), equalTo("/app/game/action/diceRoll"));

        // setup2
        // dice roll when already rolled
        boardState.setLastDiceRoll(3);
        boardState.setRollPending(false);
        boardState.setMovePending(true);
        boardStateRepo.save(boardState);
        boardClients.send("/app/game/action/diceRoll", new DiceRollDto(boardState.getBoardId()), 1);

        // verify
        error = boardClients.getUserErrorMessage(1, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Invalid dice roll request."));
        assertThat(error.getDestination(), equalTo("/app/game/action/diceRoll"));
    }
}
