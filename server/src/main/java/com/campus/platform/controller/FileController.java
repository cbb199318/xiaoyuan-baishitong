package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.FileService;
import com.campus.platform.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/image")
    public ApiResponse<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "bizType", defaultValue = "common") String bizType) {
        return ApiResponse.success(fileService.uploadImage(file, SecurityUtils.getCurrentUserId(), bizType));
    }
}

