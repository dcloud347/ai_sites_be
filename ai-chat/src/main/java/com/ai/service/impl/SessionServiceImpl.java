package com.ai.service.impl;

import com.ai.entity.Session;
import com.ai.mapper.SessionMapper;
import com.ai.service.ISessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ai.exceptions.CustomException;
import reactor.util.retry.Retry;

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
    private final WebClient webClient;

    public SessionServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://ip-api.com").build();
    }

    @Override
    public Mono<LocalDateTime> getTimeZone(String ip) throws CustomException{
        // 解析返回的JSON，获取时区信息
        // 解析返回的JSON，获取时区信息
        return this.webClient.get()
                .uri("/json/{ip}", ip)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .map(this::extractTimeZoneFromResponse)
                .onErrorResume(throwable -> {
                    // Handle the error here
                    return Mono.empty();
                });
    }



    private LocalDateTime extractTimeZoneFromResponse(String response) throws CustomException{
        // JSON解析
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if(jsonObject.get("status").getAsString().equals("fail")){
            throw new CustomException("Local Time Parse Error");
        }
        String timeZone = jsonObject.get("timezone").getAsString();
        TimeZone time = TimeZone.getTimeZone(timeZone);
        //格式
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 设置时区
        TimeZone.setDefault(time);
        // 获取实例
        Calendar calendar = Calendar.getInstance();
        //获取Date对象
        Date date = calendar.getTime();
        System.out.println(format.format(date));
        return LocalDateTime.parse(format.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
