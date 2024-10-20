package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.entity.File;
import com.ai.exceptions.CustomException;
import com.ai.mapper.FileMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IFileService;
import com.ai.util.CommonUtil;
import com.ai.util.OssUtils;
import com.ai.util.Result;
import com.ai.vo.FileVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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
    public Result<FileVo> uploadFile(MultipartFile file) throws CustomException {
        if (file.isEmpty()) {
            throw new CustomException("The file can not be empty.");
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 文件类型
        String t = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        // 上传到对象存储
        String Id = CommonUtil.generateUUID();
        while(this.getById(Id)!=null){
            Id = CommonUtil.generateUUID();
        }
        String fileName = Id + t;
        String name = loginEntity.getUserId() + "/"  + fileName;
        String url = ossUtils.uploadFile(file, name);
        File file_ = new File().setId(Id).setFilename(fileName).setCreatedAt(LocalDateTime.now()).setUrl(url);
        file_.setBytes(file.getSize());
        this.save(file_);
        return Result.success(new FileVo(file_));
    }
}
