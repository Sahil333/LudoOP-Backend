package com.op.ludo.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="playerState")
public class PlayerState {

    @Id
    private Integer boardId;

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
}
