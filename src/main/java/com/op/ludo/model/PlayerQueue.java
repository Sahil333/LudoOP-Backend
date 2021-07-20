package com.op.ludo.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "playerQueue")
public class PlayerQueue {
    @Id
    private Long playerId;

    private Long requestTime;

    public PlayerQueue(){}

    public PlayerQueue(Long playerId, Long requestTime) {
        this.playerId = playerId;
        this.requestTime = requestTime;
    }
}
