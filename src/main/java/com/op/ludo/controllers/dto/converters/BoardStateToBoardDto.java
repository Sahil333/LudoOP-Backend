package com.op.ludo.controllers.dto.converters;

import com.op.ludo.controllers.dto.websocket.BoardDto;
import com.op.ludo.controllers.dto.websocket.PlayerStateDto;
import com.op.ludo.model.BoardState;
import java.util.ArrayList;
import java.util.List;

public class BoardStateToBoardDto {

    public static BoardDto convertToDto(BoardState boardState) {
        List<PlayerStateDto> players = new ArrayList<>();
        boardState
                .getPlayers()
                .forEach(player -> players.add(PlayerStateToPlayerDto.convertToDto(player)));

        return BoardDto.builder()
                .whoseTurn(boardState.getWhoseTurn())
                .isStarted(boardState.isStarted())
                .isEnded(boardState.isEnded())
                .lastDiceRoll(boardState.getLastDiceRoll())
                .isMovePending(boardState.isMovePending())
                .isRollPending(boardState.isRollPending())
                .boardTheme(boardState.getBoardTheme())
                .lastActionTime(boardState.getLastActionTime())
                .turnTimeLimit(boardState.getTurnTimeLimit())
                .players(players)
                .build();
    }
}
