package com.lukematthews.routeoptimiser.api;

import com.lukematthews.routeoptimiser.config.MapBoundsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MapControllerTest {
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new MapController(new MapBoundsProperties(51.28, -0.52, 51.70, 0.34)))
            .build();

    @Test
    void returnsConfiguredBounds() throws Exception {
        mvc.perform(get("/api/v1/map/bounds"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.south").value(51.28))
                .andExpect(jsonPath("$.west").value(-0.52))
                .andExpect(jsonPath("$.north").value(51.70))
                .andExpect(jsonPath("$.east").value(0.34));
    }
}