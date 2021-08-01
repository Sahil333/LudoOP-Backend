package com.op.ludo.controllers.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class BoardDto {

    private String whoseTurn;

    @JsonProperty("started")
    private boolean isStarted;

    @JsonProperty("ended")
    private boolean isEnded;

    private Integer lastDiceRoll;

    @JsonProperty("movePending")
    private boolean isMovePending;

    @JsonProperty("rollPending")
    private boolean isRollPending;

    private String boardTheme;
    @EqualsAndHashCode.Exclude private Long lastActionTime;
    private Long turnTimeLimit;
    private List<PlayerStateDto> players;

    public PlayerStateDto getPlayer(String playerId) {
        return getPlayers().stream()
                .filter(playerState -> playerState.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No player found with player id=" + playerId));
    }
}
