package com.orchard.service.storage;

import com.orchard.common.BizException;
import com.orchard.entity.FileRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地盘存储实现（storage.type=local，默认）。
 * 开发/演示环境无需 MinIO 即可使用；文件保存在 storage.local-dir，经 /api/files/preview/{key} 访问。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${storage.local-dir:./uploads}")
    private String localDir;

    @Value("${storage.public-url:}")
    private String publicUrl;

    @Override
    public FileRecord upload(MultipartFile file, String biz, Long uploaderId) {
        try {
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot).toLowerCase();
            }
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String key = dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path dir = Paths.get(localDir, dateDir);
            Files.createDirectories(dir);
            Path target = Paths.get(localDir, key);
            try (java.io.InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            FileRecord r = new FileRecord();
            r.setObjectKey(key);
            r.setStorage("LOCAL");
            r.setFileName(original);
            r.setContentType(file.getContentType());
            r.setSize(file.getSize());
            r.setBiz(biz);
            r.setUploaderId(uploaderId);
            r.setUrl((publicUrl == null || publicUrl.isEmpty() ? "" : publicUrl) + "/api/files/preview/" + key);
            return r;
        } catch (IOException e) {
            log.error("本地文件保存失败", e);
            throw new BizException("文件保存失败：" + e.getMessage());
        }
    }

    @Override
    public void delete(FileRecord record) {
        try {
            Files.deleteIfExists(Paths.get(localDir, record.getObjectKey()));
        } catch (IOException e) {
            log.warn("本地文件删除失败: {}", record.getObjectKey(), e);
        }
    }
}
