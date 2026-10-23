package com.lukematthews.routeoptimiser.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(int status, String error, String message, List<String> details) {
}
