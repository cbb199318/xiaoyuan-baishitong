package com.campus.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadVO {

    private Long fileId;
    private String url;
    private String originalName;
}

