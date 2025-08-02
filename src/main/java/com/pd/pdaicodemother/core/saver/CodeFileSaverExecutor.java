package com.pd.pdaicodemother.core.saver;

import com.pd.pdaicodemother.ai.model.HtmlCodeResult;
import com.pd.pdaicodemother.ai.model.MultiFileCodeResult;
import com.pd.pdaicodemother.core.saver.template.HtmlCodeSaver;
import com.pd.pdaicodemother.core.saver.template.MultiFileCodeSaver;
import com.pd.pdaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaverExecutor {

    private static final HtmlCodeSaver HTML_CODE_SAVER = new HtmlCodeSaver();
    private static final MultiFileCodeSaver MULTI_FILE_CODE_SAVER = new MultiFileCodeSaver();

    public static File saveCode(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_SAVER.saveCode((HtmlCodeResult) result, appId);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVER.saveCode((MultiFileCodeResult) result, appId);
            default -> throw new RuntimeException("不支持的代码生成类型" + codeGenTypeEnum);
        };
    }
}
