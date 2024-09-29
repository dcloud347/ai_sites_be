package com.ai.mapper;

import com.ai.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
