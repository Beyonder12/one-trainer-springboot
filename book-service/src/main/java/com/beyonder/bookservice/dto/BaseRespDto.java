package com.beyonder.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseRespDto<T> {
    private String code = "2000";
    private String message = "SUCCESS_BASIC";
    private String status = "SUCCESS";
    private String source = "BUNGA_SERVICE";
    private T data;

    public BaseRespDto(T data) {
        this.data = data;
    }
}
