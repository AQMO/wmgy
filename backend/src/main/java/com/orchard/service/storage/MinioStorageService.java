package com.orchard.service.storage;

import com.orchard.common.BizException;
import com.orchard.entity.FileRecord;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 对象存储实现（storage.type=minio）。
 * 端点/密钥/桶全部由配置项维护（application.yml 或环境变量），支持迁移变更：
 * 更换 MinIO 服务器只需修改 endpoint，历史文件 URL 由 public-url 域名前缀决定。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    private final MinioClient client;

    @Value("${storage.minio.bucket:orchard}")
    private String bucket;

    @Value("${storage.public-url:}")
    private String publicUrl;

    public MinioStorageService(@Value("${storage.minio.endpoint:http://localhost:9000}") String endpoint,
                               @Value("${storage.minio.access-key:minioadmin}") String accessKey,
                               @Value("${storage.minio.secret-key:minioadmin}") String secretKey) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public FileRecord upload(MultipartFile file, String biz, Long uploaderId) {
        try {
            ensureBucket();
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot).toLowerCase();
            }
            String key = biz.toLowerCase() + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
            try (InputStream in = file.getInputStream()) {
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .stream(in, file.getSize(), -1)
                        .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                        .build());
            }
            FileRecord r = new FileRecord();
            r.setObjectKey(key);
            r.setStorage("MINIO");
            r.setFileName(original);
            r.setContentType(file.getContentType());
            r.setSize(file.getSize());
            r.setBiz(biz);
            r.setUploaderId(uploaderId);
            r.setUrl((publicUrl == null || publicUrl.isEmpty() ? "" : publicUrl) + "/api/files/preview/" + key);
            return r;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BizException("文件上传到 MinIO 失败：" + e.getMessage());
        }
    }

    @Override
    public void delete(FileRecord record) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(record.getObjectKey()).build());
        } catch (Exception e) {
            log.warn("MinIO 对象删除失败: {}", record.getObjectKey(), e);
        }
    }

    /** 读取对象流（本地代理预览用） */
    public InputStream getObject(String key) {
        try {
            return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new BizException("读取文件失败：" + e.getMessage());
        }
    }

    private void ensureBucket() throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
