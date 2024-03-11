package com.ai.config;

import com.ai.enums.RedisPrefixEnum;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author 刘晨
 */

public class MyEndpointConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        StringRedisTemplate stringRedisTemplate = SpringContextHelper.getBean(StringRedisTemplate.class);
        // 从请求中获取JWT token，例如从查询参数或者头部信息
        String token;
        if (request.getHeaders().get("token") != null){
            token = request.getHeaders().get("token").get(0);
        }else {
            token = request.getParameterMap().get("token").get(0);
        }
        String user_id = stringRedisTemplate.opsForValue().get(RedisPrefixEnum.USER_TOKEN.getPrefix() + token);
        sec.getUserProperties().put("userId", user_id);
    }
}
