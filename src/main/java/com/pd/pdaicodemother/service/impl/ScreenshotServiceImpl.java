package com.pd.pdaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pd.pdaicodemother.exception.ErrorCode;
import com.pd.pdaicodemother.exception.ThrowUtils;
import com.pd.pdaicodemother.manager.MinioManager;
import com.pd.pdaicodemother.service.ScreenshotService;
import com.pd.pdaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private MinioManager minioManager;


    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "webUrl不能为空");
        // 1. 生成截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.SYSTEM_ERROR, "本地截图生成失败");
        try {
            // 2. 上传截图
            String screenshotUrl = uploadScreenshotToMinio(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(screenshotUrl), ErrorCode.SYSTEM_ERROR, "截图上传失败");
            return screenshotUrl;
        } finally {
            cleanUpLocalFile(localScreenshotPath);
        }
    }

    private String uploadScreenshotToMinio(String localScreenshotPath) {
        File screenshotFile = new File(localScreenshotPath);
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String key = generateScreenshotKey(fileName);
        return minioManager.picUpload(key, screenshotFile);
    }

    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    private void cleanUpLocalFile(String localScreenshotPath) {
        File localFile = new File(localScreenshotPath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("截图上传成功，清理本地文件: {}", localScreenshotPath);
        }
    }

}