package com.op.ludo.controllers.advice;

import com.op.ludo.exceptions.InvalidBoardRequest;
import com.op.ludo.exceptions.PlayerQueueException;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class ApiErrorHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, InvalidBoardRequest.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(Exception ex, HttpServletRequest request) {
        log.error("Error in api call {}",request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(buildApiError(ex, request, status), status);
    }

    @ExceptionHandler({PlayerQueueException.class})
    public ResponseEntity<ApiErrorResponse> handleInternalServerException(Exception ex, HttpServletRequest request) {
        log.error("Error in api call {}",request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(buildApiError(ex, request, status), status);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error in api call {}",request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(buildApiError(ex, request, status), status);
    }

    private ApiErrorResponse buildApiError(Exception ex, HttpServletRequest request, HttpStatus status) {
        ApiErrorResponse.ApiErrorResponseBuilder errorResponseBuilder = ApiErrorResponse.builder();
        errorResponseBuilder
            .dateTime(LocalDateTime.now())
            .message(ex.getMessage())
            .statusCode(status.value())
            .status(status.getReasonPhrase())
            .path(request.getRequestURI());
        if(ex.getCause() != null) {
            errorResponseBuilder.message(ex.getCause().getMessage());
        }
        return errorResponseBuilder.build();
    }

    @Value
    @Builder
    public static class ApiErrorResponse {
        String message;
        String details;
        String path;
        int statusCode;
        String status;
        LocalDateTime dateTime;
    }
}
