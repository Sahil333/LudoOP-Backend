package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.*;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.ArrayList;
import java.util.List;
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

    public List<AbstractAction> rollDiceForPlayer(DiceRollReq diceRollReq) {
        List<AbstractAction> actionList = new ArrayList<>();
        PlayerState playerState = em.find(PlayerState.class, diceRollReq.getArgs().getPlayerId());
        BoardState boardState = playerState.getBoardState();
        if (!boardState.isRollPending()
                || boardState.getWhoseTurn() != playerState.getPlayerNumber()) {
            throw new InvalidPlayerMoveException("Invalid dice roll request.");
        }
        DiceRoll diceRoll =
                new DiceRoll(
                        diceRollReq.getArgs().getPlayerId(), diceRollReq.getArgs().getBoardId());
        boardState.setLastDiceRoll(diceRoll.getArgs().getDiceRoll());
        boardState.setRollPending(false);
        boardState.setMovePending(true);
        boardStateRepo.save(boardState);
        actionList.add(diceRoll);
        StoneMovePending stoneMovePending =
                new StoneMovePending(
                        diceRollReq.getArgs().getBoardId(),
                        diceRollReq.getArgs().getPlayerId(),
                        playerState.getPlayerNumber());
        actionList.add(stoneMovePending);
        return actionList;
    }

    public List<AbstractAction> updateBoardWithStoneMove(StoneMove stoneMove) {
        List<AbstractAction> actionList = new ArrayList<AbstractAction>();
        if (!(isSafePosition(stoneMove.getArgs().getFinalPosition())
                || getFinalPositionStoneCount(stoneMove) != 1)) {
            StoneMove cutStoneMove = getFinalPositionCutStoneMove(stoneMove);
            actionList.add(cutStoneMove);
            updateStoneMoveInDB(cutStoneMove);
        }
        updateStoneMoveInDB(stoneMove);
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMove.getArgs().getPlayerId());
        BoardState boardState = em.getReference(BoardState.class, stoneMove.getArgs().getBoardId());
        boardState.setRollPending(true);
        boardState.setMovePending(false);
        boardStateRepo.save(boardState);
        DiceRollPending diceRollPending;
        if (boardState.getLastDiceRoll() == 6) {
            diceRollPending =
                    new DiceRollPending(
                            stoneMove.getArgs().getBoardId(),
                            stoneMove.getArgs().getPlayerId(),
                            playerState.getPlayerNumber());
        } else {
            PlayerState nextPlayer = getNextPlayer(playerState, boardState);
            diceRollPending =
                    new DiceRollPending(
                            boardState.getBoardId(),
                            nextPlayer.getPlayerId(),
                            nextPlayer.getPlayerNumber());
        }
        actionList.add(diceRollPending);
        return actionList;
    }

    public List<AbstractAction> missedDiceRollHandler(Long boardId, String playerId) {
        List<AbstractAction> actionList = new ArrayList<>();
        return actionList;
    }

    public List<AbstractAction> missedTurnHandler(Long boardId, String playerId) {
        List<AbstractAction> actionList = new ArrayList<>();
        PlayerState playerState = em.getReference(PlayerState.class, playerId);
        BoardState boardState = em.getReference(BoardState.class, boardId);
        List<Integer> movableStones = moveableStoneList(playerState, boardState);
        if (movableStones.size() == 0) {
            PlayerState nextPlayer = getNextPlayer(playerState, boardState);
            DiceRollPending diceRollPending =
                    new DiceRollPending(
                            boardState.getBoardId(),
                            nextPlayer.getPlayerId(),
                            nextPlayer.getPlayerNumber());
            actionList.add(diceRollPending);
            return actionList;
        }
        Integer moveRandomStoneNumber =
                LobbyHelper.getRandomNumberInRange(0, movableStones.size() - 1);
        StoneMove randomStoneMove =
                getNewStoneMove(playerState, boardState, movableStones.get(moveRandomStoneNumber));
        actionList.add(randomStoneMove);
        actionList.addAll(updateBoardWithStoneMove(randomStoneMove));
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

    public Boolean isStoneMoveValid(
            Long boardId,
            String playerId,
            Integer stoneNumber,
            Integer initialPosition,
            Integer finalPosition)
            throws InvalidPlayerMoveException {
        if (!lobbyService.isPlayerAlreadyPartOfGame(playerId)) {
            throw new InvalidPlayerMoveException("Player is not part of the game.");
        } else if (stoneNumber < 1 || stoneNumber > 4) {
            throw new InvalidPlayerMoveException("Invalid stone number.");
        }
        PlayerState playerState = em.getReference(PlayerState.class, playerId);
        BoardState boardState = playerState.getBoardState();
        Integer currentDBPosition = getDatabaseStonePosition(playerState, stoneNumber);
        if (playerState.getPlayerNumber() != boardState.getWhoseTurn()
                || !boardState.isMovePending()
                || boardState.getBoardId() != boardId) {
            throw new InvalidPlayerMoveException("Turn not valid.");
        } else if (currentDBPosition != initialPosition) {
            throw new InvalidPlayerMoveException("Invalid initial position");
        }
        return getNewStonePosition(
                        currentDBPosition,
                        boardState.getLastDiceRoll(),
                        playerState.getPlayerNumber())
                == finalPosition;
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
            }
        }
        if (index < playerStateList.size() - 1) {
            return playerStateList.get(index + 1);
        } else {
            return playerStateList.get(0);
        }
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
            if (playerState.getStone1() == stoneMove.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        1,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 1));
            }
            if (playerState.getStone2() == stoneMove.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        2,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 2));
            }
            if (playerState.getStone3() == stoneMove.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMove.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        3,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 3));
            }
            if (playerState.getStone4() == stoneMove.getArgs().getFinalPosition()) {
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
            if (playerState.getPlayerId() == stoneMove.getArgs().getPlayerId()) {
                continue;
            }
            if (playerState.getStone1() == stoneMove.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone2() == stoneMove.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone3() == stoneMove.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone4() == stoneMove.getArgs().getFinalPosition()) {
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
                || currentPosition == 266
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
                if (currentPosition <= 26 && diceRoll + currentPosition > 26) {
                    return 260 + currentPosition + diceRoll - 26;
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
}
