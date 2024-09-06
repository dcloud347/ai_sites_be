package com.ai.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
public class AddrUtil {
    public static String calcAuthorization(String source, String secretId, String secretKey, String datetime) {
        String signStr = "x-date: " + datetime + "\n" + "x-source: " + source;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            Key sKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), mac.getAlgorithm());
            mac.init(sKey);
            byte[] hash = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));
            // 替换 BASE64Encoder
            String sig = Base64.getEncoder().encodeToString(hash);
            String auth = "hmac id=\"" + secretId + "\", algorithm=\"hmac-sha1\", headers=\"x-date x-source\", signature=\"" + sig + "\"";
            return auth;
        } catch (Exception e) {
            return null;
        }
    }
    public static String urlencode(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8),
                    URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8)
            ));
        }
        return sb.toString();
    }
    public static String getAddr(String ip) {
        //云市场分配的密钥Id
        String secretId = "AKID2j9Exec9df9201ng561o6edsj09njj4yx4dw";
        //云市场分配的密钥Key
        String secretKey = "dq1CpKg91bJDiZ4P9XjsHuQH7bMKbB3Muo3G6X74";
        String source = "market";
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String datetime = sdf.format(cd.getTime());
        // 签名
        String auth = calcAuthorization(source, secretId, secretKey, datetime);
        // 请求方法
        String method = "GET";
        // 请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Source", source);
        headers.put("X-Date", datetime);
        headers.put("Authorization", auth);
        // 查询参数
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("ipAddress", ip);
        // body参数
        Map<String, String> bodyParams = new HashMap<>();
        // url参数拼接
        String url = "https://service-bnnmp0n9-1319869901.bj.apigw.tencentcs.com/release/ipAddress/query";
        url += "?" + urlencode(queryParams);
        System.out.println(headers);
        BufferedReader in = null;
        String result = null;
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod(method);
            // request headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            // request body
            Map<String, Boolean> methods = new HashMap<>();
            methods.put("POST", true);
            methods.put("PUT", true);
            methods.put("PATCH", true);
            Boolean hasBody = methods.get(method);
            if (hasBody != null) {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(urlencode(bodyParams));
                out.flush();
                out.close();
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            result = "";
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        getAddr("4.234.8.238");
    }
}

