package com.ai.util;

import com.ai.config.OpenAiConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘晨
 */

@Component
public class Gpt3Util {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    // 将API_KEY替换成你的API密钥
    private static final String API_KEY = new OpenAiConfig().getApiKey();

    private  WebClient webClient =  WebClient.builder()
            .baseUrl("https://api.openai.com/")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
            .build();
    public String chat(List<String> conversationHistory, String model){
        // 准备JSON数据
        String jsonData = String.format("""
                {
                    "model": "%s",
                    "messages": [%s]
                }""", model, String.join(",", conversationHistory));
        System.out.println(jsonData);
        // 创建HttpClient
        HttpClient client = HttpClient.newHttpClient();
        // 构建HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
//                .timeout(Duration.ofSeconds(3))
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

    public String uploadFile(MultipartFile file) throws IOException {
        // 构建多部分请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("purpose", "fine-tune");
        // 发送请求
        String response = webClient.post().uri("v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null) {
            throw new IOException("Failed to upload file, no response from server.");
        }

        return response;
    }


    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user", "你是gpt3.5还是gpt4"));
//        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "assistant", "我是GPT-3，尚未更新至GPT-4版本。如有其他问题，我将竭诚为您提供帮助。"));
//        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user", "那你有新版本了吗"));

    }
}
