package com.ai.service;

import com.ai.entity.Session;
import com.ai.util.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * <p>
 * 会话表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
public interface ISessionService extends IService<Session> {

    Result getSession();

    Mono<LocalDateTime> getTimeZone(String ip);
}
