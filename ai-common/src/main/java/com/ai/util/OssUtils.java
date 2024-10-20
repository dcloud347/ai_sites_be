package com.ai.util;

import com.ai.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class OssUtils {
    public String uploadFile(MultipartFile file, String name) {
        COSCredentials cred = new BasicCOSCredentials(CosConfig.SECRET_ID, CosConfig.SECRET_KEY);
        Region region = new Region(CosConfig.REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        COSClient cosClient = new COSClient(cred, clientConfig);
        String bucketName = CosConfig.BUCKET_NAME;
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 创建上传Object的Metadata
        ObjectMetadata meta = new ObjectMetadata();
        // 必须设置ContentLength
        try {
            meta.setContentLength(file.getBytes().length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        meta.setContentEncoding("UTF-8");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, name, inputStream, meta);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        System.out.println(putObjectResult);
        return "https://" + CosConfig.BUCKET_NAME + ".cos." + CosConfig.REGION + ".myqcloud.com/" + name;
    }

}
