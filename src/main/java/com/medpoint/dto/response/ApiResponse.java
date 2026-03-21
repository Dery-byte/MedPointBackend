package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;

/** Generic envelope for simple success/failure responses. */
@Data @Builder
public class ApiResponse<D> {
    private boolean success;
    private String message;
    private D data;
    public static <D> ApiResponse<D> ok(String message) {
        return ApiResponse.<D>builder().success(true).message(message).build();
    }


    public static <D> ApiResponse<D> ok(String message, D data) {
        return ApiResponse.<D>builder().success(true).message(message).data(data).build();
    }

    public static <D> ApiResponse<D> error(String message) {
        return ApiResponse.<D>builder().success(false).message(message).build();
    }
}
