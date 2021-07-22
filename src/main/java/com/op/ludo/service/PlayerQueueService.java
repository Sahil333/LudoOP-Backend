package com.op.ludo.service;

import com.op.ludo.dao.PlayerQueueRepo;
import com.op.ludo.exceptions.PlayerQueueException;
import com.op.ludo.helper.LobbyHelper;
import com.op.ludo.model.LobbyPlayerQueue;
import com.op.ludo.model.PlayerQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
public class PlayerQueueService {

  @Autowired PlayerQueueRepo playerQueueRepo;

  @Autowired LobbyPlayerQueue lobbyPlayerQueue;

  @Autowired LobbyService lobbyService;

  public void addToPlayerQueue(String playerId) {
    if (isPlayerInQueue(playerId)) {
      throw new IllegalStateException("player=" + playerId + " is already in queue");
    }
    PlayerQueue playerQueue = LobbyHelper.intializePlayerInQueue(playerId);
    playerQueueRepo.save(playerQueue);
    try {
      lobbyPlayerQueue.insertInQueue(playerId);
    } catch (IOException e) {
      throw new PlayerQueueException("Failed to add player to queue", e);
    }
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

  // TODO: if players are removed from the queue and error happens either deleting in queue repo or
  // creating board
  //  data will become inconsistent that the players are still in queue table but not in the queue
  // model
  //  need a transactional solution for this problem accounting for any failure at any point.
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
