package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.dish.CreateDishDto;
import be.kdg.sa.backend.api.dto.dish.DishForOrderingDto;
import be.kdg.sa.backend.api.dto.dish.UpdateDishDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningDayDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningPeriodDto;
import be.kdg.sa.backend.api.dto.restaurant.DecisionDto;
import be.kdg.sa.backend.api.dto.restaurant.OverrideOpeningDto;
import be.kdg.sa.backend.application.services.restaurant.RestaurantOrderService;
import be.kdg.sa.backend.application.services.restaurant.RestaurantService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private RestaurantOrderService restaurantOrderService;

    @Test
    void getRestaurantById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(restaurantService.getById(any(RestaurantId.class))).willReturn(null);

        mockMvc.perform(get("/api/restaurants/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRestaurantById_found_returns200_and_body() throws Exception {
        UUID id = UUID.randomUUID();

        Restaurant mocked = Mockito.mock(Restaurant.class);
        given(mocked.getId()).willReturn(new RestaurantId(id));
        given(mocked.getName()).willReturn("TestResto");
        given(mocked.getAddress()).willReturn(new Address("St", "1", null, "1000", "City", "BE"));
        given(mocked.getCuisineType()).willReturn("Italian");
        given(mocked.getPriceIndicator()).willReturn("€€");
        OpeningHours oh = Mockito.mock(OpeningHours.class);
        given(oh.days()).willReturn(Collections.emptyList());
        given(mocked.getOpeningHours()).willReturn(oh);
        given(mocked.isCurrentlyOpen()).willReturn(true);

        given(restaurantService.getById(any(RestaurantId.class))).willReturn(mocked);

        mockMvc.perform(get("/api/restaurants/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("TestResto"))
                .andExpect(jsonPath("$.address.city").value("City"))
                .andExpect(jsonPath("$.cuisineType").value("Italian"))
                .andExpect(jsonPath("$.isOpen").value(true));
    }

    @Test
    void getAllRestaurants_returnsList() throws Exception {
        Restaurant mocked = Mockito.mock(Restaurant.class);
        UUID id = UUID.randomUUID();
        given(mocked.getId()).willReturn(new RestaurantId(id));
        given(mocked.getName()).willReturn("R1");
        given(mocked.getAddress()).willReturn(new Address("St", "1", null, "1000", "City", "BE"));
        given(mocked.getCuisineType()).willReturn("Fusion");
        given(mocked.getPriceIndicator()).willReturn("€");
        OpeningHours oh = Mockito.mock(OpeningHours.class);
        given(oh.days()).willReturn(Collections.emptyList());
        given(mocked.getOpeningHours()).willReturn(oh);
        given(mocked.isCurrentlyOpen()).willReturn(false);

        given(restaurantService.getAll()).willReturn(List.of(mocked));

        mockMvc.perform(get("/api/restaurants").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("R1"));
    }

    @Test
    void isOpen_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        given(restaurantService.getById(any(RestaurantId.class))).willReturn(null);

        mockMvc.perform(get("/api/restaurants/{id}/open-status", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void isOpen_returns200_and_status() throws Exception {
        UUID id = UUID.randomUUID();
        Restaurant mocked = Mockito.mock(Restaurant.class);
        given(mocked.isCurrentlyOpen()).willReturn(true);
        given(restaurantService.getById(any(RestaurantId.class))).willReturn(mocked);

        mockMvc.perform(get("/api/restaurants/{id}/open-status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true));
    }

    @Test
    void addDish_ownerRole_returnsCreated() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        Dish mockedDish = Mockito.mock(Dish.class);
        given(mockedDish.getId()).willReturn(new DishId(dishId));
        given(mockedDish.getName()).willReturn("Pasta");
        given(mockedDish.getCategory()).willReturn(DishCategory.MAIN);
        given(mockedDish.getStatus()).willReturn(DishStatus.AVAILABLE);

        given(restaurantService.addDish(
                any(RestaurantId.class),
                anyString(),
                anyString(),
                anyDouble(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                any(DishCategory.class),
                any(DishStatus.class)
        )).willReturn(mockedDish);

        var createDishDto = new CreateDishDto(
                "Pasta",
                "Nice",
                10.5,
                false,
                false,
                false,
                DishCategory.MAIN,
                DishStatus.AVAILABLE
        );

        mockMvc.perform(post("/api/restaurants/{restaurantId}/dishes", rid)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDishDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateDish_ownerRole_returnsOk() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        Dish mocked = Mockito.mock(Dish.class);
        given(mocked.getId()).willReturn(new DishId(dishId));
        given(mocked.getName()).willReturn("Pasta Updated");
        given(mocked.getCategory()).willReturn(DishCategory.MAIN);
        given(mocked.getStatus()).willReturn(DishStatus.AVAILABLE);

        given(restaurantService.updateDish(any(RestaurantId.class), any(DishId.class), any()))
                .willReturn(mocked);

        var updateDto = new UpdateDishDto(
                "Pasta Updated",
                "Better",
                11.0,
                false,
                false,
                false,
                DishCategory.MAIN,
                DishStatus.AVAILABLE,
                null
        );

        mockMvc.perform(put("/api/restaurants/{restaurantId}/dishes/{dishId}", rid, dishId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void changeDishStatus_ownerRole_returnsOk() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        Dish mocked = Mockito.mock(Dish.class);
        given(mocked.getId()).willReturn(new DishId(dishId));
        given(restaurantService.updateDishStatus(any(RestaurantId.class), any(DishId.class), eq("CONCEPT")))
                .willReturn(mocked);
        given(mocked.getCategory()).willReturn(DishCategory.MAIN);
        given(mocked.getStatus()).willReturn(DishStatus.AVAILABLE);

        mockMvc.perform(patch("/api/restaurants/{restaurantId}/dishes/{dishId}/status", rid, dishId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .param("status", "CONCEPT"))
                .andExpect(status().isOk());
    }

    @Test
    void removeDish_ownerRole_returnsNoContent() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        mockMvc.perform(delete("/api/restaurants/{restaurantId}/dishes/{dishId}", rid, dishId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void publishAllDishes_ownerRole_returnsOk() throws Exception {
        UUID rid = UUID.randomUUID();

        mockMvc.perform(post("/api/restaurants/{restaurantId}/dishes/publish", rid)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isOk());
    }

    @Test
    void overrideOpeningStatus_ownerRole_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        var overrideDto = new OverrideOpeningDto(true, LocalDateTime.of(2099, 12, 31, 23, 59));

        mockMvc.perform(patch("/api/restaurants/{id}/status-override", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overrideDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void clearOverride_ownerRole_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/restaurants/{id}/status-override", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateOpeningHours_ownerRole_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        var period1 = new CreateOpeningPeriodDto(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"));
        var period2 = new CreateOpeningPeriodDto(LocalTime.parse("13:00:00"), LocalTime.parse("17:00:00"));
        var day = new CreateOpeningDayDto(DayOfWeek.MONDAY, List.of(period1, period2));
        var createOpeningHoursDto = new CreateOpeningHoursDto(List.of(day));

        mockMvc.perform(put("/api/restaurants/{id}/opening-hours", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOpeningHoursDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getDish_returnsOk() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        var dishForOrdering = new DishForOrderingDto(dishId, "Soup", "Good", 5.0, false, false, false, "STARTER", "AVAILABLE");
        given(restaurantService.getRestaurantDishes(any(RestaurantId.class), any(DishId.class))).willReturn(dishForOrdering);

        mockMvc.perform(get("/api/restaurants/{id}/dishes/{dishId}", rid, dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dishId.toString()))
                .andExpect(jsonPath("$.name").value("Soup"));
    }

    @Test
    void getDishes_returnsOk_and_emptyByDefault() throws Exception {
        UUID rid = UUID.randomUUID();
        given(restaurantService.getNonConceptDishesFromRestaurant(any(RestaurantId.class))).willReturn(Collections.emptyList());
        given(restaurantService.getAllDishesFromRestaurant(any(RestaurantId.class))).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/restaurants/{id}/dishes", rid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPendingOrders_ownerRole_returnsOk() throws Exception {
        UUID rid = UUID.randomUUID();
        given(restaurantOrderService.getPendingOrders(eq(rid))).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/restaurants/{restaurantId}/orders", rid)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOrder_ownerRole_notFound_returns404() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        given(restaurantOrderService.getOrder(orderId)).willReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/restaurants/{restaurantId}/orders/{orderId}", rid, orderId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void decideOrder_ownerRole_applied_noContent_elseOk() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(restaurantOrderService.applyDecision(orderId, "ACCEPT", "ok")).willReturn(true);
        String payload = objectMapper.writeValueAsString(new DecisionDto("ACCEPT", "ok"));

        mockMvc.perform(post("/api/restaurants/{restaurantId}/orders/{orderId}/decision", rid, orderId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());

        given(restaurantOrderService.applyDecision(orderId, "REJECT", "bad")).willReturn(false);
        payload = objectMapper.writeValueAsString(new DecisionDto("REJECT", "bad"));

        mockMvc.perform(post("/api/restaurants/{restaurantId}/orders/{orderId}/decision", rid, orderId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void readyOrder_ownerRole_applied_noContent_elseOk() throws Exception {
        UUID rid = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(restaurantOrderService.markReadyForPickup(orderId)).willReturn(true);
        mockMvc.perform(post("/api/restaurants/{restaurantId}/orders/{orderId}/ready", rid, orderId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isNoContent());

        given(restaurantOrderService.markReadyForPickup(orderId)).willReturn(false);
        mockMvc.perform(post("/api/restaurants/{restaurantId}/orders/{orderId}/ready", rid, orderId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_owner"))))
                .andExpect(status().isOk());
    }
}
