package com.op.ludo.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BoardRequest {
    @NonNull private Integer bid;
    @NonNull private Type type;
    @NonNull private String playerId;

    public enum Type {
        ONLINE,
        FRIEND
    }
}
