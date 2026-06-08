package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank
    @Size(max = 64)
    private String nickname;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 16)
    private String gender;

    @Size(max = 64)
    private String contactPhone;

    @Size(max = 255)
    private String bio;

    @Size(max = 20)
    private String publishRole;
}
