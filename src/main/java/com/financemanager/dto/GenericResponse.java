package com.financemanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> GenericResponse<T> success(T data, String message) {
        return GenericResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> GenericResponse<T> success(String message) {
        return GenericResponse.<T>builder().success(true).message(message).build();
    }

    public static <T> GenericResponse<T> error(String message) {
        return GenericResponse.<T>builder().success(false).message(message).build();
    }
}
