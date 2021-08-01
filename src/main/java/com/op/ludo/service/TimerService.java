package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.model.BoardState;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class TimerService {

    @Value("${gameconfig.enable-timer}")
    private Boolean enableTimer;

    @Autowired private BoardStateRepo boardStateRepo;

    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired private GamePlayService gamePlayService;

    @Autowired private CommunicationService communicationService;

    private class TimerAction implements Runnable {
        Long boardId;
        String playerId;
        Long actionTime;

        public TimerAction(Long boardId, String playerId, Long actionTime) {
            this.boardId = boardId;
            this.playerId = playerId;
            this.actionTime = actionTime;
        }

        @Override
        public void run() {
            if (enableTimer) {
                log.info("timer caught up");
                actionMissedHandler(this.boardId, this.playerId, this.actionTime);
            }
        }
    }

    public void setEnableTimer(boolean enableTimer) {
        this.enableTimer = enableTimer;
    }

    public void clearTimerTasks() {
        ((ScheduledThreadPoolExecutor) threadPoolTaskScheduler.getScheduledExecutor())
                .getQueue()
                .clear();
        log.debug("cleared timer tasks");
    }

    public void scheduleActionCheck(Long boardId, String playerId, Long actionTime) {
        if (!enableTimer) return;
        BoardState boardState = boardStateRepo.findById(boardId).get();
        TimerAction timerAction = new TimerAction(boardId, playerId, actionTime);
        threadPoolTaskScheduler.schedule(
                timerAction,
                new Date(
                        threadPoolTaskScheduler.getClock().millis()
                                + boardState.getTurnTimeLimit()));
    }

    private void actionMissedHandler(Long boardId, String playerId, Long actionTime) {
        BoardState boardState = boardStateRepo.findById(boardId).get();
        Boolean isDiceRollMissed = boardState.isRollPending();
        Boolean isMoveMissed = boardState.isMovePending();
        if (actionTime == boardState.getLastActionTime()) { // block all possible actions
            gamePlayService.blockAllBoardMoves(boardState);
        }
        List<AbstractAction> abstractActionList = new ArrayList<>();
        if (isDiceRollMissed) {
            abstractActionList = gamePlayService.missedDiceRollHandler(boardId, playerId);
        } else if (isMoveMissed) {
            abstractActionList = gamePlayService.missedTurnHandler(boardId, playerId);
        }
        if (!abstractActionList.isEmpty())
            communicationService.sendActions(boardId, abstractActionList);
    }
}
