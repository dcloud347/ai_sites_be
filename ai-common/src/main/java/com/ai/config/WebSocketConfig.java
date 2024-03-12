package com.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * @author 刘晨
 */
@Configuration
@EnableWebSocketMessageBroker  // 启用WebSocket消息处理，由消息代理支持
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个WebSocket端点，客户端将使用它连接到我们的WebSocket服务器。
        registry.addEndpoint("/ws").withSockJS();
        // .withSockJS()是为了兼容不支持WebSocket的浏览器，使用SockJS作为备选方案。
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置一个简单的消息代理，它接收消息的应用程序前缀和消息代理的目的地前缀。
        registry.enableSimpleBroker("/topic");
        // 指定客户端发送消息的路径的前缀，例如，"/app/chat.sendMessage"
        registry.setApplicationDestinationPrefixes("/chat");
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
