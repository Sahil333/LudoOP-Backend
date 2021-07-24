package com.op.ludo.model;

import java.util.List;
import javax.persistence.*;
import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(name = "boardState")
public class BoardState {

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
      mappedBy = "boardState",
      cascade = {CascadeType.ALL})
  private List<PlayerState> players;

  @NonNull private Integer whoseTurn;

  @NonNull private Integer turnTimeLimit;

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
      @NonNull Integer whoseTurn,
      @NonNull Integer turnTimeLimit,
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
