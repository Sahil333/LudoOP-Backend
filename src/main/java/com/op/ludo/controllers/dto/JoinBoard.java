package com.op.ludo.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JoinBoard {
    @NonNull Long boardId;
    @NonNull String playerId;
}
