package com.op.ludo.model;


import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "boardState")
public class BoardState {

    @Id
    @NonNull
    private Long id;

    @NonNull
    private Boolean isStarted;

    @NonNull
    private Boolean isEnded;

    @NonNull
    private Long startTime;

    @NonNull
    private Long endTime;

    @NonNull
    private Integer lastDiceRoll;

    @NonNull
    private Boolean isMovePending;

    @NonNull
    private Boolean isRollPending;

    @NonNull
    private Long lastActionTime;

    @NonNull
    private Integer playerCount;

    @NonNull
    private String playerIds;

    @NonNull
    private Integer whoseTurn;

    @NonNull
    private Integer turnTimeLimit;

    @NonNull
    private String boardTheme;

    @NonNull
    private Integer bid;

    @NonNull
    private Long createdTime;

    public BoardState(){
    }

    public BoardState(@NonNull Long id, @NonNull Boolean isStarted, @NonNull Boolean isEnded, @NonNull Long startTime, @NonNull Long endTime, @NonNull Integer lastDiceRoll, @NonNull Boolean isMovePending, @NonNull Boolean isRollPending, @NonNull Long lastActionTime, @NonNull Integer playerCount, @NonNull String playerIds, @NonNull Integer whoseTurn, @NonNull Integer turnTimeLimit, @NonNull String boardTheme, @NonNull Integer bid, @NonNull Long createdTime) {
        this.id = id;
        this.isStarted = isStarted;
        this.isEnded = isEnded;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastDiceRoll = lastDiceRoll;
        this.isMovePending = isMovePending;
        this.isRollPending = isRollPending;
        this.lastActionTime = lastActionTime;
        this.playerCount = playerCount;
        this.playerIds = playerIds;
        this.whoseTurn = whoseTurn;
        this.turnTimeLimit = turnTimeLimit;
        this.boardTheme = boardTheme;
        this.bid = bid;
        this.createdTime = createdTime;
    }
}
