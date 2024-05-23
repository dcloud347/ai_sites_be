package com.ai.util;

import com.ai.config.OpenAiConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpeechUtils {
    private static final String TTS_API_URL = "https://api.openai.com/v1/audio/speech";
    private static final String STT_API_URL = "https://api.openai.com/v1/audio/transcriptions";

    // 将API_KEY替换成你的API密钥
    private static final String API_KEY = new OpenAiConfig().getApiKey();
    public byte[] textToSpeech(String content, String model, String voice){
        // 准备JSON数据
        String jsonData = String.format("""
                {
                    "model": "%s",
                    "input": "%s",
                    "voice": "%s"
                }""", model, content, voice);
        // 创建HttpClient
        HttpClient client = HttpClient.newHttpClient();
        // 构建HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TTS_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
//                .timeout(Duration.ofSeconds(3))
                .build();
        try {
            // 发送请求并获取响应
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String speechToText(byte[] audio, String model,String suffix){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(STT_API_URL);

            // 添加授权头
            post.setHeader("Authorization", "Bearer " + API_KEY);

            // 构建 multipart 请求
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", audio, ContentType.DEFAULT_BINARY, "audio."+suffix);
            builder.addTextBody("model", model);
            builder.addTextBody("response_format", "text");

            post.setEntity(builder.build());
            String result;
            // 发送请求并处理响应
            try (CloseableHttpResponse response = client.execute(post)) {
                return new String(response.getEntity().getContent().readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        try{
            Path audioFilePath = Paths.get("South Way.m4a"); // 替换为你的音频文件路径
            if(audioFilePath.toFile().exists()){
                byte[] audioData = Files.readAllBytes(audioFilePath);
                String result = new SpeechUtils().speechToText(audioData,"whisper-1","m4a");
                System.out.println(result);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
