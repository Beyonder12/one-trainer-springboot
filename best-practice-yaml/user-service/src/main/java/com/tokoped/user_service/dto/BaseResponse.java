package com.tokoped.user_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BaseResponse<T>{
    private String code;
    private String message;
    private T data;
}
