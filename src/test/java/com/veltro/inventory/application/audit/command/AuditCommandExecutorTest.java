package com.veltro.inventory.application.audit.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import com.veltro.inventory.domain.audit.model.AuditRecordEntity;
import com.veltro.inventory.domain.audit.ports.AuditRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditCommandExecutor (B3-03).
 */
@ExtendWith(MockitoExtension.class)
class AuditCommandExecutorTest {

    @Mock
    private AuditRecordRepository auditRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AuditCommandExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AuditCommandExecutor(auditRepository, objectMapper);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void shouldExecuteOperationWithBeforeAndAfterSnapshots() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john.doe");

        Map<String, Object> beforeData = Map.of("status", "PENDING");
        Map<String, Object> afterData = Map.of("status", "CONFIRMED");

        when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"status\":\"PENDING\"}");
        when(objectMapper.writeValueAsString(afterData)).thenReturn("{\"status\":\"CONFIRMED\"}");

        AuditContext context = new AuditContext("192.168.1.100");
        String expectedResult = "Operation completed";

        // When
        String result = executor.execute(
                AuditEntityType.SALE,
                123L,
                AuditAction.CONFIRM,
                () -> beforeData,
                () -> expectedResult,
                r -> afterData,
                context
        );

        // Then
        assertThat(result).isEqualTo(expectedResult);

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.SALE);
        assertThat(saved.getEntityId()).isEqualTo(123L);
        assertThat(saved.getAction()).isEqualTo(AuditAction.CONFIRM);
        assertThat(saved.getUsername()).isEqualTo("john.doe");
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(saved.getPreviousData()).isEqualTo("{\"status\":\"PENDING\"}");
        assertThat(saved.getNewData()).isEqualTo("{\"status\":\"CONFIRMED\"}");

    }

    @Test
    void shouldHandleNullBeforeSnapshot() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("jane.doe");

        Map<String, Object> afterData = Map.of("status", "CONFIRMED");
        when(objectMapper.writeValueAsString(afterData)).thenReturn("{\"status\":\"CONFIRMED\"}");

        AuditContext context = new AuditContext("10.0.0.1");

        // When
        String result = executor.execute(
                AuditEntityType.SALE,
                100L,
                AuditAction.CONFIRM,
                null,  // No before snapshot
                () -> "Created",
                r -> afterData,
                context
        );

        // Then
        assertThat(result).isEqualTo("Created");

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getPreviousData()).isNull();
        assertThat(saved.getNewData()).isEqualTo("{\"status\":\"CONFIRMED\"}");
    }

    @Test
    void shouldHandleNullAfterSnapshot() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        Map<String, Object> beforeData = Map.of("status", "PENDING");
        when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"status\":\"PENDING\"}");

        AuditContext context = new AuditContext("172.16.0.1");

        // When
        String result = executor.execute(
                AuditEntityType.PURCHASE_ORDER,
                200L,
                AuditAction.VOID,
                () -> beforeData,
                () -> "Deleted",
                null,  // No after snapshot
                context
        );

        // Then
        assertThat(result).isEqualTo("Deleted");

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getPreviousData()).isEqualTo("{\"status\":\"PENDING\"}");
        assertThat(saved.getNewData()).isNull();
    }

    @Test
    void shouldUseSYSTEMWhenNoAuthentication() throws Exception {
        // Given
        lenient().when(securityContext.getAuthentication()).thenReturn(null);

        Map<String, Object> beforeData = Map.of("stock", 10);
        Map<String, Object> afterData = Map.of("stock", 20);

        lenient().when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"stock\":10}");
        lenient().when(objectMapper.writeValueAsString(afterData)).thenReturn("{\"stock\":20}");

        AuditContext context = new AuditContext("127.0.0.1");

        // When
        String result = executor.execute(
                AuditEntityType.INVENTORY,
                50L,
                AuditAction.ADJUST,
                () -> beforeData,
                () -> "Adjusted",
                r -> afterData,
                context
        );

        // Then
        assertThat(result).isEqualTo("Adjusted");

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("SYSTEM");
    }

    @Test
    void shouldExecuteOperationForPurchaseOrderReceive() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("warehouse.user");

        Map<String, Object> beforeData = Map.of("status", "PENDING", "receivedQuantity", 0);
        Map<String, Object> afterData = Map.of("status", "RECEIVED", "receivedQuantity", 100);

        when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"status\":\"PENDING\",\"receivedQuantity\":0}");
        when(objectMapper.writeValueAsString(afterData)).thenReturn("{\"status\":\"RECEIVED\",\"receivedQuantity\":100}");

        AuditContext context = new AuditContext("192.168.10.50");

        // When
        String result = executor.execute(
                AuditEntityType.PURCHASE_ORDER,
                300L,
                AuditAction.RECEIVE,
                () -> beforeData,
                () -> "Received",
                r -> afterData,
                context
        );

        // Then
        assertThat(result).isEqualTo("Received");

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.PURCHASE_ORDER);
        assertThat(saved.getAction()).isEqualTo(AuditAction.RECEIVE);
        assertThat(saved.getUsername()).isEqualTo("warehouse.user");
    }

    @Test
    void shouldExecuteOperationForInventoryAdjustment() throws Exception {
        // Given
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("admin");

        Map<String, Object> beforeData = Map.of("currentStock", 50, "minStock", 10);
        Map<String, Object> afterData = Map.of("currentStock", 75, "minStock", 10);

        lenient().when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"currentStock\":50,\"minStock\":10}");
        lenient().when(objectMapper.writeValueAsString(afterData)).thenReturn("{\"currentStock\":75,\"minStock\":10}");

        AuditContext context = new AuditContext("192.168.1.200");

        // When
        String result = executor.execute(
                AuditEntityType.INVENTORY,
                75L,
                AuditAction.ADJUST,
                () -> beforeData,
                () -> "Adjusted",
                r -> afterData,
                context
        );

        // Then
        assertThat(result).isEqualTo("Adjusted");

        ArgumentCaptor<AuditRecordEntity> captor = ArgumentCaptor.forClass(AuditRecordEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditRecordEntity saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.INVENTORY);
        assertThat(saved.getAction()).isEqualTo(AuditAction.ADJUST);
    }

    @Test
    void shouldPropagateOperationException() throws Exception {
        // Given
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user");

        Map<String, Object> beforeData = Map.of("status", "PENDING");
        lenient().when(objectMapper.writeValueAsString(beforeData)).thenReturn("{\"status\":\"PENDING\"}");

        AuditContext context = new AuditContext("192.168.1.1");
        RuntimeException operationError = new RuntimeException("Operation failed");

        // When/Then
        assertThatThrownBy(() -> executor.execute(
                AuditEntityType.SALE,
                1L,
                AuditAction.CONFIRM,
                () -> beforeData,
                () -> {
                    throw operationError;
                },
                r -> Map.of("status", "CONFIRMED"),
                context
        )).isEqualTo(operationError);

        // Audit should not be saved when operation fails
        verify(auditRepository, never()).save(any());
    }

    @Test
    void shouldHandleJsonSerializationException() throws Exception {
        // Given
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user");

        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("circular", beforeData); // Circular reference

        lenient().when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("JSON serialization failed"));

        AuditContext context = new AuditContext("192.168.1.1");

        // When/Then
        assertThatThrownBy(() -> executor.execute(
                AuditEntityType.SALE,
                1L,
                AuditAction.CONFIRM,
                () -> beforeData,
                () -> "Result",
                r -> Map.of("status", "CONFIRMED"),
                context
        )).hasMessageContaining("JSON serialization failed");

        verify(auditRepository, never()).save(any());
    }
}
