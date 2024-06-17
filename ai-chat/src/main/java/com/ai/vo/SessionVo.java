package com.ai.vo;

import com.ai.entity.Session;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDateTime startTime;
    private String type;

    public SessionVo(Session session){
        id = session.getId();
        title = session.getTitle();
        startTime = session.getStartTime();
        type = session.getType();
    }
}
