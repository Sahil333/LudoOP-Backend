package com.op.ludo.controllers.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StoneMoveDto {

    private Long boardId;
    private Integer stoneNumber;
    private Integer initialPosition;
    private Integer finalPosition;
}
