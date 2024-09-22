package com.ai.mapper;

import com.ai.entity.Session;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 会话表 Mapper 接口
 * </p>
 *
 * @author 潘越
 * @since 2024-03-14
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
    @Delete("DELETE FROM session WHERE title = 'new chat' AND id NOT IN (SELECT session_id FROM message);")
    void deleteSessionsWithoutMessages();
}
