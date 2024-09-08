package com.ai.util;

import com.ai.config.OpenAiConfig;
import com.ai.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    public static ChatResponse chat(ChatApiVo chatApiVo){
        // 创建HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getChatRequest(chatApiVo);
        try {
            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json_str =  response.body();
            JSONObject json = JSON.parseObject(json_str);
            return analytics(json);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ChatResponse analytics(JSONObject json){
        ChatResponse chatResponse = new ChatResponse();
        JSONObject error = json.getJSONObject("error");
        if(error!=null){
            System.out.println("error:"+error.getString("message"));
            return chatResponse.setSuccess(false);
        }
        JSONObject usage = json.getJSONObject("usage");
        Integer total_tokens = usage.getInteger("total_tokens");
        JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
        String finish_reason = choice.getString("finish_reason");
        JSONObject message = choice.getJSONObject("message");
        String content = message.getString("content");
        String role = message.getString("role");
        chatResponse.setTotal_tokens(total_tokens).
                    setRole(role).
                    setContent(content).
                    setFinishReason(finish_reason);
        JSONArray tool_calls = message.getJSONArray("tool_calls");
        if(tool_calls==null)return chatResponse;
        for(int i = 0; i < tool_calls.size();i++){
            JSONObject tool_call = tool_calls.getJSONObject(i);
            ToolCallResponse toolCallResponse = chatResponse.getToolCall(i);
            toolCallResponse.setId(tool_call.getString("id"));
            toolCallResponse.setType(tool_call.getString("type"));
            JSONObject function = tool_call.getJSONObject("function");
            FunctionResponse functionResponse = toolCallResponse.getFunction();
            functionResponse.setName(function.getString("name"));
            functionResponse.setArguments(function.getString("arguments"));
        }
        return chatResponse;
    }



    public static void addUtils(ChatApiVo chatApiVo){
        ParametersApiVo parametersApiVo = new ParametersApiVo();
        parametersApiVo.addProperty("word","string","The searching words");
        parametersApiVo.addProperty("num","integer","The number of results to return, this number must be in the range 1 to 10");
        parametersApiVo.addProperty("start","integer","The number of result to start with");
        parametersApiVo.addRequired("word");
        chatApiVo.addTool("googleSearch","Search on Google", parametersApiVo);
        parametersApiVo = new ParametersApiVo();
        chatApiVo.addTool("get-city","Get User's location",parametersApiVo);
    }


    public static HttpRequest getChatRequest(ChatApiVo chatApiVo){
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
    public static void streamChat(ChatApiVo chatApiVo){

        chatApiVo.setStream(true);

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
        ChatApiVo chatApiVo = new ChatApiVo();
        chatApiVo.addTextMessage("你知道我在哪里吗","user");
        chatApiVo.setModel("gpt-4o");
        Gpt3Util.addUtils(chatApiVo);
        ChatResponse chatResponse = Gpt3Util.chat(chatApiVo);
        System.out.println(chatResponse);
    }


}
