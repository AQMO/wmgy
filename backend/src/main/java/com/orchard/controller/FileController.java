package com.orchard.controller;

import com.orchard.common.Result;
import com.orchard.entity.FileRecord;
import com.orchard.service.FileService;
import com.orchard.service.storage.MinioStorageService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/** 文件上传/预览/维护（MinIO 或本地盘，由 storage.type 配置切换） */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final ObjectProvider<MinioStorageService> minioProvider;

    @Value("${storage.local-dir:./uploads}")
    private String localDir;

    public FileController(FileService fileService, ObjectProvider<MinioStorageService> minioProvider) {
        this.fileService = fileService;
        this.minioProvider = minioProvider;
    }

    /** 上传：biz 标识业务归属（PRODUCT/CHAT/TRACE/AVATAR） */
    @PostMapping("/upload")
    public Result<FileRecord> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "biz", required = false) String biz,
                                     @RequestAttribute("uid") Long uid) {
        return Result.ok(fileService.upload(file, biz, uid));
    }

    /** 文件列表（维护用） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.ok(fileService.page(page, size));
    }

    /** 删除文件：上传人或管理员 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestAttribute("uid") Long uid,
                            @RequestAttribute("role") String role) {
        fileService.delete(id, uid, role);
        return Result.ok(null);
    }

    /**
     * 文件预览代理：本地盘直接读，MinIO 通过 SDK 流式转发。
     * URL 形如 /api/files/preview/{key}，前端 <img> 可直接引用（无需带 Token）。
     */
    @GetMapping("/preview/{key:.+}")
    public ResponseEntity<InputStreamResource> preview(@PathVariable String key) throws Exception {
        String decoded = key.replace("..", "");
        FileRecord r = fileService.findByKey(decoded);
        String contentType = r != null && r.getContentType() != null ? r.getContentType() : "application/octet-stream";
        InputStream in;
        MinioStorageService minio = minioProvider.getIfAvailable();
        if (r != null && "MINIO".equals(r.getStorage()) && minio != null) {
            in = minio.getObject(decoded);
        } else {
            java.nio.file.Path p = Paths.get(localDir, decoded);
            if (!Files.exists(p)) {
                return ResponseEntity.notFound().build();
            }
            in = Files.newInputStream(p);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(in));
    }
}
