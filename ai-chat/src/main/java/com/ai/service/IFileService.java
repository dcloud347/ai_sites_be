package com.ai.service;

import com.ai.entity.File;
import com.ai.util.Result;
import com.ai.vo.UploadVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2024-05-24
 */
public interface IFileService extends IService<File> {

    ResponseEntity<Result<UploadVo>> uploadFile(MultipartFile file);
}
