package com.op.ludo.service;

import static com.op.ludo.model.BoardState.getNewStonePosition;
import static com.op.ludo.model.BoardState.isStoneMovePossible;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.BoardNotFoundException;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.*;
import com.op.ludo.game.handlers.chain.GameStartChain;
import com.op.ludo.game.handlers.chain.StoneMoveChain;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import com.op.ludo.util.DateTimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GamePlayService {

    @PersistenceContext EntityManager em;

    @Autowired PlayerStateRepo playerStateRepo;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired TimerService timerService;

    @Autowired StoneMoveChain stoneMoveChain;


    @Autowired GameStartChain gameStartChain;

    public List<AbstractAction> startFriendGame(GameStarted gameStarted) {
        List<AbstractAction> actions = new ArrayList<>();
        Optional<BoardState> boardOptional =
                boardStateRepo.findById(gameStarted.getArgs().getBoardId());
        if (boardOptional.isEmpty()) {
            throw new BoardNotFoundException(
                    "boardId=" + gameStarted.getArgs().getBoardId() + " not found");
        }
        BoardState board = boardOptional.get();
        gameStartChain.handleAction(board, gameStarted, actions);
        boardStateRepo.save(board);
        return actions;
    }
  
    public List<AbstractAction> rollDiceForPlayer(DiceRollReq diceRollReq) {
        List<AbstractAction> actionList = new ArrayList<>();
        PlayerState playerState = em.find(PlayerState.class, diceRollReq.getArgs().getPlayerId());
        BoardState boardState = playerState.getBoardState();
        if (!boardState.isRollPending()
                || !boardState.getWhoseTurn().equals(playerState.getPlayerId())) {
            throw new InvalidPlayerMoveException("Invalid dice roll request.");
        }
        DiceRoll diceRoll =
                new DiceRoll(
                        diceRollReq.getArgs().getPlayerId(), diceRollReq.getArgs().getBoardId());
        boardState.setLastDiceRoll(diceRoll.getArgs().getDiceRoll());
        boardState.setRollPending(false);
        boardState.setMovePending(true); // whose turn won't be updated here as it will remain same
        Long actionTime = DateTimeUtil.nowEpoch();
        boardState.setLastActionTime(actionTime);
        timerService.scheduleActionCheck(
                boardState.getBoardId(), playerState.getPlayerId(), actionTime);
        boardStateRepo.save(boardState);
        actionList.add(diceRoll);
        StoneMovePending stoneMovePending =
                new StoneMovePending(
                        diceRollReq.getArgs().getBoardId(), diceRollReq.getArgs().getPlayerId());
        actionList.add(stoneMovePending);
        return actionList;
    }

    public List<AbstractAction> updateBoardWithStoneMove(StoneMove stoneMove) {
        List<AbstractAction> actionList = new ArrayList<>();
        Long boardId = stoneMove.getArgs().getBoardId();
        BoardState boardState =
                boardStateRepo
                        .findById(boardId)
                        .orElseThrow(
                                () ->
                                        new BoardNotFoundException(
                                                "board=" + boardId + " not found"));
        stoneMoveChain.handleAction(boardState, stoneMove, actionList);
        boardStateRepo.save(boardState);
        return actionList;
    }

    public List<AbstractAction> missedDiceRollHandler(Long boardId, String playerId) {
        DiceRollReq diceRollReq = new DiceRollReq(boardId, playerId);
        PlayerState playerState = em.getReference(PlayerState.class, playerId);
        playerState.setTurnsMissed(playerState.getTurnsMissed() + 1);
        playerStateRepo.save(playerState);
        return rollDiceForPlayer(diceRollReq);
    }

    public List<AbstractAction> missedTurnHandler(Long boardId, String playerId) {
        List<AbstractAction> actionList = new ArrayList<>();
        PlayerState playerState = em.getReference(PlayerState.class, playerId);
        BoardState boardState = em.getReference(BoardState.class, boardId);
        List<Integer> movableStones = moveableStoneList(playerState, boardState);
        if (movableStones.size() == 0) {
            PlayerState nextPlayer = boardState.getNextPlayer(playerState);
            DiceRollPending diceRollPending =
                    new DiceRollPending(boardState.getBoardId(), nextPlayer.getPlayerId());
            actionList.add(diceRollPending);
            boardState.setRollPending(true);
            boardState.setMovePending(false);
            Long actionTime = DateTimeUtil.nowEpoch();
            boardState.setLastActionTime(actionTime);
            boardState.setWhoseTurn(nextPlayer.getPlayerId());
            timerService.scheduleActionCheck(
                    boardState.getBoardId(), nextPlayer.getPlayerId(), actionTime);
        } else {
            Integer moveRandomStoneNumber =
                    LobbyHelper.getRandomNumberInRange(0, movableStones.size() - 1);
            StoneMove randomStoneMove =
                    getNewStoneMove(
                            playerState, boardState, movableStones.get(moveRandomStoneNumber));
            actionList.addAll(updateBoardWithStoneMove(randomStoneMove));
        }
        playerState.setTurnsMissed(playerState.getTurnsMissed() + 1);
        playerStateRepo.save(playerState);
        boardStateRepo.save(boardState);
        return actionList;
    }

    public List<AbstractAction> getComputerStoneMoveActions(Long boardId, String playerId) {
        if (em.getReference(PlayerState.class, playerId)
                .getPlayerType()
                .equalsIgnoreCase("computer")) {
            List<AbstractAction> actionList = new ArrayList<>();
            // So this method will move that stone which has the max points will initialize the to
            // 100 and
            // if the coin is immovable we will set its value to -1 and will move the coin with
            // maximum points.
            BoardState boardState = em.getReference(BoardState.class, boardId);
            PlayerState playerState = em.getReference(PlayerState.class, playerId);
            Integer maxPoints = -1;
            Integer stoneWithMaxPoints = -1;
            for (int i = 1; i < 5; i++) {
                Integer currentPoints = getComputerStoneMovePoints(playerState, boardState, i);
                if (currentPoints > maxPoints) {
                    maxPoints = currentPoints;
                    stoneWithMaxPoints = i;
                }
            }
            // To do implement Stone Move for computer if possible other wise make diceRollPending
            // for other player.
            if (maxPoints == -1) {
                PlayerState nextPlayer = boardState.getNextPlayer(playerState);
                DiceRollPending diceRollPending =
                        new DiceRollPending(boardState.getBoardId(), nextPlayer.getPlayerId());
                actionList.add(diceRollPending);
                boardState.setRollPending(true);
                boardState.setMovePending(false);
                Long actionTime = DateTimeUtil.nowEpoch();
                boardState.setLastActionTime(actionTime);
                boardState.setWhoseTurn(nextPlayer.getPlayerId());
                timerService.scheduleActionCheck(
                        boardState.getBoardId(), nextPlayer.getPlayerId(), actionTime);
            } else {
                StoneMove stoneMove = getNewStoneMove(playerState, boardState, stoneWithMaxPoints);
                actionList.addAll(updateBoardWithStoneMove(stoneMove));
            }
            return actionList;
        }
        throw new InvalidBoardRequest("The player is not a computer.");
    }

    private Integer getComputerStoneMovePoints(
            PlayerState currentPlayerState, BoardState boardState, Integer stoneNumber) {
        Integer currentPosition = currentPlayerState.getDatabaseStonePosition(stoneNumber);
        Integer currentDiceRoll = boardState.getLastDiceRoll();
        if (isStoneMovePossible(currentPosition, currentDiceRoll)) {
            Integer currentPoints = 100;
            Integer finalPosition =
                    getNewStonePosition(
                            currentPosition, currentDiceRoll, currentPlayerState.getPlayerNumber());
            Integer finalPosOtherStoneCount =
                    getPositionOtherStoneCount(currentPlayerState, boardState, finalPosition);
            Integer finalPosMyStoneCount =
                    getPositionMyStoneCount(currentPlayerState, finalPosition);
            Integer currentPositionOtherStoneCount =
                    getPositionOtherStoneCount(currentPlayerState, boardState, currentPosition);
            Integer currentPisitionMyStoneCount =
                    getPositionMyStoneCount(currentPlayerState, currentPosition);
            Boolean isCurrentPositionSafe =
                    BoardState.isSafePosition(currentPosition)
                            || currentPisitionMyStoneCount - 1 + currentPositionOtherStoneCount > 0;
            Boolean isFinalPositionSafe =
                    BoardState.isSafePosition(finalPosition)
                            || finalPosMyStoneCount > 0
                            || finalPosMyStoneCount + finalPosOtherStoneCount > 1;
            Boolean isCutPossible = !isFinalPositionSafe && finalPosOtherStoneCount == 1;
            if (isCurrentPositionSafe) {
                currentPoints =
                        currentPoints
                                - 10; // penalising for moving the coin if current position is safe
                // position
            }
            if (isFinalPositionSafe) {
                currentPoints =
                        currentPoints
                                + 20; // highly awarding for moving the coin to a safe position
            }
            if (isCutPossible) {
                currentPoints = currentPoints + 25; // making the computer highly aggressive
            }
            // To do(only if current computer is less smart than 90% player let more than 50%
            // players win against computer):
            // make computer more smart and check if other player can cut my stone with one step on
            // initial position
            return currentPoints;
        }
        return -1;
    }

    public List<AbstractAction> getComputerDiceRollActions(Long boardId, String playerId) {
        if (em.getReference(PlayerState.class, playerId)
                .getPlayerType()
                .equalsIgnoreCase("computer")) {
            DiceRollReq diceRollReq = new DiceRollReq(boardId, playerId);
            return rollDiceForPlayer(diceRollReq);
        }
        throw new InvalidBoardRequest("The player is not a computer.");
    }

    private StoneMove getNewStoneMove(
            PlayerState playerState, BoardState boardState, Integer stoneNumber) {
        Integer playerNumber = playerState.getPlayerNumber();
        Integer diceRoll = boardState.getLastDiceRoll();
        Integer currentPosition;
        if (stoneNumber == 1) {
            currentPosition = playerState.getStone1();
        } else if (stoneNumber == 2) {
            currentPosition = playerState.getStone2();
        } else if (stoneNumber == 3) {
            currentPosition = playerState.getStone3();
        } else {
            currentPosition = playerState.getStone4();
        }
        Integer finalPosition = getNewStonePosition(currentPosition, diceRoll, playerNumber);
        return new StoneMove(
                boardState.getBoardId(),
                playerState.getPlayerId(),
                stoneNumber,
                currentPosition,
                finalPosition);
    }

    private Integer getPositionMyStoneCount(PlayerState playerState, Integer position) {
        Integer count = 0;
        if (playerState.getStone1().equals(position)) {
            count++;
        }
        if (playerState.getStone2().equals(position)) {
            count++;
        }
        if (playerState.getStone3().equals(position)) {
            count++;
        }
        if (playerState.getStone4().equals(position)) {
            count++;
        }
        return count;
    }

    private Integer getPositionOtherStoneCount(
            PlayerState currentPlayerState, BoardState boardState, Integer position) {
        List<PlayerState> playerStates = boardState.getPlayers();
        Integer count = 0;
        for (PlayerState playerState : playerStates) {
            if (playerState.getPlayerId().equals(currentPlayerState.getPlayerId())) {
                continue;
            }
            if (playerState.getStone1().equals(position)) {
                count++;
            }
            if (playerState.getStone2().equals(position)) {
                count++;
            }
            if (playerState.getStone3().equals(position)) {
                count++;
            }
            if (playerState.getStone4().equals(position)) {
                count++;
            }
        }
        return count;
    }

    private List<Integer> moveableStoneList(PlayerState playerState, BoardState boardState) {
        List<Integer> list = new ArrayList<>();
        Integer diceRoll = boardState.getLastDiceRoll();
        if (isStoneMovePossible(playerState.getStone1(), diceRoll)) {
            list.add(1);
        }
        if (isStoneMovePossible(playerState.getStone2(), diceRoll)) {
            list.add(2);
        }
        if (isStoneMovePossible(playerState.getStone3(), diceRoll)) {
            list.add(3);
        }
        if (isStoneMovePossible(playerState.getStone4(), diceRoll)) {
            list.add(4);
        }

        return list;
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void blockAllBoardMoves(BoardState boardState) {
        boardState.setMovePending(false);
        boardState.setRollPending(false);
        boardStateRepo.save(boardState);
    }
}
