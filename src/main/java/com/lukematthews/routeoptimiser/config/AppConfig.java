package com.lukematthews.routeoptimiser.config;

import com.lukematthews.routeoptimiser.routing.OsrmRoutingClient;
import com.lukematthews.routeoptimiser.routing.RoutingClient;
import com.lukematthews.routeoptimiser.solver.RouteSolver;
import com.lukematthews.routeoptimiser.solver.TwoOptSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class AppConfig {

    @Bean
    RestClient osrmRestClient(OsrmProperties properties){
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
    @Bean
    RoutingClient routingClient(RestClient osrmRestClient){
        return new OsrmRoutingClient(osrmRestClient);
    }

    @Bean
    RouteSolver routeSolver(){
        return new TwoOptSolver();
    }
}
