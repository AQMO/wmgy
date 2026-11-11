package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.FileRecord;
import com.orchard.repository.FileRecordRepository;
import com.orchard.service.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/** 文件统一入口：校验 → 存储（MinIO/本地） → 登记文件记录表 */
@Service
public class FileService {

    private static final List<String> ALLOWED_EXT = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".pdf", ".mp4", ".mp3");
    private static final long MAX_SIZE = 20L * 1024 * 1024;   // 20MB

    private final StorageService storageService;
    private final FileRecordRepository fileRecordRepository;

    public FileService(StorageService storageService, FileRecordRepository fileRecordRepository) {
        this.storageService = storageService;
        this.fileRecordRepository = fileRecordRepository;
    }

    public FileRecord upload(MultipartFile file, String biz, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("文件大小不能超过 20MB");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean ok = ALLOWED_EXT.stream().anyMatch(name::endsWith);
        if (!ok) {
            throw new BizException("不支持的文件类型，仅允许图片/PDF/音视频");
        }
        if (biz == null || biz.isEmpty()) {
            biz = "COMMON";
        }
        FileRecord r = storageService.upload(file, biz, uploaderId);
        return fileRecordRepository.save(r);
    }

    public FileRecord get(Long id) {
        return fileRecordRepository.findById(id).orElseThrow(() -> new BizException(40004, "文件不存在"));
    }

    /** 文件分页（维护用） */
    public Page<FileRecord> page(int page, int size) {
        return fileRecordRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    public FileRecord findByKey(String key) {
        return fileRecordRepository.findAll().stream()
                .filter(r -> r.getObjectKey().equals(key))
                .findFirst().orElse(null);
    }

    /** 删除文件：校验归属（本人或管理员）后删对象 + 删记录 */
    public void delete(Long id, Long uid, String role) {
        FileRecord r = get(id);
        boolean admin = "ADMIN".equals(role);
        boolean owner = uid != null && uid.equals(r.getUploaderId());
        if (!admin && !owner) {
            throw new BizException(BizException.FORBIDDEN, "无权删除该文件");
        }
        storageService.delete(r);
        fileRecordRepository.delete(r);
    }
}
