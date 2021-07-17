package com.op.ludo.dao;

import com.op.ludo.model.PlayerState;
import org.springframework.data.repository.CrudRepository;

public interface PlayerStateRepo extends CrudRepository<PlayerState,Long> {
}
