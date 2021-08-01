package com.op.ludo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Entity
@Table(name = "boardState")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardState {

    @Id @NonNull private Long boardId;

    private boolean isStarted;

    private boolean isEnded;

    @EqualsAndHashCode.Exclude @NonNull private Long startTime;

    @EqualsAndHashCode.Exclude @NonNull private Long endTime;

    @NonNull private Integer lastDiceRoll;

    private boolean isMovePending;

    private boolean isRollPending;

    @EqualsAndHashCode.Exclude @NonNull private Long lastActionTime;

    @NonNull private Integer playerCount;

    @OneToMany(
            fetch = FetchType.EAGER,
            mappedBy = "boardState",
            cascade = {CascadeType.ALL})
    private List<PlayerState> players;

    @NonNull private Integer whoseTurn;

    @NonNull private Long turnTimeLimit;

    @NonNull private String boardTheme;

    @NonNull private Integer bid;

    @EqualsAndHashCode.Exclude @NonNull private Long createdTime;

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
            @NonNull Integer whoseTurn,
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
}
