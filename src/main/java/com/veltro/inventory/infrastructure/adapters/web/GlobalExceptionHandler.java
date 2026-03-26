package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.exception.ErrorResponse;
import com.veltro.inventory.exception.InsufficientStockException;
import com.veltro.inventory.exception.InvalidPaymentException;
import com.veltro.inventory.exception.InvalidPriceException;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import com.veltro.inventory.exception.NotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Central error handler for all REST controllers.
 *
 * Maps domain exceptions to structured HTTP error responses using {@link ErrorResponse}.
 * Every handler logs the exception at the appropriate level before responding.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 409 Conflict — optimistic locking / concurrency conflicts (ADR-002)
    // -------------------------------------------------------------------------

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("Optimistic lock conflict on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        "CONCURRENCY_CONFLICT",
                        "The resource was modified by another operation. Please verify availability and retry.",
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Content — domain rule violations
    // (HttpStatus.UNPROCESSABLE_ENTITY is deprecated in Spring 6+; use UNPROCESSABLE_CONTENT)
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransition(
            InvalidStateTransitionException ex, HttpServletRequest request) {

        log.warn("Invalid state transition on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(
                        "INVALID_STATE_TRANSITION",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        log.warn("Insufficient stock on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(
                        "INSUFFICIENT_STOCK",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Content — invalid price constraint (B1-03)
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPrice(
            InvalidPriceException ex, HttpServletRequest request) {

        log.warn("Invalid price on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(
                        "INVALID_PRICE",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Content — invalid payment (B2-01)
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayment(
            InvalidPaymentException ex, HttpServletRequest request) {

        log.warn("Invalid payment on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(
                        "INVALID_PAYMENT",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 404 Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest request) {

        log.info("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "NOT_FOUND",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request — unreadable / malformed request body
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.debug("Unreadable request body on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "MALFORMED_REQUEST",
                        "Request body is missing or malformed.",
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request — bean validation failures
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.debug("Validation failed on {}: {}", request.getRequestURI(), details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR",
                        details,
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 403 Forbidden — authorization denied by @PreAuthorize / method security
    // -------------------------------------------------------------------------

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex, HttpServletRequest request) {

        log.warn("Authorization denied on {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        "ACCESS_DENIED",
                        "You do not have permission to perform this action.",
                        request.getRequestURI()));
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error — catch-all (last resort)
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please contact support.",
                        request.getRequestURI()));
    }
}
