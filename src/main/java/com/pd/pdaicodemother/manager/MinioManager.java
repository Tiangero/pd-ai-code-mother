package com.pd.pdaicodemother.manager;

import com.pd.pdaicodemother.config.MinioConfig;
import com.pd.pdaicodemother.core.minio.PictureInfo;
import com.pd.pdaicodemother.exception.BusinessException;
import com.pd.pdaicodemother.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioManager {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfig minioConfig;

    private static final String MINIO_BUCKET = "image-service";

    /**
     * 图片上传并返回访问URL
     *
     * @param pictureName
     * @param file
     */
    public String picUpload(String pictureName, File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            String contentType = detectContentType(pictureName);
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(MINIO_BUCKET)
                            .object(pictureName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );
            return getPublicUrl(pictureName);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return null;
        }
    }


    /**
     * 获取图片公开访问URL
     *
     * @param pictureName
     * @return
     */
    public String getPublicUrl(String pictureName) {
        return String.format("%s/%s/%s", minioConfig.getEndpoint(), MINIO_BUCKET, pictureName);
    }

    /**
     * 获取图片预签名URL
     *
     * @param pictureName
     * @return
     */
    public String getPreSignedUrl(String pictureName) {
        try {
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(MINIO_BUCKET)
                            .object(pictureName)
                            .expiry(5, TimeUnit.MINUTES)
                            .build()
            );
            return presignedUrl;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成预签名URL失败： " + e.getMessage());
        }
    }


    /**
     * 获取图片信息
     *
     * @param pictureName 对象名称
     * @return 文件信息
     */
    public PictureInfo getFileInfo(String pictureName) {
        try {
            // 获取对象统计信息
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(MINIO_BUCKET)
                            .object(pictureName)
                            .build()
            );
            PictureInfo pictureInfo = new PictureInfo();
            // 设置基本属性
            pictureInfo.setObjectName(pictureName);
            pictureInfo.setSize(stat.size());
            pictureInfo.setSizeMB(stat.size() / (1024.0 * 1024.0)); // 转换为MB
            pictureInfo.setContentType(stat.contentType());
            // 尝试解析文件格式
            String format = parseFormatFromContentType(stat.contentType());
            if (format == null) {
                format = parseFormatFromFileName(pictureName);
            }
            pictureInfo.setFormat(format);
            // 如果是图片文件，获取宽高
            if (isImageFile(stat.contentType())) {
                getImageDimensions(pictureInfo);
            }
            return pictureInfo;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取文件信息失败：" + e.getMessage());
        }
    }


    /**
     * 根据文件扩展名推断内容类型
     *
     * @param pictureName
     * @return
     */
    private String detectContentType(String pictureName) {
        if (pictureName == null) return "application/octet-stream";

        String lowerName = pictureName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg"; // 默认为JPEG
        }
    }

    /**
     * 从内容类型解析文件格式
     */
    private String parseFormatFromContentType(String contentType) {
        if (contentType == null) return null;
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
                return "JPEG";
            case "image/png":
                return "PNG";
            case "image/gif":
                return "GIF";
            case "image/bmp":
                return "BMP";
            case "image/webp":
                return "WEBP";
            case "image/svg+xml":
                return "SVG";
            case "application/pdf":
                return "PDF";
            default:
                return null;
        }
    }

    /**
     * 从文件名解析文件格式
     */
    private String parseFormatFromFileName(String pictureName) {
        if (pictureName == null) return null;
        int lastDotIndex = pictureName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < pictureName.length() - 1) {
            return pictureName.substring(lastDotIndex + 1).toUpperCase();
        }
        return null;
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/");
    }

    /**
     * 获取图片尺寸
     */
    private void getImageDimensions(PictureInfo pictureInfo) {
        try {
            // 注意：这里为了获取图片尺寸需要下载整个文件，对于大文件可能影响性能
            // 在生产环境中建议考虑其他优化方案
            InputStream inputStream = minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(MINIO_BUCKET)
                            .object(pictureInfo.getObjectName())
                            .build()
            );

            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                pictureInfo.setWidth(image.getWidth());
                pictureInfo.setHeight(image.getHeight());
            }
            inputStream.close();
        } catch (Exception e) {
            // 如果无法读取图片尺寸，保持width和height为null
            System.err.println("无法读取图片尺寸: " + e.getMessage());
        }
    }
}
