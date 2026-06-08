package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jobs_skill_tag")
public class JobsSkillTag extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String label;
    private String category;
    private Integer enabled;
    private Integer usageCount;
}
