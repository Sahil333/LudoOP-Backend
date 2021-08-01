package com.op.ludo.controllers.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PlayerStateDto {

    private String playerId;
    private Integer playerNumber;
    private Integer turnsMissed;
    private Integer position;
    private String stoneTheme;

    @JsonProperty("active")
    private boolean isActive;

    private Integer stone1;
    private Integer stone2;
    private Integer stone3;
    private Integer stone4;
}
