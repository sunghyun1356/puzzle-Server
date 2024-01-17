package com.PuzzleU.Server.common;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiResponseDto<T> {

    private boolean success;
    private T response;
    private ErrorResponse error;
    private String jwt;

    @Builder
    ApiResponseDto(boolean success, T response, com.PuzzleU.Server.common.ErrorResponse error, String jwt)
    {
        this.success = success;
        this.response = response;
        this.error = error;
        this.jwt =jwt;
    }
}
