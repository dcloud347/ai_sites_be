package com.ai.service.impl;

import com.ai.entity.Session;
import com.ai.mapper.SessionMapper;
import com.ai.service.ISessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements ISessionService {

}
