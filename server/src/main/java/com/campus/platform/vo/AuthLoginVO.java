package com.campus.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthLoginVO {

    private String token;
    private UserProfileVO user;
}

