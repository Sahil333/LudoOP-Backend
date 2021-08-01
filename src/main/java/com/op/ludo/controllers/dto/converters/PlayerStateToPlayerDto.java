package com.op.ludo.controllers.dto.converters;

import com.op.ludo.controllers.dto.websocket.PlayerStateDto;
import com.op.ludo.model.PlayerState;

public class PlayerStateToPlayerDto {

    public static PlayerStateDto convertToDto(PlayerState playerState) {
        return PlayerStateDto.builder()
                .playerId(playerState.getPlayerId())
                .playerNumber(playerState.getPlayerNumber())
                .turnsMissed(playerState.getTurnsMissed())
                .position(playerState.getPlayerPosition())
                .stoneTheme(playerState.getStoneTheme())
                .isActive(playerState.isPlayerActive())
                .stone1(playerState.getStone1())
                .stone2(playerState.getStone2())
                .stone3(playerState.getStone3())
                .stone4(playerState.getStone4())
                .build();
    }
}
