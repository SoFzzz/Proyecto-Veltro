package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.catalog.dto.CategoryResponse;
import com.veltro.inventory.application.catalog.dto.CreateCategoryRequest;
import com.veltro.inventory.application.catalog.service.CategoryService;
import com.veltro.inventory.exception.NotFoundException;
import com.veltro.inventory.infrastructure.adapters.config.SecurityConfig;
import com.veltro.inventory.infrastructure.adapters.security.CustomUserDetailsService;
import com.veltro.inventory.infrastructure.adapters.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
 * Slice test for {@link CategoryController} (B1-03).
 *
 * Uses {@code @WebMvcTest} to load only the web layer. {@link CategoryService}
 * is mocked. Authenticated endpoints are exercised via Spring Security's
 * {@code SecurityMockMvcRequestPostProcessors.user(…)} helper.
 */
@WebMvcTest(controllers = CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    private static final String BASE = "/api/v1/categories";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    // Mocked to satisfy JwtAuthenticationFilter's dependencies.
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static CategoryResponse stubRootCategory() {
        return new CategoryResponse(1L, "Electronics", "Electronic goods", null, true,
                List.of(new CategoryResponse(2L, "Phones", null, 1L, true, List.of())));
    }

    private static CategoryResponse stubSubCategory() {
        return new CategoryResponse(2L, "Phones", null, 1L, true, List.of());
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
    // GET /categories — list roots
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /categories returns 200 with root category list")
    void listRoots_authenticated_returns200WithList() throws Exception {
        when(categoryService.findRoots()).thenReturn(List.of(stubRootCategory()));

        mockMvc.perform(get(BASE).with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].subCategories[0].name").value("Phones"));
    }

    @Test
    @DisplayName("GET /categories returns 200 for CASHIER role")
    void listRoots_cashierRole_returns200() throws Exception {
        when(categoryService.findRoots()).thenReturn(List.of());

        mockMvc.perform(get(BASE).with(user(cashierUser())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /categories returns 401 when unauthenticated")
    void listRoots_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /categories/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /categories/{id} returns 200 with category when found")
    void findById_existingCategory_returns200() throws Exception {
        when(categoryService.findById(1L)).thenReturn(stubRootCategory());

        mockMvc.perform(get(BASE + "/1").with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.parentCategoryId").doesNotExist());
    }

    @Test
    @DisplayName("GET /categories/{id} returns 404 when category does not exist")
    void findById_missingCategory_returns404() throws Exception {
        when(categoryService.findById(999L))
                .thenThrow(new NotFoundException("Category not found with id: 999"));

        mockMvc.perform(get(BASE + "/999").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /categories — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /categories with valid request returns 201 for ADMIN")
    void create_adminUser_validRequest_returns201() throws Exception {
        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(stubRootCategory());

        CreateCategoryRequest body = new CreateCategoryRequest("Electronics", "Electronic goods", null);

        mockMvc.perform(post(BASE)
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("POST /categories with valid request returns 201 for WAREHOUSE role")
    void create_warehouseUser_validRequest_returns201() throws Exception {
        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(stubSubCategory());

        CreateCategoryRequest body = new CreateCategoryRequest("Phones", null, 1L);

        mockMvc.perform(post(BASE)
                        .with(user(warehouseUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentCategoryId").value(1));
    }

    @Test
    @DisplayName("POST /categories returns 403 for CASHIER role")
    void create_cashierUser_returns403() throws Exception {
        CreateCategoryRequest body = new CreateCategoryRequest("Electronics", null, null);

        mockMvc.perform(post(BASE)
                        .with(user(cashierUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /categories with blank name returns 400")
    void create_blankName_returns400() throws Exception {
        CreateCategoryRequest body = new CreateCategoryRequest("", null, null);

        mockMvc.perform(post(BASE)
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /categories/{id}/deactivate — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /categories/{id}/deactivate returns 204 for ADMIN")
    void deactivate_adminUser_returns204() throws Exception {
        doNothing().when(categoryService).deactivate(1L);

        mockMvc.perform(put(BASE + "/1/deactivate").with(user(adminUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /categories/{id}/deactivate returns 404 when category not found")
    void deactivate_missingCategory_returns404() throws Exception {
        doThrow(new NotFoundException("Category not found with id: 999"))
                .when(categoryService).deactivate(999L);

        mockMvc.perform(put(BASE + "/999/deactivate").with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /categories/{id}/deactivate returns 403 for CASHIER role")
    void deactivate_cashierUser_returns403() throws Exception {
        mockMvc.perform(put(BASE + "/1/deactivate").with(user(cashierUser())))
                .andExpect(status().isForbidden());
    }
}
