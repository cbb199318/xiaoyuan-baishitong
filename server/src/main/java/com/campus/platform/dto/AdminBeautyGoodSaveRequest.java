package com.campus.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AdminBeautyGoodSaveRequest {

    @NotBlank
    @Size(max = 30)
    private String category;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotBlank
    @Size(max = 500)
    private String summary;

    @Size(max = 1000)
    private String usageGuide;

    @Size(max = 255)
    private String sceneText;

    @Size(max = 2000)
    private String evaluation;

    @Size(max = 2000)
    private String dormExperience;

    @Size(max = 2000)
    private String avoidanceGuide;

    @NotEmpty
    private List<@Size(max = 20) String> skinTags;

    @NotNull
    private Long productImageFileId;

    @NotNull
    private Long receiptFileId;
}
