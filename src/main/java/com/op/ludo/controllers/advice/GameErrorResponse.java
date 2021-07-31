package com.op.ludo.controllers.advice;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class GameErrorResponse {

    private String message;
    private String details;
    private String destination;
    @EqualsAndHashCode.Exclude private ZonedDateTime dateTime;
}
