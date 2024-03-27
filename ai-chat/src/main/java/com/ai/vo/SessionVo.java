package com.ai.vo;

import com.ai.entity.Session;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */

@Data
@NoArgsConstructor
public class SessionVo {
    private Long id;
    /**
     * 会话的标题
     */
    private String title;

    public SessionVo(Session session){
        id = session.getId();
        title = session.getTitle();
    }
}
