package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_asset")
public class FileAsset extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String storedName;
    private String relativePath;
    private String contentType;
    private String bizType;
    private Long uploaderId;
}

