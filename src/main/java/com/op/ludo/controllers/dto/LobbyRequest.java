package com.op.ludo.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LobbyRequest {
    @NonNull private Integer bid;
    @NonNull private Type type;
    @Setter private String playerId;

    public enum Type {
        ONLINE,
        FRIEND
    }
}
