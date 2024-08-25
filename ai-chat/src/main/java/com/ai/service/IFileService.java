package com.ai.service;

import com.ai.entity.File;
import com.ai.util.Result;
import com.ai.vo.FileVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 潘越
 * @since 2024-05-24
 */
public interface IFileService extends IService<File> {

    Result<FileVo> uploadFile(MultipartFile file);
}
