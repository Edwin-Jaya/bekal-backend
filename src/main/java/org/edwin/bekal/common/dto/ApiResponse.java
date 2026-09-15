package org.edwin.bekal.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timestamp;

    private Integer status;

    private Boolean success;

    private String message;

    private T data;

    private Object errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(Integer status, String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(Integer status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(Integer status, String message, Object errors) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
