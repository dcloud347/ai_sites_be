package com.ai.controller;

import com.ai.service.IpApiService;
import com.ai.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 通用业务控制器
 * @author 刘晨
 */
@RestController
@RequestMapping("api/common")
public class CommonController {

    @Resource
    private IpApiService ipApiService;
    @GetMapping("/time")
    public Mono<String> getTime(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        System.out.println(ip);
        return ipApiService.getTimeZone(ip)
                .map(timeZone -> {
                    ZonedDateTime now = ZonedDateTime.now(java.time.ZoneId.of(timeZone));
                    return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
                })
                .onErrorReturn("Error retrieving time for IP: " + ip);
    }
}
