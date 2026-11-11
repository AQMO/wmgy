package com.orchard.service.storage;

import com.orchard.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

/** 存储抽象：MinIO / 本地盘两种实现，由配置 storage.type 切换，业务层无感知 */
public interface StorageService {

    /**
     * 上传文件
     * @param file      文件
     * @param biz       业务标识（PRODUCT/CHAT/TRACE/AVATAR）
     * @param uploaderId 上传人
     * @return 文件记录（含访问 URL）
     */
    FileRecord upload(MultipartFile file, String biz, Long uploaderId);

    /** 删除文件（同时删除存储端对象） */
    void delete(FileRecord record);
}
