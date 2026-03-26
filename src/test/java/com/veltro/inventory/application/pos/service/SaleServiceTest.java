package com.veltro.inventory.application.pos.service;

import com.veltro.inventory.application.audit.command.AuditCommandExecutor;
import com.veltro.inventory.application.pos.dto.AddItemRequest;
import com.veltro.inventory.application.pos.dto.ConfirmSaleRequest;
import com.veltro.inventory.application.pos.dto.ModifyItemRequest;
import com.veltro.inventory.application.pos.dto.SaleResponse;
import com.veltro.inventory.application.pos.event.SaleCompletedEvent;
import com.veltro.inventory.application.pos.event.SaleVoidedEvent;
import com.veltro.inventory.application.pos.mapper.SaleMapper;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.catalog.ports.ProductRepository;
import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.domain.pos.ports.SaleRepository;
import com.veltro.inventory.exception.InvalidPaymentException;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SaleService} (B2-01).
 *
 * <p>Tests sale lifecycle operations (start, add, modify, remove, confirm, void)
 * and payment validation logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AuditCommandExecutor auditCommandExecutor;

    private SaleService saleService;

    @BeforeEach
    void setUp() {
        // Manual service instantiation
        saleService = new SaleService(saleRepository, productRepository, saleMapper, applicationEventPublisher, auditCommandExecutor);
        
        // Mock authenticated user
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CASHIER")))
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ProductEntity createProduct(Long id, String name, BigDecimal salePrice) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setSalePrice(salePrice);
        return product;
    }

    private SaleEntity createSale(Long id, String saleNumber, SaleStatus status) {
        SaleEntity sale = new SaleEntity();
        sale.setId(id);
        sale.setSaleNumber(saleNumber);
        sale.setStatus(status);
        sale.setCashierId(100L);
        return sale;
    }

    private SaleDetailEntity createSaleDetail(ProductEntity product, int quantity, BigDecimal unitPrice) {
        SaleDetailEntity detail = new SaleDetailEntity();
        detail.setProductId(product.getId());
        detail.setProductName(product.getName());
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.calculateSubtotal();
        detail.setActive(true);
        return detail;
    }

    private SaleResponse createSaleResponse(Long id, String saleNumber) {
        return new SaleResponse(id, saleNumber, SaleStatus.IN_PROGRESS, 100L,
                "0.0000", "0.0000", null, null, PaymentMethod.CASH, null, List.of(), 0L, null);
    }

    // -------------------------------------------------------------------------
    // startSale
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("startSale creates IN_PROGRESS sale with generated number")
    void startSale_createsInProgressSale() {
        when(saleRepository.getNextSaleSequenceValue()).thenReturn(123L);
        SaleEntity savedSale = createSale(1L, "VLT-2026-000123", SaleStatus.IN_PROGRESS);
        when(saleRepository.save(any(SaleEntity.class))).thenReturn(savedSale);
        when(saleMapper.toResponse(savedSale)).thenReturn(createSaleResponse(1L, "VLT-2026-000123"));

        SaleResponse response = saleService.startSale();

        assertThat(response.saleNumber()).isEqualTo("VLT-2026-000123");
        assertThat(response.status()).isEqualTo(SaleStatus.IN_PROGRESS);
        verify(saleRepository).getNextSaleSequenceValue();
        verify(saleRepository).save(any(SaleEntity.class));
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById returns sale when exists")
    void findById_existingSale_returnsSaleResponse() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById throws NotFoundException when sale not found")
    void findById_nonExistentSale_throwsNotFoundException() {
        when(saleRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Sale not found with id: 999");
    }

    // -------------------------------------------------------------------------
    // addItem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addItem adds product to sale and updates totals")
    void addItem_validProduct_addsItemAndUpdatesTotals() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("15.0000"));
        AddItemRequest request = new AddItemRequest(10L, 2);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        // Do not add item directly—only go through service call
        // SaleDetailEntity detail = createSaleDetail(product, 2, new BigDecimal("15.0000"));

        SaleResponse response = saleService.addItem(1L, request);

        verify(saleRepository).save(sale);
        assertThat(sale.getDetails()).hasSize(1);
        SaleDetailEntity savedDetail = sale.getDetails().get(0);
        assertThat(savedDetail.getProductId()).isEqualTo(10L);
        assertThat(savedDetail.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("addItem throws NotFoundException when product not found")
    void addItem_nonExistentProduct_throwsNotFoundException() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        AddItemRequest request = new AddItemRequest(999L, 2);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(productRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.addItem(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    // -------------------------------------------------------------------------
    // modifyItem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifyItem updates quantity and recalculates totals")
    void modifyItem_validItemId_updatesQuantity() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("10.0000"));
        SaleDetailEntity detail = createSaleDetail(product, 2, new BigDecimal("10.0000"));
        detail.setId(5L);
        sale.addItem(detail);

        ModifyItemRequest request = new ModifyItemRequest(4);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.modifyItem(1L, 5L, request);

        verify(saleRepository).save(sale);
        assertThat(detail.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("modifyItem throws NotFoundException when item not found")
    void modifyItem_nonExistentItemId_throwsNotFoundException() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ModifyItemRequest request = new ModifyItemRequest(3);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.modifyItem(1L, 999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Sale detail not found with id: 999");
    }

    // -------------------------------------------------------------------------
    // removeItem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeItem soft deletes item and recalculates totals")
    void removeItem_validItemId_softDeletesItem() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("10.0000"));
        SaleDetailEntity detail = createSaleDetail(product, 2, new BigDecimal("10.0000"));
        detail.setId(5L);
        sale.addItem(detail);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.removeItem(1L, 5L);

        verify(saleRepository).save(sale);
        assertThat(detail.isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // confirm - Payment Validation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirm with CASH and null amountReceived throws InvalidPaymentException")
    void confirm_cash_withoutAmountReceived_throws() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("10.0000"));
        SaleDetailEntity detail = createSaleDetail(product, 1, new BigDecimal("10.0000"));
        sale.addItem(detail);

        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CASH, null);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.confirm(1L, request))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("Amount received is required for cash payments");
    }

    @Test
    @DisplayName("confirm with CASH and insufficient amountReceived throws InvalidPaymentException")
    void confirm_cash_amountLessThanTotal_throws() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("50.0000"));
