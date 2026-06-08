package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jobs_account")
public class JobsAccount extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String role;
    private String phone;
    private String status;
    private Integer publishEnabled;
    private Integer applyEnabled;
    private Integer acceptEnabled;
    private Integer reportCount;
    private Integer disputeCount;
    private Integer creditScore;
    private String latestRemark;
}
