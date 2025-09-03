package com.pd.pdaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pd.pdaicodemother.ai.constant.UserConstant;
import com.pd.pdaicodemother.exception.ErrorCode;
import com.pd.pdaicodemother.exception.ThrowUtils;
import com.pd.pdaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.pd.pdaicodemother.model.entity.App;
import com.pd.pdaicodemother.model.entity.ChatHistory;
import com.pd.pdaicodemother.mapper.ChatHistoryMapper;
import com.pd.pdaicodemother.model.entity.User;
import com.pd.pdaicodemother.model.enums.MessageTypeEnum;
import com.pd.pdaicodemother.service.AppService;
import com.pd.pdaicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/Tiangero">程序员皮蛋</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;


    /**
     * 加载对话历史到内存
     *
     * @param appId
     * @param chatMemory
     * @param maxCount
     * @return
     */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 查询对话历史
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    // 最新消息会存入到 Memory 中，需要避免
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) return 0;
            int loadedCount = 0;
            // 清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList.reversed()) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                } else {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                }
                loadedCount++;
            }
            log.info("成功为 appid : {} 加载了 {} 条历史记录到 Memory 中", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载对话历史失败,appId {} , error : {}", appId, e.getMessage());
            return 0;
        }
    }

    /**
     * 分页获取用户对话列表
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        // 验证权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean self = app.getUserId().equals(loginUser.getId());
        boolean admin = loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE);
        ThrowUtils.throwIf(!self && !admin, ErrorCode.NO_AUTH_ERROR, "无访问权限");
        // 构造条件
        ChatHistoryQueryRequest queryRequest = ChatHistoryQueryRequest.builder()
                .appId(appId)
                .lastCreateTime(lastCreateTime)
                .build();
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        // 查询数据
        return page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 添加用户对话
     *
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ChatHistory userHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(userHistory);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id, id != null)
                .like("message", message, !StrUtil.isBlank(message))
                .eq("messageType", messageType, !StrUtil.isBlank(messageType))
                .eq("appId", appId, appId != null)
                .eq("userId", userId, userId != null);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        queryWrapper.lt("createTime", lastCreateTime, lastCreateTime != null);
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

}
