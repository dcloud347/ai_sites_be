package com.ai.service.impl;

import com.ai.entity.File;
import com.ai.mapper.FileMapper;
import com.ai.service.IFileService;
import com.ai.util.Gpt3Util;
import com.ai.util.Result;
import com.ai.vo.UploadVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2024-05-24
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    @Override
    public ResponseEntity<Result<UploadVo>> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("文件不能为空"));
        }
        try {
            // 调用工具类上传文件
            String response = new Gpt3Util().uploadFile(file);
            UploadVo uploadVo = JSON.parseObject(response, UploadVo.class);
            this.save(new File(uploadVo));
            return ResponseEntity.ok(Result.success(uploadVo));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Result.error("File upload failed: " + e.getMessage()));
        }
    }
}
