package com.op.ludo.service;

import com.op.ludo.dao.BoardStateRepo;
import com.op.ludo.exceptions.BoardNotFoundException;
import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import com.op.ludo.util.DateTimeUtil;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GameService {

  @PersistenceContext EntityManager em;

  @Autowired BoardStateRepo boardRepo;

  public void startGame(String playerId, Long boardId) {
    Optional<BoardState> board = boardRepo.findById(boardId);
    if (board.isEmpty()) {
      throw new BoardNotFoundException("boardId=" + boardId + " not found");
    }
    if (canStartGame(playerId, board.get())) {
      doStartGame(board.get());
      boardRepo.save(board.get());
    }
  }

  private void doStartGame(BoardState boardState) {
    boardState.setStartTime(DateTimeUtil.nowEpoch());
    boardState.setStarted(true);
  }

  private boolean canStartGame(String playerId, BoardState board) {
    return !board.isStarted() && isPlayerInGame(playerId, board);
  }

  public boolean isPlayerInGame(String playerId, BoardState board) {
    Optional<PlayerState> player =
        board.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();
    return player.isPresent();
  }
}
