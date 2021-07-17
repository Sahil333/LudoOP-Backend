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
}
