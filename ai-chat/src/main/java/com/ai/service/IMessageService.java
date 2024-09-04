package com.ai.service;

import com.ai.dto.ChatDto;
import com.ai.dto.MessageDto;
import com.ai.entity.Message;
import com.ai.model.LoginEntity;
import com.ai.util.Result;
import com.ai.vo.ChatApiVo;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatResponse;
import com.ai.vo.ChatVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>
 * 消息表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
public interface IMessageService extends IService<Message> {

    ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto, HttpServletRequest request);

    Result<Map<String,Object>> addMessage(MessageDto messageDto, HttpServletRequest request);

    List<ChatRecordVo> record(String id);

    ChatApiVo getChatApiVo(ChatDto chatDto, LoginEntity loginEntity);

    ChatVo afterChat(ChatDto chatDto,ChatApiVo chatApiVo, ChatResponse chatResponse,
                     LoginEntity loginEntity, HttpServletRequest request);

    Mono<String> getTimeZone(String ip);
}
