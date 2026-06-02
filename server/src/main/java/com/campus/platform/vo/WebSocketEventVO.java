package com.campus.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketEventVO<T> {

    private String event;
    private T data;
}
