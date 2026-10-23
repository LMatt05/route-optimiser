package com.lukematthews.routeoptimiser.api;

import com.lukematthews.routeoptimiser.api.dto.RouteRequest;
import com.lukematthews.routeoptimiser.api.dto.RouteResponse;
import com.lukematthews.routeoptimiser.api.dto.StopDto;
import com.lukematthews.routeoptimiser.domain.Route;
import com.lukematthews.routeoptimiser.domain.Stop;
import com.lukematthews.routeoptimiser.service.RouteOptimisationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
    private final RouteOptimisationService service;

    public RouteController(RouteOptimisationService service){
        this.service = service;
    }

    @PostMapping("/optimise")
    public RouteResponse optimise(@Valid @RequestBody RouteRequest request){
        Stop start = request.start().toDomain();
        List<Stop> destinations = request.destinations().stream().map(StopDto::toDomain).toList();

        Route route = service.optimise(start, destinations);
        return RouteResponse.from(route);
    }
}
