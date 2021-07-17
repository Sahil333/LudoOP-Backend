package com.op.ludo.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="playerState")
public class PlayerState {
    @Id
    @NonNull
    private Long playerId;

    @NonNull
    private Long boardId;

    @NonNull
    private Integer stone1;

    @NonNull
    private Integer stone2;

    @NonNull
    private Integer stone3;

    @NonNull
    private Integer stone4;

    @NonNull
    private Integer turnsMissed;

    @NonNull
    private Integer playerPosition;

    @NonNull
    private Boolean isPlayerActive;

    @NonNull
    private Integer homeCount;

    @NonNull
    private Integer playerNumber;

    @NonNull
    private String playerType;

    @NonNull
    private String stoneTheme;

    public PlayerState(){
    }

    public PlayerState(@NonNull Long playerId, @NonNull Long boardId, @NonNull Integer stone1, @NonNull Integer stone2, @NonNull Integer stone3, @NonNull Integer stone4, @NonNull Integer turnsMissed, @NonNull Integer playerPosition, @NonNull Boolean isPlayerActive, @NonNull Integer homeCount, @NonNull Integer playerNumber, @NonNull String playerType, @NonNull String stoneTheme) {
        this.playerId = playerId;
        this.boardId = boardId;
        this.stone1 = stone1;
        this.stone2 = stone2;
        this.stone3 = stone3;
        this.stone4 = stone4;
        this.turnsMissed = turnsMissed;
        this.playerPosition = playerPosition;
        this.isPlayerActive = isPlayerActive;
        this.homeCount = homeCount;
        this.playerNumber = playerNumber;
        this.playerType = playerType;
        this.stoneTheme = stoneTheme;
    }
}