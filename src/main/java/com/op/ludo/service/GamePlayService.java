package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.BoardNotFoundException;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.*;
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

    @Autowired LobbyService lobbyService;

    @PersistenceContext EntityManager em;

    @Autowired PlayerStateRepo playerStateRepo;

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired TimerService timerService;

    @Autowired StoneMoveChain stoneMoveChain;

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

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void blockAllBoardMoves(BoardState boardState) {
        boardState.setMovePending(false);
        boardState.setRollPending(false);
        boardStateRepo.save(boardState);
    }
}
