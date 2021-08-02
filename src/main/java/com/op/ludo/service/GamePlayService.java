package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.BoardNotFoundException;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.*;
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

    @Autowired LobbyService lobbyService;

    @PersistenceContext EntityManager em;

    @Autowired PlayerStateRepo playerStateRepo;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired TimerService timerService;

    public List<AbstractAction> startFriendGame(Long boardId, String playerId) {
        List<AbstractAction> actions = new ArrayList<>();
        Optional<BoardState> boardOptional = boardStateRepo.findById(boardId);
        if (boardOptional.isEmpty()) {
            throw new BoardNotFoundException("boardId=" + boardId + " not found");
        }
        BoardState board = boardOptional.get();
        if (canStartGame(playerId, board)) {
            doStartGame(board);
            actions.add(new GameStarted(boardId, playerId));
            AbstractAction playerTurn = new DiceRollPending(boardId, board.getWhoseTurn());
            actions.add(playerTurn);
            board.setRollPending(true);
            board.setMovePending(false);
            Long actionTime = DateTimeUtil.nowEpoch();
            board.setLastActionTime(actionTime);
            timerService.scheduleActionCheck(boardId, board.getWhoseTurn(), actionTime);
            boardStateRepo.save(board);
        } else {
            throw new InvalidBoardRequest("Cannot start Game.");
        }
        return actions;
    }

    List<AbstractAction> getEndGameActions(BoardState boardState) {
        List<AbstractAction> abstractActions = new ArrayList<>();
        if (hasGameFinished(boardState)) {
            GameEnded gameEnded = new GameEnded(getPlayerPositions(boardState));
            abstractActions.add(gameEnded);
            boardState.setEnded(true);
            boardState.setEndTime(DateTimeUtil.nowEpoch());
            boardStateRepo.save(boardState);
        }
        return abstractActions;
    }

    private List<GameEnded.PlayerPosition> getPlayerPositions(BoardState boardState) {
        List<PlayerState> playerStates = boardState.getPlayers();
        List<GameEnded.PlayerPosition> playerPositions = new ArrayList<>();
        for (PlayerState playerState : playerStates) {
            GameEnded.PlayerPosition playerPosition =
                    new GameEnded.PlayerPosition(
                            playerState.getPlayerId(),
                            playerState.getPlayerPosition(),
                            boardState.getBid());
            playerPositions.add(playerPosition);
        }
        return playerPositions;
    }

    private Boolean hasGameFinished(
            BoardState boardState) { // pass final board state after stone move has been done.
        List<PlayerState> playerStates = boardState.getPlayers();
        return getFinishedPlayerCount(playerStates) == playerStates.size() - 1;
    }

    private Integer getFinishedPlayerCount(List<PlayerState> playerStates) {
        int count = 0;
        for (PlayerState playerState : playerStates) {
            if (playerState.getHomeCount() == 4 || !playerState.isPlayerActive()) {
                count++;
            }
        }
        return count;
    }

    private void doStartGame(BoardState boardState) {
        Long startTime = DateTimeUtil.nowEpoch();
        boardState.setStartTime(startTime);
        boardState.setLastActionTime(startTime);
        boardState.setStarted(true);
    }

    private boolean canStartGame(String playerId, BoardState board) {
        return !board.isStarted()
                && isPlayerInGame(playerId, board)
                && board.getPlayers().get(0).getPlayerId().equals(playerId);
    }

    public boolean isPlayerInGame(String playerId, BoardState board) {
        Optional<PlayerState> player =
                board.getPlayers().stream()
                        .filter(p -> p.getPlayerId().equals(playerId))
                        .findFirst();
        return player.isPresent();
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
        if (!isStoneMoveValid(stoneMove)) {
            throw new InvalidPlayerMoveException(
                    "Move is not valid for stone=" + stoneMove.getArgs().getStoneNumber());
        }
        actionList.add(stoneMove);
        boolean hasCutStone = false;
        if (!isSafePosition(stoneMove.getArgs().getFinalPosition())
                && getFinalPositionStoneCount(stoneMove) == 1) {
            hasCutStone = true;
            StoneMove cutStoneMove = getFinalPositionCutStoneMove(stoneMove);
            actionList.add(cutStoneMove);
            updateStoneMoveInDB(cutStoneMove);
        }
        updateStoneMoveInDB(stoneMove);
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMove.getArgs().getPlayerId());
        BoardState boardState = em.getReference(BoardState.class, stoneMove.getArgs().getBoardId());
        List<AbstractAction> endGameActions = getEndGameActions(boardState);
        if (endGameActions.size() > 0) {
            actionList.addAll(endGameActions);
            return actionList;
        }
        boardState.setRollPending(true);
        boardState.setMovePending(false);
        DiceRollPending diceRollPending;
        String diceRollPlayerId;
        if (boardState.getLastDiceRoll() == 6 || hasCutStone) {
            diceRollPlayerId = playerState.getPlayerId();
            diceRollPending =
                    new DiceRollPending(
                            stoneMove.getArgs().getBoardId(), stoneMove.getArgs().getPlayerId());
        } else {
            PlayerState nextPlayer = getNextPlayer(playerState, boardState);
            diceRollPlayerId = nextPlayer.getPlayerId();
            diceRollPending =
                    new DiceRollPending(boardState.getBoardId(), nextPlayer.getPlayerId());
            boardState.setWhoseTurn(nextPlayer.getPlayerId());
        }
        Long actionTime = DateTimeUtil.nowEpoch();
        boardState.setLastActionTime(actionTime);
        boardState.setWhoseTurn(diceRollPlayerId);
        timerService.scheduleActionCheck(boardState.getBoardId(), diceRollPlayerId, actionTime);
        actionList.add(diceRollPending);
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
            PlayerState nextPlayer = getNextPlayer(playerState, boardState);
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

    public Boolean isStoneMoveValid(StoneMove stoneMove) throws InvalidPlayerMoveException {
        if (!lobbyService.isPlayerAlreadyPartOfGame(stoneMove.getArgs().getPlayerId())) {
            throw new InvalidPlayerMoveException("Player is not part of the game.");
        } else if (stoneMove.getArgs().getStoneNumber() < 1
                || stoneMove.getArgs().getStoneNumber() > 4) {
            throw new InvalidPlayerMoveException("Invalid stone number.");
        }
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMove.getArgs().getPlayerId());
        BoardState boardState = playerState.getBoardState();
        Integer currentDBPosition =
                getDatabaseStonePosition(playerState, stoneMove.getArgs().getStoneNumber());
        if (!playerState.getPlayerId().equals(boardState.getWhoseTurn())
                || !boardState.isMovePending()
                || !boardState.getBoardId().equals(stoneMove.getArgs().getBoardId())) {
            throw new InvalidPlayerMoveException("Turn not valid.");
        } else if (!currentDBPosition.equals(stoneMove.getArgs().getInitialPosition())) {
            throw new InvalidPlayerMoveException("Invalid initial position");
        }
        return getNewStonePosition(
                        currentDBPosition,
                        boardState.getLastDiceRoll(),
                        playerState.getPlayerNumber())
                .equals(stoneMove.getArgs().getFinalPosition());
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

    private PlayerState getNextPlayer(PlayerState currentPlayer, BoardState boardState) {
        List<PlayerState> playerStateList = boardState.getPlayers();
        Integer index = 0;
        for (int i = 0; i < playerStateList.size(); i++) {
            if (playerStateList.get(i).equals(currentPlayer)) {
                index = i;
                index++;
                if (index >= playerStateList.size()) {
                    index = 0;
                }
                break;
            }
        }
        while (!playerStateList.get(index).equals(currentPlayer)) {
            if (index < playerStateList.size() && playerStateList.get(index).isPlayerActive()) {
                return playerStateList.get(index);
            } else if (index >= playerStateList.size()) {
                index = 0;
            } else {
                index++;
            }
        }
        throw new InvalidBoardRequest("Player not found");
    }

    private void updateStoneMoveInDB(StoneMove stoneMove) {
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMove.getArgs().getPlayerId());
        playerState =
                updatePlayerStateWithNewPosition(
                        playerState,
                        stoneMove.getArgs().getStoneNumber(),
                        stoneMove.getArgs().getFinalPosition());
        playerStateRepo.save(playerState);
    }

    private StoneMove getFinalPositionCutStoneMove(StoneMove stoneMove) {
        BoardState boardState = em.getReference(BoardState.class, stoneMove.getArgs().getBoardId());
        List<PlayerState> playerStates = boardState.getPlayers();
        for (PlayerState playerState : playerStates) {
            if (playerState.getStone1().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        1,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 1));
            }
            if (playerState.getStone2().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        2,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 2));
            }
            if (playerState.getStone3().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        3,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 3));
            }
            if (playerState.getStone4().equals(stoneMove.getArgs().getFinalPosition())) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        4,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 4));
            }
        }
        throw new InvalidPlayerMoveException("Invalid move.");
    }

    private PlayerState updatePlayerStateWithNewPosition(
            PlayerState playerState, Integer stoneNumber, Integer finalPosition) {
        if (stoneNumber == 1) {
            playerState.setStone1(finalPosition);
        } else if (stoneNumber == 2) {
            playerState.setStone2(finalPosition);
        } else if (stoneNumber == 3) {
            playerState.setStone3(finalPosition);
        } else {
            playerState.setStone4(finalPosition);
        }
        return playerState;
    }

    private Integer getStoneBaseValue(Integer playerNumber, Integer stoneNumber) {
        return (playerNumber * (-10)) - stoneNumber;
    }

    private Boolean isSafePosition(Integer position) {
        return (position > 100
                || position == 1
                || position == 9
                || position == 14
                || position == 22
                || position == 27
                || position == 35
                || position == 40
                || position == 48);
    }

    private Integer getFinalPositionStoneCount(StoneMove stoneMove) {
        BoardState boardState = em.getReference(BoardState.class, stoneMove.getArgs().getBoardId());
        List<PlayerState> playerStates = boardState.getPlayers();
        Integer count = 0;
        for (PlayerState playerState : playerStates) {
            if (playerState.getPlayerId().equals(stoneMove.getArgs().getPlayerId())) {
                continue;
            }
            if (playerState.getStone1().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone2().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone3().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
            if (playerState.getStone4().equals(stoneMove.getArgs().getFinalPosition())) {
                count++;
            }
        }
        return count;
    }

    private Boolean isStoneMovePossible(Integer currentPosition, Integer diceRoll) {
        if (diceRoll < 1 || diceRoll > 6) {
            return false;
        }
        if (currentPosition == 516
                || currentPosition == 126
                || currentPosition == 256
                || currentPosition == 386) {
            return false;
        }
        if (currentPosition < 0) {
            return diceRoll == 6;
        }
        if (currentPosition > 99) {
            return diceRoll <= 6 - currentPosition % 10;
        }
        return true;
    }

    private Integer getNewStonePosition(
            Integer currentPosition, Integer diceRoll, Integer playerNumber)
            throws InvalidPlayerMoveException {
        if (!isStoneMovePossible(currentPosition, diceRoll)) {
            throw new InvalidPlayerMoveException("Player move not possible");
        }
        if (currentPosition < 0 && diceRoll == 6) {
            if (playerNumber == 1) {
                return 1;
            } else if (playerNumber == 2) {
                return 14;
            } else if (playerNumber == 3) {
                return 27;
            } else {
                return 40;
            }
        }
        if (currentPosition < 99) {
            if (playerNumber == 1) {
                if (currentPosition + diceRoll > 51) {
                    return 510 + currentPosition + diceRoll - 51;
                } else {
                    return currentPosition + diceRoll;
                }
            } else if (playerNumber == 2) {
                if (currentPosition <= 12 && diceRoll + currentPosition > 12) {
                    return 120 + currentPosition + diceRoll - 12;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            } else if (playerNumber == 3) {
                if (currentPosition <= 25 && diceRoll + currentPosition > 25) {
                    return 250 + currentPosition + diceRoll - 25;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            } else {
                if (currentPosition <= 38 && diceRoll + currentPosition > 38) {
                    return 380 + currentPosition + diceRoll - 38;
                } else if (currentPosition + diceRoll > 52) {
                    return (currentPosition + diceRoll) % 52 + 1;
                } else {
                    return currentPosition + diceRoll;
                }
            }
        } else {
            return currentPosition + diceRoll;
        }
    }

    private Integer getDatabaseStonePosition(PlayerState playerState, Integer stoneNumber) {
        if (stoneNumber == 1) {
            return playerState.getStone1();
        } else if (stoneNumber == 2) {
            return playerState.getStone2();
        } else if (stoneNumber == 3) {
            return playerState.getStone3();
        }
        return playerState.getStone4();
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void blockAllBoardMoves(BoardState boardState) {
        boardState.setMovePending(false);
        boardState.setRollPending(false);
        boardStateRepo.save(boardState);
    }
}
