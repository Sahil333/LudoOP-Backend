package com.op.ludo.dao;

import com.op.ludo.model.BoardState;
import com.op.ludo.model.PlayerState;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerStateRepo extends CrudRepository<PlayerState, String> {
    List<PlayerState> findByBoardState(BoardState boardState);
}
