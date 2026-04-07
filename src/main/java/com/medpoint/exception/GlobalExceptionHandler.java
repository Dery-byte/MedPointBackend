package com.medpoint.exception;
import com.medpoint.dto.paystackdto.PaymentResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.stream.Collectors;

/**
 * All error responses use the same envelope the frontend expects:
 *   { success: false, message: "...", data: null }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Data @Builder
    static class ErrorBody {
        private boolean success;
        private String message;
        private Object data;
    }

    private ResponseEntity<ErrorBody> err(HttpStatus status, String msg) {
        return ResponseEntity.status(status)
                .body(ErrorBody.builder().success(false).message(msg).data(null).build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBody> handleNotFound(ResourceNotFoundException ex) {
        return err(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorBody> handleBusiness(BusinessException ex) {
        return err(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorBody> handleBadCredentials(BadCredentialsException ex) {
        return err(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorBody> handleDisabled(DisabledException ex) {
        return err(HttpStatus.UNAUTHORIZED, "Account is disabled. Contact your administrator.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBody> handleAccessDenied(AccessDeniedException ex) {
        return err(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
//        String msg = ex.getBindingResult().getFieldErrors().stream()
//                .map(e -> e.getField() + ": " + e.getDefaultMessage())
//                .findFirst().orElse("Validation failed");
//        return err(HttpStatus.BAD_REQUEST, msg);
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorBody> handleGeneral(Exception ex) {
//        return err(HttpStatus.INTERNAL_SERVER_ERROR,
//                "An unexpected error occurred: " + ex.getMessage());
//    }




    @ExceptionHandler(TransactionAlreadyCancelledException.class)
    public ResponseEntity<ErrorBody> transactionAlreadyCacelled(Exception ex) {
        return err(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }

















    /** Bean validation failures (e.g. missing email, invalid amount) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(PaymentResponse.error("Validation error: " + errors));
    }

    /** Unknown transaction reference */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(PaymentResponse.error(ex.getMessage()));
    }

    /** Paystack API returned a 4xx/5xx */
    /** Paystack API returned a 4xx/5xx */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<PaymentResponse> handleWebClientError(HttpClientErrorException ex) {
        log.error("Paystack API error | status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(PaymentResponse.error("Paystack API error: " + ex.getMessage()));
    }
    /** Catch-all */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentResponse.error("An unexpected error occurred. Please try again."));
    }
}
