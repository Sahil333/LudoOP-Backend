package com.op.ludo.model;

import static java.lang.Math.max;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.exceptions.InvalidPlayerMoveException;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(name = "boardState")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardState {

    public static Boolean isSafePosition(Integer position) {
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

    public static Integer getStoneBaseValue(Integer playerNumber, Integer stoneNumber) {
        return (playerNumber * (-10)) - stoneNumber;
    }

    public static Boolean isStoneMovePossible(Integer currentPosition, Integer diceRoll) {
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

    public static Integer getNewStonePosition(
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

    @Id @NonNull private Long boardId;

    private boolean isStarted;

    private boolean isEnded;

    @NonNull private Long startTime;

    @NonNull private Long endTime;

    @NonNull private Integer lastDiceRoll;

    private boolean isMovePending;

    private boolean isRollPending;

    @NonNull private Long lastActionTime;

    @NonNull private Integer playerCount;

    @OneToMany(
            fetch = FetchType.EAGER,
            mappedBy = "boardState",
            cascade = {CascadeType.ALL})
    private List<PlayerState> players;

    @NonNull private String whoseTurn;

    @NonNull private Long turnTimeLimit;

    @NonNull private String boardTheme;

    @NonNull private Integer bid;

    @NonNull private Long createdTime;

    public BoardState() {}

    public BoardState(
            @NonNull Long boardId,
            @NonNull Boolean isStarted,
            @NonNull Boolean isEnded,
            @NonNull Long startTime,
            @NonNull Long endTime,
            @NonNull Integer lastDiceRoll,
            @NonNull Boolean isMovePending,
            @NonNull Boolean isRollPending,
            @NonNull Long lastActionTime,
            @NonNull Integer playerCount,
            @NonNull String whoseTurn,
            @NonNull Long turnTimeLimit,
            @NonNull String boardTheme,
            @NonNull Integer bid,
            @NonNull Long createdTime) {
        this.boardId = boardId;
        this.isStarted = isStarted;
        this.isEnded = isEnded;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastDiceRoll = lastDiceRoll;
        this.isMovePending = isMovePending;
        this.isRollPending = isRollPending;
        this.lastActionTime = lastActionTime;
        this.playerCount = playerCount;
        this.whoseTurn = whoseTurn;
        this.turnTimeLimit = turnTimeLimit;
        this.boardTheme = boardTheme;
        this.bid = bid;
        this.createdTime = createdTime;
    }

    @JsonIgnore
    public PlayerState getPlayerState(String playerId) {
        return getPlayers().stream()
                .filter(playerState -> playerState.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No player found with player id=" + playerId));
    }

    @JsonIgnore
    public boolean isPlayerActive(String playerId) {
        return getPlayerState(playerId).isPlayerActive();
    }

    public void updatePlayerStateWithNewPosition(
            String playerId, Integer stoneNumber, Integer finalPosition) {
        PlayerState playerState = getPlayerState(playerId);
        if (stoneNumber == 1) {
            playerState.setStone1(finalPosition);
        } else if (stoneNumber == 2) {
            playerState.setStone2(finalPosition);
        } else if (stoneNumber == 3) {
            playerState.setStone3(finalPosition);
        } else {
            playerState.setStone4(finalPosition);
        }
        if (finalPosition == 516
                || finalPosition == 126
                || finalPosition == 256
                || finalPosition == 386) {
            playerState.setHomeCount(1 + playerState.getHomeCount());
        }
    }

    @JsonIgnore
    public Integer getNextPlayerPosition() {
        int maxPos = 0;
        for (PlayerState player : players) {
            maxPos = max(maxPos, player.getPlayerPosition());
        }
        return maxPos + 1;
    }

    @JsonIgnore
    public Boolean hasGameFinished() { // pass final board state after stone move has been done.
        return getFinishedPlayerCount() == players.size() - 1;
    }

    @JsonIgnore
    private Integer getFinishedPlayerCount() {
        int count = 0;
        for (PlayerState playerState : players) {
            if (playerState.getHomeCount() == 4 || !playerState.isPlayerActive()) {
                count++;
            }
        }
        return count;
    }

    @JsonIgnore
    public PlayerState getNextPlayer(PlayerState currentPlayer) {
        Integer index = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).equals(currentPlayer)) {
                index = i;
                index++;
                if (index >= players.size()) {
                    index = 0;
                }
                break;
            }
        }
        while (!players.get(index).equals(currentPlayer)) {
            if (players.get(index).isPlayerActive()) {
                return players.get(index);
            } else if (index >= players.size()) {
                index = 0;
            } else {
                index++;
            }
        }
        throw new InvalidBoardRequest("Player not found");
    }
}
