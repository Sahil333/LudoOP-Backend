package com.op.ludo.controllers.dto.websocket;

import com.op.ludo.game.action.AbstractAction;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ActionsWithBoardState {

    private List<AbstractAction> actions;
    private BoardDto board;
}
