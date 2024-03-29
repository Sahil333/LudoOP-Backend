package com.op.ludo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(name = "playerState")
public class PlayerState {
    @Id @NonNull private String playerId;

    @ManyToOne
    @NonNull
    // ignoring in serialization because of circular reference
    @JsonIgnore
    private BoardState boardState;

    @NonNull private Integer stone1;

    @NonNull private Integer stone2;

    @NonNull private Integer stone3;

    @NonNull private Integer stone4;

    @NonNull private Integer turnsMissed;

    @NonNull private Integer playerPosition;

    private boolean isPlayerActive;

    @NonNull private Integer homeCount;

    @NonNull private Integer playerNumber;

    @NonNull private String playerType;

    @NonNull private String stoneTheme;

    public PlayerState() {}

    public PlayerState(
            @NonNull String playerId,
            @NonNull BoardState boardState,
            @NonNull Integer stone1,
            @NonNull Integer stone2,
            @NonNull Integer stone3,
            @NonNull Integer stone4,
            @NonNull Integer turnsMissed,
            @NonNull Integer playerPosition,
            @NonNull Boolean isPlayerActive,
            @NonNull Integer homeCount,
            @NonNull Integer playerNumber,
            @NonNull String playerType,
            @NonNull String stoneTheme) {
        this.playerId = playerId;
        this.boardState = boardState;
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

    @JsonIgnore
    public Integer getDatabaseStonePosition(Integer stoneNumber) {
        if (stoneNumber == 1) {
            return getStone1();
        } else if (stoneNumber == 2) {
            return getStone2();
        } else if (stoneNumber == 3) {
            return getStone3();
        }
        return getStone4();
    }
}
