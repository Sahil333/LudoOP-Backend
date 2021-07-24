package com.op.ludo.dao;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PlayerStateRepo extends CrudRepository<PlayerState, String> {
    List<PlayerState> findByBoardState(BoardState boardState);
}
