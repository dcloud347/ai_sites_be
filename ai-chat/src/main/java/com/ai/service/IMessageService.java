package com.ai.service;

import com.ai.dto.ChatDto;
import com.ai.entity.Message;
import com.ai.util.Result;
import com.ai.vo.ChatVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 消息表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
public interface IMessageService extends IService<Message> {

    Result<ChatVo> chat(ChatDto chatDto);
}
