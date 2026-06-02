package com.campus.platform.service;

import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.vo.FileUploadVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileAssetMapper fileAssetMapper;
    private final AppProperties appProperties;

    @Transactional
    public FileUploadVO uploadImage(MultipartFile file, Long userId, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        try {
            Path directory = Paths.get(appProperties.getFile().getStoragePath());
            Files.createDirectories(directory);
            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
            }
            String storedName = UUID.randomUUID() + extension;
            Path target = directory.resolve(storedName);
            file.transferTo(target);

            FileAsset asset = new FileAsset();
            asset.setOriginalName(originalName == null ? storedName : originalName);
            asset.setStoredName(storedName);
            asset.setRelativePath(storedName);
            asset.setContentType(file.getContentType());
            asset.setBizType(bizType);
            asset.setUploaderId(userId);
            fileAssetMapper.insert(asset);

            return new FileUploadVO(asset.getId(), appProperties.getFile().getPublicBaseUrl() + storedName, asset.getOriginalName());
        } catch (IOException ex) {
            throw new BusinessException(500, "文件上传失败");
        }
    }
}

