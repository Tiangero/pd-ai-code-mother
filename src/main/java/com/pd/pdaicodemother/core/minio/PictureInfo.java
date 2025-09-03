package com.pd.pdaicodemother.core.minio;

import lombok.Data;

/**
 * 文件信息
 */
@Data
public class PictureInfo {

    private String objectName;
    private long size; // bytes
    private double sizeMB; // MB
    private String format;
    private Integer width;
    private Integer height;
    private String contentType;
}
