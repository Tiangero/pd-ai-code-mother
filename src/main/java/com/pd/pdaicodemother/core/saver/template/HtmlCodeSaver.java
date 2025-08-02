package com.pd.pdaicodemother.core.saver.template;

import cn.hutool.core.util.StrUtil;
import com.pd.pdaicodemother.ai.model.HtmlCodeResult;
import com.pd.pdaicodemother.core.saver.CodeFileSaverTemplate;
import com.pd.pdaicodemother.exception.BusinessException;
import com.pd.pdaicodemother.exception.ErrorCode;
import com.pd.pdaicodemother.model.enums.CodeGenTypeEnum;

public class HtmlCodeSaver extends CodeFileSaverTemplate<HtmlCodeResult> {


    @Override
    protected CodeGenTypeEnum getCodeGenTypeEnum() {
        return CodeGenTypeEnum.HTML;

    }

    @Override
    protected void validInput(HtmlCodeResult result) {
        super.validInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }
}
