package com.pd.pdaicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 * @version 2025/9/2 12:13
 */
public interface ProjectDownloadService{
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
