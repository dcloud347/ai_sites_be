package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.entity.File;
import com.ai.exceptions.CustomException;
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
import org.springframework.beans.factory.annotation.Value;

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
    @Value("${oss.chatting-files}")
    private String chatting_files_container;

    @Override
    public ResponseEntity<Result<UploadVo>> uploadFile(MultipartFile file) throws CustomException {
        if (file.isEmpty()) {
            throw new CustomException("The file can not be empty.");
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
            String url = ossUtils.uploadFile(file, name, chatting_files_container);
            uploadVo.setUrl(url);
            this.save(new File(uploadVo));
            return ResponseEntity.ok(Result.success(uploadVo));

        } catch (IOException e) {
            throw new CustomException("File upload failed: " + e.getMessage());
        }
    }
}
