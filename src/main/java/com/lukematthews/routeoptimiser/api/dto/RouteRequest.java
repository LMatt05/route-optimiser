package com.lukematthews.routeoptimiser.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RouteRequest(
    @NotNull @Valid StopDto start,
    @NotEmpty List<@NotNull @Valid StopDto> destinations){
    }
