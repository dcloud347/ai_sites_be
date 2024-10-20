package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.model.LoginEntity;
import com.ai.util.CommonUtil;
import com.ai.util.OssUtils;
import com.ai.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {
    @Resource
    private OssUtils ossUtils;
    /**
     * 上传文件
     */
    @PostMapping
    @LoginRequired
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file){
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 文件类型
        String t = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String name = loginEntity.getUserId() + "/"  + CommonUtil.generateUUID() + t;
        String url = ossUtils.uploadFile(file, name);
        HashMap<String, Object> map = new HashMap<>(1);
        map.put("url", url);
        return Result.success(map);
    }

}
