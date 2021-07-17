package com.op.ludo.dao;

import com.op.ludo.model.BoardState;
import org.springframework.data.repository.CrudRepository;

public interface BoardStateRepo extends CrudRepository<BoardState,Long> {
}