SaleDetailEntity detail = createSaleDetail(product, 1, new BigDecimal("50.0000"));
sale.addItem(detail);
sale.recalculateTotals();


        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CASH, new BigDecimal("40.0000"));

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.confirm(1L, request))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("Amount received must be greater than or equal to total for cash payments");
    }

    @Test
    @DisplayName("confirm with CASH and valid amountReceived calculates change")
    void confirm_cash_valid_calculatesChange() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("30.0000"));
SaleDetailEntity detail = createSaleDetail(product, 2, new BigDecimal("30.0000"));
sale.addItem(detail);
sale.recalculateTotals();


        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CASH, new BigDecimal("100.0000"));

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.confirm(1L, request);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getAmountReceived()).isEqualByComparingTo(new BigDecimal("100.0000"));
        assertThat(sale.getChange()).isEqualByComparingTo(new BigDecimal("40.0000")); // 100 - 60
        verify(applicationEventPublisher).publishEvent(any(SaleCompletedEvent.class));
    }

    @Test
    @DisplayName("confirm with CARD and no amountReceived succeeds")
    void confirm_card_noAmountReceived_succeeds() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);
        ProductEntity product = createProduct(10L, "Widget", new BigDecimal("25.0000"));
        SaleDetailEntity detail = createSaleDetail(product, 1, new BigDecimal("25.0000"));
        sale.addItem(detail);

        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CARD, null);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.confirm(1L, request);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getAmountReceived()).isNull();
        assertThat(sale.getChange()).isNull();
        verify(applicationEventPublisher).publishEvent(any(SaleCompletedEvent.class));
    }

    // -------------------------------------------------------------------------
    // voidSale
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("voidSale transitions COMPLETED to VOIDED and publishes event")
    void voidSale_completedSale_transitionsToVoided() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.COMPLETED);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(createSaleResponse(1L, "VLT-2026-000001"));

        SaleResponse response = saleService.voidSale(1L);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.VOIDED);
        verify(applicationEventPublisher).publishEvent(any(SaleVoidedEvent.class));
    }

    @Test
    @DisplayName("voidSale on IN_PROGRESS throws InvalidStateTransitionException")
    void voidSale_inProgressSale_throwsInvalidStateTransition() {
        SaleEntity sale = createSale(1L, "VLT-2026-000001", SaleStatus.IN_PROGRESS);

        when(saleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.voidSale(1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Sale VLT-2026-000001 is in IN_PROGRESS status. Only completed sales can be voided.");
    }
}
