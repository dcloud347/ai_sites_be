package com.ai.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class GoogleUtil {
    private static final String googleClientId = "442601140790-3ouk3tulslkf8mpvv0dhv688porktg04.apps.googleusercontent.com";

    private static final String googleSearchKey = "AIzaSyCafETF7TFkwBNwKtdm6M3wpMMZF1a4Ch4";

    private static final String googleSearchEngineId = "d54064872586644c9";

    private static final String googleSearchUrl = "https://www.googleapis.com/customsearch/v1";

    public static String get_email(String token){
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null){
                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload.getEmail();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static String search(String word, Integer num, Integer start){
        if(num==null){
            num=10;
        }
        if(start==null){
            start=0;
        }
        try {
            // 创建HttpClient对象
            HttpClient client = HttpClient.newHttpClient();

            // 创建HttpRequest对象
            String urlString = String.format(googleSearchUrl+"?key=%s&cx=%s&q=%s&start=%d&num=%d",
                    URLEncoder.encode(googleSearchKey, StandardCharsets.UTF_8),
                    URLEncoder.encode(googleSearchEngineId, StandardCharsets.UTF_8),
                    URLEncoder.encode(word,StandardCharsets.UTF_8),
                    start,num);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET() // 默认是GET请求，可以省略
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode()==200){
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args){
    }




}
