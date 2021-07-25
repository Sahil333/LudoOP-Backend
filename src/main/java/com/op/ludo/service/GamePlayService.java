package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.impl.*;
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
        List<AbstractAction> abstractActionList = new ArrayList<>();
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
        abstractActionList.add(diceRoll);
        StoneMovePending stoneMovePending =
                new StoneMovePending(
                        diceRollReq.getArgs().getBoardId(),
                        diceRollReq.getArgs().getPlayerId(),
                        playerState.getPlayerNumber());
        abstractActionList.add(stoneMovePending);
        return abstractActionList;
    }

    public List<AbstractAction> updateBoardWithStoneMove(StoneMoveReq stoneMoveReq) {
        List<AbstractAction> actionList = new ArrayList<AbstractAction>();
        if (!(isSafePosition(stoneMoveReq.getArgs().getFinalPosition())
                || getFinalPositionStoneCount(stoneMoveReq) != 1)) {
            StoneMove cutStoneMove = getFinalPositionCutStoneMove(stoneMoveReq);
            actionList.add(cutStoneMove);
            updateStoneMoveInDB(cutStoneMove);
        }
        updateStoneMoveInDB(stoneMoveReq);
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMoveReq.getArgs().getPlayerId());
        BoardState boardState =
                em.getReference(BoardState.class, stoneMoveReq.getArgs().getBoardId());
        boardState.setRollPending(true);
        boardState.setMovePending(false);
        boardStateRepo.save(boardState);
        DiceRollPending diceRollPending;
        if (boardState.getLastDiceRoll() == 6) {
            diceRollPending =
                    new DiceRollPending(
                            stoneMoveReq.getArgs().getBoardId(),
                            stoneMoveReq.getArgs().getPlayerId(),
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

    public Boolean isStoneMoveValid(StoneMoveReq stoneMoveReq) throws InvalidPlayerMoveException {
        if (!lobbyService.isPlayerAlreadyPartOfGame(stoneMoveReq.getArgs().getPlayerId())) {
            throw new InvalidPlayerMoveException("Player is not part of the game.");
        } else if (stoneMoveReq.getArgs().getStoneNumber() < 1
                || stoneMoveReq.getArgs().getStoneNumber() > 4) {
            throw new InvalidPlayerMoveException("Invalid stone number.");
        }
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMoveReq.getArgs().getPlayerId());
        BoardState boardState = playerState.getBoardState();
        Integer currentDBPosition =
                getDatabaseStonePosition(playerState, stoneMoveReq.getArgs().getStoneNumber());
        if (playerState.getPlayerNumber() != boardState.getWhoseTurn()
                || !boardState.isMovePending()
                || boardState.getBoardId() != stoneMoveReq.getArgs().getBoardId()) {
            throw new InvalidPlayerMoveException("Turn not valid.");
        } else if (currentDBPosition != stoneMoveReq.getArgs().getInitialPosition()) {
            throw new InvalidPlayerMoveException("Invalid initial position");
        }
        return getNewStonePosition(
                        currentDBPosition,
                        boardState.getLastDiceRoll(),
                        playerState.getPlayerNumber())
                == stoneMoveReq.getArgs().getFinalPosition();
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

    private void updateStoneMoveInDB(StoneMoveReq stoneMoveReq) {
        PlayerState playerState =
                em.getReference(PlayerState.class, stoneMoveReq.getArgs().getPlayerId());
        playerState =
                updatePlayerStateWithNewPosition(
                        playerState,
                        stoneMoveReq.getArgs().getStoneNumber(),
                        stoneMoveReq.getArgs().getFinalPosition());
        playerStateRepo.save(playerState);
    }

    private StoneMove getFinalPositionCutStoneMove(StoneMoveReq stoneMoveReq) {
        BoardState boardState =
                em.getReference(BoardState.class, stoneMoveReq.getArgs().getBoardId());
        List<PlayerState> playerStates = boardState.getPlayers();
        for (PlayerState playerState : playerStates) {
            if (playerState.getStone1() == stoneMoveReq.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMoveReq.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        1,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 1));
            }
            if (playerState.getStone2() == stoneMoveReq.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMoveReq.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        2,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 2));
            }
            if (playerState.getStone3() == stoneMoveReq.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMoveReq.getArgs().getBoardId(),
                        playerState.getPlayerId(),
                        3,
                        playerState.getStone1(),
                        getStoneBaseValue(playerState.getPlayerNumber(), 3));
            }
            if (playerState.getStone4() == stoneMoveReq.getArgs().getFinalPosition()) {
                return new StoneMove(
                        stoneMoveReq.getArgs().getBoardId(),
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

    private Integer getFinalPositionStoneCount(StoneMoveReq stoneMoveReq) {
        BoardState boardState =
                em.getReference(BoardState.class, stoneMoveReq.getArgs().getBoardId());
        List<PlayerState> playerStates = boardState.getPlayers();
        Integer count = 0;
        for (PlayerState playerState : playerStates) {
            if (playerState.getPlayerId() == stoneMoveReq.getArgs().getPlayerId()) {
                continue;
            }
            if (playerState.getStone1() == stoneMoveReq.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone2() == stoneMoveReq.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone3() == stoneMoveReq.getArgs().getFinalPosition()) {
                count++;
            }
            if (playerState.getStone4() == stoneMoveReq.getArgs().getFinalPosition()) {
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
