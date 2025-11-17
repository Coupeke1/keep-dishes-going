package be.kdg.sa.backend.api.driver;

import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveriesResponse;
import be.kdg.sa.backend.api.driver.dto.CreateDriverDto;
import be.kdg.sa.backend.application.services.driver.DriverService;
import be.kdg.sa.backend.config.SecurityConfigTest;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverRestController.class)
@Import(SecurityConfigTest.class)
class DriverRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DriverService driverService;

    private UUID driverUuid;
    private DriverId driverId;
    private Driver driver;
    private Address address;

    @BeforeEach
    void setUp() {
        driverUuid = UUID.randomUUID();
        driverId = new DriverId(driverUuid);
        address = new Address("Main St", "123", "A", "1000", "Brussels", "BE");
        driver = new Driver(driverId, "John Doe", "john@example.com",
                "+3212345678", "BE123456789", address, null);
    }

    @Test
    void getCurrentDriver_ShouldReturnDriver_WhenCourierRole() throws Exception {
        when(driverService.getById(driverId)).thenReturn(driver);

        mockMvc.perform(get("/api/drivers/me")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverUuid.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(driverService).getById(driverId);
    }

    @Test
    void registerDriver_ShouldCreateDriver_WhenCourierRole() throws Exception {
        CreateDriverDto dto = new CreateDriverDto(
                "John Doe", "john@example.com", "+3212345678", "BE123456789",
                new be.kdg.sa.backend.api.dto.AddressDto("Main St", "123", "A", "1000", "Brussels", "BE")
        );

        when(driverService.registerDriver(
                eq(new DriverId(driverUuid)),
                eq("John Doe"),
                eq("john@example.com"),
                eq("+3212345678"),
                eq("BE123456789"),
                any(Address.class))
        ).thenReturn(driver);

        mockMvc.perform(post("/api/drivers/register")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(driverUuid.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(driverService).registerDriver(eq(new DriverId(driverUuid)),
                eq("John Doe"), eq("john@example.com"), eq("+3212345678"),
                eq("BE123456789"), any(Address.class));
    }

    @Test
    void claimDelivery_ShouldInvokeService_WhenCourierRole() throws Exception {
        UUID deliveryUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/drivers/claim/{deliveryId}", deliveryUuid)
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(driverService).claimDelivery(driverUuid, deliveryUuid);
    }

    @Test
    void startPickup_ShouldInvokeService_WhenCourierRole() throws Exception {
        mockMvc.perform(post("/api/drivers/pickup")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(driverService).startDelivery(driverUuid);
    }

    @Test
    void completeDelivery_ShouldReturnPayout_WhenCourierRole() throws Exception {
        BigDecimal payout = new BigDecimal("15.50");
        when(driverService.completeDelivery(driverUuid)).thenReturn(payout);

        mockMvc.perform(post("/api/drivers/complete")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(driverUuid.toString()))
                .andExpect(jsonPath("$.payout").value(15.50))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(driverService).completeDelivery(driverUuid);
    }

    @Test
    void cancelDelivery_ShouldInvokeService_WhenCourierRole() throws Exception {
        mockMvc.perform(post("/api/drivers/cancel")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers")))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(driverService).cancelDelivery(driverUuid);
    }

    @Test
    void getCompletedDeliveries_ShouldReturnResponse_WhenCourierRole() throws Exception {
        CompletedDeliveriesResponse response =
                new CompletedDeliveriesResponse(driverUuid, new BigDecimal("100.50"), List.of());
        when(driverService.getCompletedDeliveriesForDriver(driverId)).thenReturn(response);

        mockMvc.perform(get("/api/drivers/completed-deliveries")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_couriers"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(driverUuid.toString()))
                .andExpect(jsonPath("$.total").value(100.50))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(driverService).getCompletedDeliveriesForDriver(driverId);
    }

    @Test
    void getCurrentDriver_ShouldReturnUnauthorized_WhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/drivers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerDriver_ShouldReturnUnauthorized_WhenNoJwt() throws Exception {
        CreateDriverDto dto = new CreateDriverDto("John", "john@example.com", "+32123", "BE123",
                new be.kdg.sa.backend.api.dto.AddressDto("Main", "1", null, "1000", "Brussels", "BE"));

        mockMvc.perform(post("/api/drivers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerDriver_ShouldReturnForbidden_WhenWrongRole() throws Exception {
        CreateDriverDto dto = new CreateDriverDto("John", "john@example.com", "+32123", "BE123",
                new be.kdg.sa.backend.api.dto.AddressDto("Main", "1", null, "1000", "Brussels", "BE"));

        mockMvc.perform(post("/api/drivers/register")
                        .with(jwt().jwt(j -> j.subject(driverUuid.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_owner")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
