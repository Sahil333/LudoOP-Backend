package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.dao.PlayerStateRepo;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TimerService {

    @Autowired BoardStateRepo boardStateRepo;

    @Autowired PlayerStateRepo playerStateRepo;

    @Autowired GamePlayService gamePlayService;

    @Autowired CommunicationService communicationService;

    public void scheduleActionCheck(Long boardId, String playerId, Long actionTime) {
        new java.util.Timer()
                .schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                actionMissedHandler(boardId, playerId, actionTime);
                            }
                        },
                        10000);
    }

    private void actionMissedHandler(Long boardId, String playerId, Long actionTime) {
        BoardState boardState = boardStateRepo.findById(boardId).get();
        Boolean isDiceRollMissed = false;
        Boolean isMoveMissed = true;
        if (actionTime == boardState.getLastActionTime()) { // block all possible actions
            boardState.setMovePending(false);
            boardState.setRollPending(false);
            boardStateRepo.save(boardState);
        }
        List<AbstractAction> abstractActionList = new ArrayList<>();
        if (isDiceRollMissed) {
            abstractActionList = gamePlayService.missedDiceRollHandler(boardId, playerId);
        } else if (isMoveMissed) {
            abstractActionList = gamePlayService.missedTurnHandler(boardId, playerId);
        }
        communicationService.sendActions(boardId, abstractActionList);
    }
}
