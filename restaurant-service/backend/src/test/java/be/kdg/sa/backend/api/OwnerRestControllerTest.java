package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningDayDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningPeriodDto;
import be.kdg.sa.backend.api.dto.owner.OwnerOverviewDto;
import be.kdg.sa.backend.api.dto.restaurant.DishSummaryDto;
import be.kdg.sa.backend.api.dto.restaurant.RestaurantOverviewDto;
import be.kdg.sa.backend.application.services.restaurant.RestaurantService;
import be.kdg.sa.backend.domain.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OwnerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    @Test
    void getOverview_ownerRole_returnsOverview() throws Exception {
        UUID ownerUuid = UUID.randomUUID();

        List<DishSummaryDto> dishes = List.of();
        var resto = new RestaurantOverviewDto(
                UUID.randomUUID(), "Resto", true, 0L, dishes
        );
        var overview = new OwnerOverviewDto(ownerUuid, resto);

        given(restaurantService.getOverviewForOwner(ownerUuid)).willReturn(overview);

        mockMvc.perform(get("/api/owners/overview")
                        .with(jwt().jwt(j -> j.subject(ownerUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(ownerUuid.toString()))
                .andExpect(jsonPath("$.restaurant.name").value("Resto"));
    }

    @Test
    void createRestaurant_ownerRole_callsService() throws Exception {
        UUID ownerUuid = UUID.randomUUID();

        var period = new CreateOpeningPeriodDto(LocalTime.of(9, 0), LocalTime.of(17, 0));
        var day = new CreateOpeningDayDto(DayOfWeek.MONDAY, List.of(period));
        var opening = new CreateOpeningHoursDto(List.of(day));

        var req = new OwnerRestController.CreateRestaurantRequest(
                "NewResto",
                new Address("St", "1", null, "1000", "City", "BE"),
                "123",
                "a@b.com",
                "Italian",
                opening
        );

        mockMvc.perform(post("/api/owners/restaurant")
                        .with(jwt().jwt(j -> j.subject(ownerUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(restaurantService).create(
                eq(ownerUuid),
                eq("NewResto"),
                any(Address.class),
                eq("123"),
                eq("a@b.com"),
                eq("Italian"),
                any(CreateOpeningHoursDto.class)
        );
    }

    @Test
    void getOverview_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/owners/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRestaurant_unauthenticated_401() throws Exception {
        var req = new OwnerRestController.CreateRestaurantRequest(
                "NewResto",
                new Address("St", "1", null, "1000", "City", "BE"),
                "123",
                "a@b.com",
                "Italian",
                new CreateOpeningHoursDto(List.of())
        );

        mockMvc.perform(post("/api/owners/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
