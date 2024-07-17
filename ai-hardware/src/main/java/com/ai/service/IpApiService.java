package com.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class IpApiService {

    private final WebClient webClient;

    public IpApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://ip-api.com").build();
    }

    public Mono<String> getTimeZone(String ip) {
        return this.webClient.get()
                .uri("/json/{ip}", ip)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    // 解析返回的JSON，获取时区信息
                    return extractTimeZoneFromResponse(response);
                });
    }

    private String extractTimeZoneFromResponse(String response) {
        System.out.println(response);
        // 简单的JSON解析，可以用更强大的JSON库来解析
        String timeZone = response.split("\"timezone\":\"")[1].split("\"")[0];
        return timeZone;
    }
}

