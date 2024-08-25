package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.service.IFileService;
import com.ai.util.Result;
import com.ai.vo.FileVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2024-05-24
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Resource
    private IFileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @LoginRequired
    public Result<FileVo> handleFileUpload(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }
}
