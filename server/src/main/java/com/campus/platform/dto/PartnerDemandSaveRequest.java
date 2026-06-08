package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class PartnerDemandSaveRequest {

    @NotBlank
    @Size(max = 40)
    private String type;

    @NotBlank
    @Size(max = 120)
    private String schedule;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotBlank
    @Size(max = 1000)
    private String description;

    private List<String> needTags;
}
