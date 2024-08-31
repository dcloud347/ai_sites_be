package com.ai.util;

import com.ai.config.OpenAiConfig;
import com.ai.vo.ChatApiVo;
import com.ai.vo.ParametersApiVo;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author 刘晨
 */

@Component
public class Gpt3Util {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    // 将API_KEY替换成你的API密钥
    private static final String API_KEY = new OpenAiConfig().getApiKey();

    public String chat(ChatApiVo chatApiVo){
        // 创建HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getChatRequest(chatApiVo);
        try {
            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addUtils(ChatApiVo chatApiVo){
        ParametersApiVo parametersApiVo = new ParametersApiVo();
        parametersApiVo.addProperty("words","string","The searching words");
        parametersApiVo.addProperty("num","integer","The number of results to return, this number must be in the range 1 to 10");
        parametersApiVo.addProperty("start","integer","The number of result to start with");
        parametersApiVo.addRequired("words");
        parametersApiVo.setAdditionalProperties(false);
        chatApiVo.addTool("googleSearch","Search on Google", parametersApiVo);
    }


    public HttpRequest getChatRequest(ChatApiVo chatApiVo){
        // 准备JSON数据
        Gson gson = new Gson();
        String jsonData = gson.toJson(chatApiVo);
        // 构建HttpRequest
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
//                .timeout(Duration.ofSeconds(3))
                .build();
    }
    public void streamChat(ChatApiVo chatApiVo){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getChatRequest(chatApiVo);


        // 异步发送请求，并处理响应流
        CompletableFuture<Void> responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body().forEach(line -> {
                        System.out.println("Received: " + line);
                        // 这里可以处理接收到的每一行流数据
                    });
                });

        // 等待完成
        responseFuture.join();
    }

    public static void main(String[] args) throws Exception{
        Gpt3Util gpt3Util = new Gpt3Util();
        ChatApiVo chatApiVo = new ChatApiVo();
        chatApiVo.addTextMessage("今天有什么新闻","user");
        chatApiVo.setModel("gpt-4o");
        Gpt3Util.addUtils(chatApiVo);
        String result = gpt3Util.chat(chatApiVo);
        System.out.println(result);
    }


}
