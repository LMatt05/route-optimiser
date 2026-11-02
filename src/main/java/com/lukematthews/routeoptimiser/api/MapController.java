package com.lukematthews.routeoptimiser.api;

import com.lukematthews.routeoptimiser.api.dto.MapBoundsResponse;
import com.lukematthews.routeoptimiser.config.MapBoundsProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/map")
public class MapController {
    private final MapBoundsProperties bounds;

    public MapController(MapBoundsProperties bounds){
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
    }

    /** The area covered by the loaded OSRM map, so clients can limit what they show. */
    @GetMapping("/bounds")
    public MapBoundsResponse bounds(){
        return MapBoundsResponse.from(bounds);
    }
}
