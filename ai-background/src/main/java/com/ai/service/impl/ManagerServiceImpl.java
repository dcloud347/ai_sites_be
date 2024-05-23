package com.ai.service.impl;

import com.ai.entity.Manager;
import com.ai.mapper.ManagerMapper;
import com.ai.service.IManagerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager> implements IManagerService {

}
