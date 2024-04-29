package com.ai.util;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.ibatis.annotations.Case;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 刘晨
 */

@Component
public class Gpt3Util {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    // 将API_KEY替换成你的API密钥
    private static final String API_KEY = "sk-proj-Mg57a8TpL6nvzHgfRPeeT3BlbkFJyWgzMEGEDW85zpnl3zg6";
    public String chat(List<String> conversationHistory, String model){
        // 准备JSON数据
        String jsonData = String.format("""
                {
                    "model": "%s",
                    "messages": [%s]
                }""", model, String.join(",", conversationHistory));

        // 创建HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // 构建HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(3))
                .build();
        try {
            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user", "你是gpt3.5还是gpt4"));
        new Gpt3Util().chat(list, "gpt-3.5-turbo");
    }
}
