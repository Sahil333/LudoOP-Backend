package com.op.ludo.dao;

import com.op.ludo.model.BoardState;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BoardStateRepo extends CrudRepository<BoardState,Long> {
}
