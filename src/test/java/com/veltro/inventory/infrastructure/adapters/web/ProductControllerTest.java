package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.catalog.dto.CreateProductRequest;
import com.veltro.inventory.application.catalog.dto.ProductResponse;
import com.veltro.inventory.application.catalog.service.ProductService;
import com.veltro.inventory.exception.InvalidPriceException;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link ProductController} (B1-03).
 *
 * Uses {@code @WebMvcTest} to load only the web layer. {@link ProductService}
 * is mocked. Authenticated endpoints are exercised via Spring Security's
 * {@code SecurityMockMvcRequestPostProcessors.user(…)} helper.
 */
@WebMvcTest(controllers = ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    private static final String BASE = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    // Mocked to satisfy JwtAuthenticationFilter's dependencies.
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ProductResponse stubProduct() {
        return new ProductResponse(
                1L, "Widget A", "BARC-001", "WGT-001", "A widget",
                "5.0000", "9.9900", 10L, "Electronics", true);
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
    // GET /products — paginated listing
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /products returns 200 with paginated product list")
    void listProducts_authenticated_returns200WithPage() throws Exception {
        PageImpl<ProductResponse> page = new PageImpl<>(
                List.of(stubProduct()), PageRequest.of(0, 20), 1);
        when(productService.findAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE).with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Widget A"))
                .andExpect(jsonPath("$.content[0].barcode").value("BARC-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /products returns 200 for CASHIER role")
    void listProducts_cashierRole_returns200() throws Exception {
        PageImpl<ProductResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(productService.findAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE).with(user(cashierUser())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /products returns 401 when unauthenticated")
    void listProducts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /products/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /products/{id} returns 200 with product when found")
    void findById_existingProduct_returns200() throws Exception {
        when(productService.findById(1L)).thenReturn(stubProduct());

        mockMvc.perform(get(BASE + "/1").with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Widget A"))
                .andExpect(jsonPath("$.costPrice").value("5.0000"))
                .andExpect(jsonPath("$.salePrice").value("9.9900"));
    }

    @Test
    @DisplayName("GET /products/{id} returns 404 when product does not exist")
    void findById_missingProduct_returns404() throws Exception {
        when(productService.findById(999L))
                .thenThrow(new NotFoundException("Product not found with id: 999"));

        mockMvc.perform(get(BASE + "/999").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /products/barcode/{barcode}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /products/barcode/{barcode} returns 200 when barcode exists")
    void findByBarcode_existingBarcode_returns200() throws Exception {
        when(productService.findByBarcode("BARC-001")).thenReturn(stubProduct());

        mockMvc.perform(get(BASE + "/barcode/BARC-001").with(user(cashierUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("BARC-001"))
                .andExpect(jsonPath("$.name").value("Widget A"));
    }

    @Test
    @DisplayName("GET /products/barcode/{barcode} returns 404 when barcode not found")
    void findByBarcode_unknownBarcode_returns404() throws Exception {
        when(productService.findByBarcode("UNKNOWN"))
                .thenThrow(new NotFoundException("Product not found with barcode: UNKNOWN"));

        mockMvc.perform(get(BASE + "/barcode/UNKNOWN").with(user(cashierUser())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /products — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /products with valid request returns 201 for ADMIN")
    void create_adminUser_validRequest_returns201() throws Exception {
        when(productService.create(any(CreateProductRequest.class))).thenReturn(stubProduct());

        CreateProductRequest body = new CreateProductRequest(
                "Widget A", "BARC-001", "WGT-001", "A widget",
                new BigDecimal("5.0000"), new BigDecimal("9.9900"), 10L);

        mockMvc.perform(post(BASE)
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Widget A"));
    }

    @Test
    @DisplayName("POST /products with valid request returns 201 for WAREHOUSE role")
    void create_warehouseUser_validRequest_returns201() throws Exception {
        when(productService.create(any(CreateProductRequest.class))).thenReturn(stubProduct());

        CreateProductRequest body = new CreateProductRequest(
                "Widget A", "BARC-001", "WGT-001", "A widget",
                new BigDecimal("5.0000"), new BigDecimal("9.9900"), null);

        mockMvc.perform(post(BASE)
                        .with(user(warehouseUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /products returns 403 for CASHIER role")
    void create_cashierUser_returns403() throws Exception {
        CreateProductRequest body = new CreateProductRequest(
                "Widget A", "BARC-001", "WGT-001", "A widget",
                new BigDecimal("5.0000"), new BigDecimal("9.9900"), null);

        mockMvc.perform(post(BASE)
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /products with blank name returns 400")
    void create_blankName_returns400() throws Exception {
        CreateProductRequest body = new CreateProductRequest(
                "", "BARC-001", "WGT-001", "A widget",
                new BigDecimal("5.0000"), new BigDecimal("9.9900"), null);

        mockMvc.perform(post(BASE)
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /products returns 422 when salePrice < costPrice")
    void create_invalidPrice_returns422() throws Exception {
        when(productService.create(any(CreateProductRequest.class)))
                .thenThrow(new InvalidPriceException(
                        "Sale price (4.0000) must be greater than or equal to cost price (5.0000)."));

        CreateProductRequest body = new CreateProductRequest(
                "Widget A", "BARC-001", "WGT-001", "A widget",
                new BigDecimal("5.0000"), new BigDecimal("4.0000"), null);

        mockMvc.perform(post(BASE)
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_PRICE"));
    }

    // -------------------------------------------------------------------------
    // PUT /products/{id}/deactivate — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /products/{id}/deactivate returns 204 for ADMIN")
    void deactivate_adminUser_returns204() throws Exception {
        doNothing().when(productService).deactivate(1L);

        mockMvc.perform(put(BASE + "/1/deactivate").with(user(adminUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /products/{id}/deactivate returns 404 when product not found")
    void deactivate_missingProduct_returns404() throws Exception {
        doThrow(new NotFoundException("Product not found with id: 999"))
                .when(productService).deactivate(999L);

        mockMvc.perform(put(BASE + "/999/deactivate").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /products/{id}/deactivate returns 403 for CASHIER role")
    void deactivate_cashierUser_returns403() throws Exception {
        mockMvc.perform(put(BASE + "/1/deactivate").with(user(cashierUser())))
                .andExpect(status().isForbidden());
    }
}
