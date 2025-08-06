package com.pd.pdaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.pd.pdaicodemother.model.dto.app.AppQueryRequest;
import com.pd.pdaicodemother.model.entity.App;
import com.pd.pdaicodemother.model.entity.User;
import com.pd.pdaicodemother.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/Tiangero">程序员皮蛋</a>
 */
public interface AppService extends IService<App> {

    /**
     * 聊天生成代码
     *
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 应用部署
     *
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);
}