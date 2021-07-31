package com.op.ludo.controllers.dto.websocket;

import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ActionsWithBoardState {

    private List<AbstractAction> actions;
    // use board state directly for now
    private BoardState board;
}
