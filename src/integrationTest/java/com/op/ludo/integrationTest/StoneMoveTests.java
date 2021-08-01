package com.op.ludo.integrationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.op.ludo.controllers.advice.GameErrorResponse;
import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.controllers.dto.websocket.StoneMoveDto;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.model.BoardState;
import com.op.ludo.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class StoneMoveTests extends BaseIntegrationTest {

    @Test
    public void firstMove() {
        // setup
        BoardState boardState = setupAStartedBoard();

        boardState.setLastDiceRoll(6);
        boardState.setWhoseTurn(boardClients.getPlayerId(0));
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 3, -13, 1),
                0);

        // verify
        ActionsWithBoardState actions = checkAndReturnActions(ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(2));
        assertThat(
                actions.getActions().get(0),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(0), 3, -13, 1)));

        assertThat(
                actions.getActions().get(1),
                equalTo(new DiceRollPending(boardState.getBoardId(), boardClients.getPlayerId(0))));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.isRollPending(), equalTo(true));
        assertThat(boardState.isMovePending(), equalTo(false));
        assertThat(boardState.getWhoseTurn(), equalTo(boardClients.getPlayerId(0)));
        assertThat(boardState.getPlayerState(boardClients.getPlayerId(0)).getStone3(), equalTo(1));

        assertThat(actions.getBoard().isRollPending(), equalTo(true));
        assertThat(actions.getBoard().isMovePending(), equalTo(false));
        assertThat(actions.getBoard().getWhoseTurn(), equalTo(boardClients.getPlayerId(0)));
        assertThat(
                actions.getBoard().getPlayer(boardClients.getPlayerId(0)).getStone3(), equalTo(1));
    }

    @Test
    public void checkMoves() {
        // setup1
        // Move stone from somewhere in the middle
        BoardState boardState = setupAStartedBoard();

        boardState.setLastDiceRoll(4);
        boardState.setWhoseTurn(boardClients.getPlayerId(0));
        boardState.getPlayerState(boardClients.getPlayerId(0)).setStone1(15);
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 1, 15, 19),
                0);

        // verify
        ActionsWithBoardState actions = checkAndReturnActions(ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(2));
        assertThat(
                actions.getActions().get(0),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(0), 1, 15, 19)));
        assertThat(
                actions.getActions().get(1),
                equalTo(new DiceRollPending(boardState.getBoardId(), boardClients.getPlayerId(1))));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.getWhoseTurn(), equalTo(boardClients.getPlayerId(1)));
        assertThat(boardState.getPlayerState(boardClients.getPlayerId(0)).getStone1(), equalTo(19));
        assertThat(boardState.isRollPending(), equalTo(true));
        assertThat(boardState.isMovePending(), equalTo(false));

        assertThat(actions.getBoard().getWhoseTurn(), equalTo(boardClients.getPlayerId(1)));
        assertThat(
                actions.getBoard().getPlayer(boardClients.getPlayerId(0)).getStone1(), equalTo(19));
        assertThat(actions.getBoard().isRollPending(), equalTo(true));
        assertThat(actions.getBoard().isMovePending(), equalTo(false));

        // setup2
        // check cut move
        boardState.setLastDiceRoll(3);
        boardState.getPlayerState(boardClients.getPlayerId(1)).setStone3(16);
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 3, 16, 19),
                1);

        // verify
        actions = checkAndReturnActions(ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(3));
        assertThat(
                actions.getActions().get(0),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(1), 3, 16, 19)));

        assertThat(
                actions.getActions().get(1),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(0), 1, 19, -11)));

        assertThat(
                actions.getActions().get(2),
                equalTo(new DiceRollPending(boardState.getBoardId(), boardClients.getPlayerId(1))));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.getWhoseTurn(), equalTo(boardClients.getPlayerId(1)));
        assertThat(boardState.getPlayerState(boardClients.getPlayerId(1)).getStone3(), equalTo(19));
        assertThat(
                boardState.getPlayerState(boardClients.getPlayerId(0)).getStone1(), equalTo(-11));
        assertThat(boardState.isRollPending(), equalTo(true));
        assertThat(boardState.isMovePending(), equalTo(false));

        assertThat(actions.getBoard().getWhoseTurn(), equalTo(boardClients.getPlayerId(1)));
        assertThat(
                actions.getBoard().getPlayer(boardClients.getPlayerId(1)).getStone3(), equalTo(19));
        assertThat(
                actions.getBoard().getPlayer(boardClients.getPlayerId(0)).getStone1(),
                equalTo(-11));
        assertThat(actions.getBoard().isRollPending(), equalTo(true));
        assertThat(actions.getBoard().isMovePending(), equalTo(false));

        // setup3
        // check home way
        boardState.setLastDiceRoll(5);
        boardState.getPlayerState(boardClients.getPlayerId(1)).setStone2(8);
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 2, 8, 121),
                1);

        // verify
        actions = checkAndReturnActions(ActionsWithBoardState.class);

        assertThat(actions.getActions().size(), equalTo(2));

        assertThat(
                actions.getActions().get(0),
                equalTo(
                        new StoneMove(
                                boardState.getBoardId(), boardClients.getPlayerId(1), 2, 8, 121)));

        assertThat(
                actions.getActions().get(1),
                equalTo(new DiceRollPending(boardState.getBoardId(), boardClients.getPlayerId(2))));

        boardState = boardStateRepo.findById(boardState.getBoardId()).get();
        assertThat(boardState.getWhoseTurn(), equalTo(boardClients.getPlayerId(2)));
        assertThat(
                boardState.getPlayerState(boardClients.getPlayerId(1)).getStone2(), equalTo(121));
        assertThat(boardState.isRollPending(), equalTo(true));
        assertThat(boardState.isMovePending(), equalTo(false));

        assertThat(actions.getBoard().getWhoseTurn(), equalTo(boardClients.getPlayerId(2)));
        assertThat(
                actions.getBoard().getPlayer(boardClients.getPlayerId(1)).getStone2(),
                equalTo(121));
        assertThat(actions.getBoard().isRollPending(), equalTo(true));
        assertThat(actions.getBoard().isMovePending(), equalTo(false));
    }

    @Test
    public void errorScenarios() {
        // setup1
        // Dice roll is not six but move is made on base stone
        BoardState boardState = setupAStartedBoard();
        boardState.setLastDiceRoll(4);
        boardState.setWhoseTurn(boardClients.getPlayerId(0));
        boardState.setMovePending(true);
        boardState.setRollPending(false);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 3, -13, 1),
                0);

        // verify
        GameErrorResponse error = boardClients.getUserErrorMessage(0, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Player move not possible"));
        assertThat(error.getDestination(), equalTo("/app/game/action/moveStone"));

        // setup2
        // player move from a player who doesn't have a turn
        boardState.setWhoseTurn(boardClients.getPlayerId(2));
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 3, -13, 1),
                0);

        // verify
        error = boardClients.getUserErrorMessage(0, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Turn not valid."));
        assertThat(error.getDestination(), equalTo("/app/game/action/moveStone"));

        // setup3
        // player move from a invalid initial position
        boardState.setWhoseTurn(boardClients.getPlayerId(3));
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 4, 21, 24),
                3);

        // verify
        error = boardClients.getUserErrorMessage(3, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Invalid initial position"));
        assertThat(error.getDestination(), equalTo("/app/game/action/moveStone"));

        // setup4
        // player move to a invalid final position
        boardState.setLastDiceRoll(5);
        boardState.setWhoseTurn(boardClients.getPlayerId(3));
        boardState.getPlayerState(boardClients.getPlayerId(3)).setStone1(15);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 1, 15, 17),
                3);

        // verify
        error = boardClients.getUserErrorMessage(3, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Move is not valid for stone=1"));
        assertThat(error.getDestination(), equalTo("/app/game/action/moveStone"));

        // setup4
        // move is not possible with current dice roll on given stone
        boardState.setLastDiceRoll(5);
        boardState.setWhoseTurn(boardClients.getPlayerId(3));
        boardState.getPlayerState(boardClients.getPlayerId(3)).setStone1(385);
        boardState.setLastActionTime(DateTimeUtil.nowEpoch());
        boardStateRepo.save(boardState);

        // action
        boardClients.send(
                "/app/game/action/moveStone",
                new StoneMoveDto(boardState.getBoardId(), 1, 385, 390),
                3);

        // verify
        error = boardClients.getUserErrorMessage(3, 300, GameErrorResponse.class);

        assertThat(error.getMessage(), equalTo("Player move not possible"));
        assertThat(error.getDestination(), equalTo("/app/game/action/moveStone"));
    }
}
