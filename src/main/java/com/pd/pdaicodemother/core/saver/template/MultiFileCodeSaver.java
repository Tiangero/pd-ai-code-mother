package com.pd.pdaicodemother.core.saver.template;

import cn.hutool.core.util.StrUtil;
import com.pd.pdaicodemother.ai.model.MultiFileCodeResult;
import com.pd.pdaicodemother.core.saver.CodeFileSaverTemplate;
import com.pd.pdaicodemother.exception.BusinessException;
import com.pd.pdaicodemother.exception.ErrorCode;
import com.pd.pdaicodemother.model.enums.CodeGenTypeEnum;

public class MultiFileCodeSaver extends CodeFileSaverTemplate<MultiFileCodeResult> {


    @Override
    protected CodeGenTypeEnum getCodeGenTypeEnum() {
        return CodeGenTypeEnum.MULTI_FILE;

    }

    @Override
    protected void validInput(MultiFileCodeResult result) {
        super.validInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
        if (StrUtil.isBlank(result.getCssCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "CSS代码不能为空");
        }
        if (StrUtil.isBlank(result.getJsCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "JS代码不能为空");
        }
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
    }
}
