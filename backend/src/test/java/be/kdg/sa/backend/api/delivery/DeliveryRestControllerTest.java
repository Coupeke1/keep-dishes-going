package be.kdg.sa.backend.api.delivery;

import be.kdg.sa.backend.api.delivery.dto.CreateDeliveryDto;
import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryRestController.class)
class DeliveryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryService deliveryService;

    private UUID orderId;
    private OrderId orderIdDomain;
    private Delivery delivery;
    private AddressDto pickupAddressDto;
    private AddressDto deliveryAddressDto;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        orderIdDomain = new OrderId(orderId);

        Address pickupAddress = new Address("Pickup St", "123", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "456", null, "2000", "Antwerp", "Belgium");

        delivery = Delivery.createNew(orderIdDomain, pickupAddress, deliveryAddress);

        pickupAddressDto = new AddressDto("Pickup St", "123", null, "1000", "Brussels", "Belgium");
        deliveryAddressDto = new AddressDto("Delivery St", "456", null, "2000", "Antwerp", "Belgium");
    }

    @Test
    @WithMockUser(roles = {"owner", "couriers"})
    void getDelivery_WithExistingDelivery_ShouldReturnDelivery() throws Exception {
        when(deliveryService.getById(orderIdDomain)).thenReturn(delivery);

        mockMvc.perform(get("/api/deliveries/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value(DeliveryStatus.OPEN.toString()))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.ACCEPTED.toString()))
                .andExpect(jsonPath("$.pickupAddress.street").value("Pickup St"))
                .andExpect(jsonPath("$.deliveryAddress.street").value("Delivery St"));
    }

    @Test
    @WithMockUser(roles = {"owner"})
    void createDelivery_WithValidData_ShouldReturnCreated() throws Exception {
        UUID newOrderId = UUID.randomUUID();
        Delivery newDelivery = Delivery.createNew(new OrderId(newOrderId),
                pickupAddressDto.toDomain(), deliveryAddressDto.toDomain());

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(newDelivery);

        CreateDeliveryDto createDto = new CreateDeliveryDto(pickupAddressDto, deliveryAddressDto);

        mockMvc.perform(post("/api/deliveries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.orderId").value(newOrderId.toString()))
                .andExpect(jsonPath("$.status").value(DeliveryStatus.OPEN.toString()));
    }

    @Test
    @WithMockUser(roles = {"owner"})
    void getDelivery_WithOwnerRole_ShouldReturnOk() throws Exception {
        when(deliveryService.getById(orderIdDomain)).thenReturn(delivery);

        mockMvc.perform(get("/api/deliveries/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"couriers"})
    void getDelivery_WithCouriersRole_ShouldReturnOk() throws Exception {
        when(deliveryService.getById(orderIdDomain)).thenReturn(delivery);

        mockMvc.perform(get("/api/deliveries/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"owner"})
    void createDelivery_WithTrailingSlash_ShouldReturnCreated() throws Exception {
        UUID newOrderId = UUID.randomUUID();
        Delivery newDelivery = Delivery.createNew(new OrderId(newOrderId),
                pickupAddressDto.toDomain(), deliveryAddressDto.toDomain());

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(newDelivery);

        CreateDeliveryDto createDto = new CreateDeliveryDto(pickupAddressDto, deliveryAddressDto);

        mockMvc.perform(post("/api/deliveries/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"owner"})
    void createDelivery_WithoutCsrf_ShouldReturnForbidden() throws Exception {
        CreateDeliveryDto createDto = new CreateDeliveryDto(pickupAddressDto, deliveryAddressDto);

        mockMvc.perform(post("/api/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verify(deliveryService, never()).createDelivery(any(UUID.class), any(Address.class), any(Address.class));
    }
}