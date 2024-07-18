package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.entity.File;
import com.ai.mapper.FileMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IFileService;
import com.ai.util.CommonUtil;
import com.ai.util.Gpt3Util;
import com.ai.util.OssUtils;
import com.ai.util.Result;
import com.ai.vo.UploadVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    @Resource
    private OssUtils ossUtils = new OssUtils();
    @Override
    public ResponseEntity<Result<UploadVo>> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("The file can not be empty."));
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        try {
            // 调用工具类上传文件
            String response = new Gpt3Util().uploadFile(file);
            UploadVo uploadVo = JSON.parseObject(response, UploadVo.class);
            // 文件类型
            String t = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            // 上传到对象存储
            String name = loginEntity.getUserId() + "/"  + CommonUtil.generateUUID() + t;
            String container_name = "ai-sites-chatting-files";
            String url = ossUtils.uploadFile(file, name, container_name);
            uploadVo.setUrl(url);
            this.save(new File(uploadVo));
            return ResponseEntity.ok(Result.success(uploadVo));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Result.error("File upload failed: " + e.getMessage()));
        }
    }
}
