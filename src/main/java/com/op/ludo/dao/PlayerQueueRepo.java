package com.op.ludo.dao;

import com.op.ludo.model.PlayerQueue;
import org.springframework.data.repository.CrudRepository;

public interface PlayerQueueRepo extends CrudRepository<PlayerQueue, String> {
}
