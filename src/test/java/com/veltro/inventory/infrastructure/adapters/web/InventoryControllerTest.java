package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.inventory.dto.InventoryMovementResponse;
import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.application.inventory.dto.StockAdjustmentRequest;
import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.dto.UpdateStockLimitsRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.exception.InsufficientStockException;
import com.veltro.inventory.exception.NotFoundException;
import com.veltro.inventory.infrastructure.adapters.config.SecurityConfig;
import com.veltro.inventory.infrastructure.adapters.security.CustomUserDetailsService;
import com.veltro.inventory.infrastructure.adapters.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link InventoryController} (B1-04).
 *
 * Uses {@code @WebMvcTest} to load only the web layer. {@link InventoryService}
 * is mocked. Authenticated endpoints are exercised via Spring Security's
 * {@code SecurityMockMvcRequestPostProcessors.user(…)} helper.
 */
@WebMvcTest(controllers = InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    private static final String BASE = "/api/v1/inventory";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InventoryService inventoryService;

    // Mocked to satisfy JwtAuthenticationFilter's dependencies.
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static InventoryResponse stubInventory() {
        return new InventoryResponse(1L, 10L, "Widget A", 50, 5, 200, true, 0L);
    }

    private static InventoryMovementResponse stubMovement() {
        return new InventoryMovementResponse(
                1L, 1L, "ENTRY", 10, 40, 50, "restock", Instant.now(), "admin");
    }

    private static UserDetails adminUser() {
        return User.withUsername("admin").password("irrelevant").roles("ADMIN").build();
    }

    private static UserDetails warehouseUser() {
        return User.withUsername("warehouse").password("irrelevant").roles("WAREHOUSE").build();
    }

    private static UserDetails cashierUser() {
        return User.withUsername("cashier").password("irrelevant").roles("CASHIER").build();
    }

    // -------------------------------------------------------------------------
    // GET /{productId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /inventory/{productId} returns 200 with inventory for authenticated user")
    void getByProductId_authenticatedUser_returns200() throws Exception {
        when(inventoryService.findByProductId(10L)).thenReturn(stubInventory());

        mockMvc.perform(get(BASE + "/10").with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.productName").value("Widget A"))
                .andExpect(jsonPath("$.currentStock").value(50));
    }

    @Test
    @DisplayName("GET /inventory/{productId} returns 200 for CASHIER role")
    void getByProductId_cashierRole_returns200() throws Exception {
        when(inventoryService.findByProductId(10L)).thenReturn(stubInventory());

        mockMvc.perform(get(BASE + "/10").with(user(cashierUser())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /inventory/{productId} returns 404 when no inventory for product")
    void getByProductId_unknownProduct_returns404() throws Exception {
        when(inventoryService.findByProductId(99L))
                .thenThrow(new NotFoundException("Inventory not found for product id: 99"));

        mockMvc.perform(get(BASE + "/99").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /inventory/{productId} returns 401 when unauthenticated")
    void getByProductId_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/10"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /{productId}/movements
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /inventory/{productId}/movements returns 200 with paginated movements")
    void getMovements_authenticated_returns200WithPage() throws Exception {
        PageImpl<InventoryMovementResponse> page = new PageImpl<>(
                List.of(stubMovement()), PageRequest.of(0, 20), 1);
        when(inventoryService.getMovements(eq(10L), any())).thenReturn(page);

        mockMvc.perform(get(BASE + "/10/movements").with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].movementType").value("ENTRY"))
                .andExpect(jsonPath("$.content[0].quantity").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /inventory/{productId}/movements returns 404 when product has no inventory")
    void getMovements_unknownProduct_returns404() throws Exception {
        when(inventoryService.getMovements(eq(99L), any()))
                .thenThrow(new NotFoundException("Inventory not found for product id: 99"));

        mockMvc.perform(get(BASE + "/99/movements").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/entry — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/entry returns 200 for ADMIN with valid request")
    void recordEntry_adminUser_validRequest_returns200() throws Exception {
        when(inventoryService.recordEntry(eq(10L), any(StockEntryRequest.class)))
                .thenReturn(stubInventory());

        StockEntryRequest body = new StockEntryRequest(5, "restock");

        mockMvc.perform(post(BASE + "/10/entry")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock").value(50));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/entry returns 200 for WAREHOUSE role")
    void recordEntry_warehouseUser_validRequest_returns200() throws Exception {
        when(inventoryService.recordEntry(eq(10L), any(StockEntryRequest.class)))
                .thenReturn(stubInventory());

        StockEntryRequest body = new StockEntryRequest(5, "restock");

        mockMvc.perform(post(BASE + "/10/entry")
                        .with(user(warehouseUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /inventory/{productId}/entry returns 403 for CASHIER role")
    void recordEntry_cashierUser_returns403() throws Exception {
        StockEntryRequest body = new StockEntryRequest(5, "restock");

        mockMvc.perform(post(BASE + "/10/entry")
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /inventory/{productId}/entry returns 400 when quantity is zero")
    void recordEntry_zeroQuantity_returns400() throws Exception {
        StockEntryRequest body = new StockEntryRequest(0, "invalid");

        mockMvc.perform(post(BASE + "/10/entry")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/exit — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/exit returns 200 for ADMIN with sufficient stock")
    void recordExit_adminUser_sufficientStock_returns200() throws Exception {
        when(inventoryService.recordExit(eq(10L), any(StockExitRequest.class)))
                .thenReturn(stubInventory());

        StockExitRequest body = new StockExitRequest(5, "shrinkage");

        mockMvc.perform(post(BASE + "/10/exit")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /inventory/{productId}/exit returns 422 with INSUFFICIENT_STOCK when stock is too low (AC-04)")
    void recordExit_insufficientStock_returns422() throws Exception {
        when(inventoryService.recordExit(eq(10L), any(StockExitRequest.class)))
                .thenThrow(new InsufficientStockException("Widget A", 3, 10));

        StockExitRequest body = new StockExitRequest(10, "overshoot");

        mockMvc.perform(post(BASE + "/10/exit")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/exit returns 403 for CASHIER role")
    void recordExit_cashierUser_returns403() throws Exception {
        StockExitRequest body = new StockExitRequest(5, "shrinkage");

        mockMvc.perform(post(BASE + "/10/exit")
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /inventory/{productId}/exit returns 400 when quantity is zero")
    void recordExit_zeroQuantity_returns400() throws Exception {
        StockExitRequest body = new StockExitRequest(0, "invalid");

        mockMvc.perform(post(BASE + "/10/exit")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/adjustment — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/adjustment returns 200 for ADMIN with valid request")
    void recordAdjustment_adminUser_validRequest_returns200() throws Exception {
        when(inventoryService.recordAdjustment(eq(10L), any(StockAdjustmentRequest.class)))
                .thenReturn(stubInventory());

        StockAdjustmentRequest body = new StockAdjustmentRequest(45, "physical count");

        mockMvc.perform(post(BASE + "/10/adjustment")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock").value(50));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/adjustment returns 403 for CASHIER role")
    void recordAdjustment_cashierUser_returns403() throws Exception {
        StockAdjustmentRequest body = new StockAdjustmentRequest(45, "physical count");

        mockMvc.perform(post(BASE + "/10/adjustment")
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /inventory/{productId}/adjustment returns 400 when reason is blank")
    void recordAdjustment_blankReason_returns400() throws Exception {
        StockAdjustmentRequest body = new StockAdjustmentRequest(45, "");

        mockMvc.perform(post(BASE + "/10/adjustment")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /{productId}/limits — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /inventory/{productId}/limits returns 200 for ADMIN with valid request")
    void updateLimits_adminUser_validRequest_returns200() throws Exception {
        when(inventoryService.updateLimits(eq(10L), any(UpdateStockLimitsRequest.class)))
                .thenReturn(stubInventory());

        UpdateStockLimitsRequest body = new UpdateStockLimitsRequest(5, 200);

        mockMvc.perform(put(BASE + "/10/limits")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minStock").value(5))
                .andExpect(jsonPath("$.maxStock").value(200));
    }

    @Test
    @DisplayName("PUT /inventory/{productId}/limits returns 200 for WAREHOUSE role")
    void updateLimits_warehouseUser_validRequest_returns200() throws Exception {
        when(inventoryService.updateLimits(eq(10L), any(UpdateStockLimitsRequest.class)))
                .thenReturn(stubInventory());

        UpdateStockLimitsRequest body = new UpdateStockLimitsRequest(5, 200);

        mockMvc.perform(put(BASE + "/10/limits")
                        .with(user(warehouseUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /inventory/{productId}/limits returns 403 for CASHIER role")
    void updateLimits_cashierUser_returns403() throws Exception {
        UpdateStockLimitsRequest body = new UpdateStockLimitsRequest(5, 200);

        mockMvc.perform(put(BASE + "/10/limits")
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /inventory/{productId}/limits returns 401 when unauthenticated")
    void updateLimits_unauthenticated_returns401() throws Exception {
        UpdateStockLimitsRequest body = new UpdateStockLimitsRequest(5, 200);

        mockMvc.perform(put(BASE + "/10/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
