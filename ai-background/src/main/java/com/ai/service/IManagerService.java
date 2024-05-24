package com.ai.service;

import com.ai.dto.LoginDto;
import com.ai.entity.Manager;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
public interface IManagerService extends IService<Manager> {

    Result<LoginVo> login(LoginDto loginDto);

    Result create(Manager manager);
}
