package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsSkillTagVO {

    private Long id;
    private String label;
    private String category;
    private Boolean enabled;
    private Integer usageCount;
    private LocalDateTime updatedAt;
}
