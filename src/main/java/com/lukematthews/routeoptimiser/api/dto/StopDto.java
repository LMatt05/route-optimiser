package com.lukematthews.routeoptimiser.api.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.lukematthews.routeoptimiser.domain.Coordinate;
import com.lukematthews.routeoptimiser.domain.Stop;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StopDto(
    @Size(max = 100) String label,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude){
        public Stop toDomain(){
            return new Stop(label, new Coordinate(latitude, longitude));
    }

    public static StopDto from(Stop stop){
            return new StopDto(stop.label(), stop.coordinate().latitude(), stop.coordinate().longitude());
    }
}
