package com.lukematthews.routeoptimiser.api.exception;

import com.lukematthews.routeoptimiser.api.dto.ErrorResponse;
import com.lukematthews.routeoptimiser.domain.NoRouteFoundException;
import com.lukematthews.routeoptimiser.domain.RoutingUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /** 422: the request is well-formed, but these points cannot be connected by road. */
    private static final HttpStatus NO_ROUTE_STATUS = HttpStatus.valueOf(422);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.add(error.getField() + ": " + Objects.toString(error.getDefaultMessage(), "is invalid")));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                details.add(Objects.toString(error.getDefaultMessage(), "is invalid")));
        Collections.sort(details);
        return respond(HttpStatus.BAD_REQUEST, "Request validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return respond(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON of the expected shape");
    }

    /** Business-rule violations raised by the service, such as "too many stops". */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadInput(IllegalArgumentException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoRouteFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoRouteFoundException ex) {
        return respond(NO_ROUTE_STATUS, ex.getMessage());
    }

    @ExceptionHandler(RoutingUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRoutingUnavailable(RoutingUnavailableException ex) {
        log.error("Routing backend problem", ex);   // details stay in the log
        return respond(HttpStatus.BAD_GATEWAY, "The routing service is currently unavailable");
    }

    /** Thrown by the service's checks, so bug on this end. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleInternalError(IllegalStateException ex) {
        log.error("Internal error", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return respond(status, message, List.of());
    }

    private static ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message, details));
    }
}
