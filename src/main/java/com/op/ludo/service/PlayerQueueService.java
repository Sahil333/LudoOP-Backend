package com.op.ludo.service;

import com.op.ludo.dao.PlayerQueueRepo;
import com.op.ludo.exceptions.PlayerQueueException;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.LobbyPlayerQueue;
import com.op.ludo.model.PlayerQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PlayerQueueService {

  @Autowired PlayerQueueRepo playerQueueRepo;

  @Autowired LobbyPlayerQueue lobbyPlayerQueue;

  @Autowired LobbyService lobbyService;

  public void addToPlayerQueue(String playerId) {
    if (isPlayerInQueue(playerId)) {
      throw new IllegalStateException("player=" + playerId + " is already in queue");
    }
    try {
      lobbyPlayerQueue.insertInQueue(playerId);
    } catch (IOException e) {
      throw new PlayerQueueException("Failed to add player to queue", e);
    }
    PlayerQueue playerQueue = LobbyHelper.intializePlayerInQueue(playerId);
    playerQueueRepo.save(playerQueue);
    log.info("Added player={} to queue", playerId);
  }

  @Scheduled(fixedDelayString = "${player.queue.time-step}")
  public void queueHandler() throws IOException {
    log.info("Handler Running");
    while (lobbyPlayerQueue.getQueueSize() > 3) {
      createBoard();
    }
    if (!lobbyPlayerQueue.isQueueEmpty()) {
      log.info("players in queue={}", lobbyPlayerQueue.getQueueSize());
    }
  }

  @Scheduled(fixedDelayString = "${player.queue.gc}")
  public void runGC() throws IOException {
    lobbyPlayerQueue.gc();
  }

  public Boolean isPlayerInQueue(String playerId) {
    return playerQueueRepo.existsById(playerId);
  }

  private void createBoard() throws IOException {
    List<String> playerIds = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      String queuePlayerId = String.valueOf(lobbyPlayerQueue.dequeue());
      log.info("dequeued player={}", queuePlayerId);
      playerIds.add(queuePlayerId);
      if (playerQueueRepo.existsById(queuePlayerId)) {
        playerQueueRepo.deleteById(queuePlayerId);
      }
    }
    lobbyService.createBoardWithPlayers(playerIds);
  }
}
