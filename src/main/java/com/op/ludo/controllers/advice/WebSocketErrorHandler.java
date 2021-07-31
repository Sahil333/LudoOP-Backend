package com.op.ludo.controllers.advice;

import com.op.ludo.util.DateTimeUtil;
import java.util.Map;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebSocketErrorHandler {

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public GameErrorResponse handleGameException(
            Exception ex, @Headers Map<String, Object> headers) {
        return buildGameError(
                ex, (String) headers.get(SimpMessageHeaderAccessor.DESTINATION_HEADER));
    }

    private GameErrorResponse buildGameError(Exception ex, String destination) {
        GameErrorResponse.GameErrorResponseBuilder errorResponseBuilder =
                GameErrorResponse.builder();
        errorResponseBuilder
                .dateTime(DateTimeUtil.now())
                .message(ex.getMessage())
                .destination(destination);
        if (ex.getCause() != null) {
            errorResponseBuilder.message(ex.getCause().getMessage());
        }
        return errorResponseBuilder.build();
    }
}
