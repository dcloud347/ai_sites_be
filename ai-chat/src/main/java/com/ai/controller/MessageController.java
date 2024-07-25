package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.dto.ChatDto;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatVo;
import com.ai.vo.SessionVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 消息表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private IMessageService messageService;

    @Resource
    private ISessionService sessionService;

    /**
     * 发起聊天
     */
    @PostMapping
    @LoginRequired
    public ResponseEntity<Result<ChatVo>> chat(@RequestBody ChatDto chatDto, HttpServletRequest request){
        return messageService.chat(chatDto, request);
    }
    /**
     * 查询我的所有会话记录
     */
    @GetMapping
    @LoginRequired
    public Result<Map> list(){
        LocalDateTime now = LocalDateTime.now();
        List<SessionVo> today = new ArrayList<>();
        List<SessionVo> yesterday = new ArrayList<>();
        List<SessionVo> last7Days = new ArrayList<>();
        List<SessionVo> last30Days = new ArrayList<>();
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        ArrayList<SessionVo> sessionVos = new ArrayList<>();
        sessionService.list(new QueryWrapper<Session>().eq("user_id", loginEntity.getUserId()).orderByDesc("id")).forEach(session -> sessionVos.add(new SessionVo(session)));
        for (SessionVo vo : sessionVos) {
            LocalDate sessionDate = vo.getStartTime().toLocalDate();
            if (sessionDate.equals(now.toLocalDate())) {
                today.add(vo);
            } else if (sessionDate.equals(now.minusDays(1).toLocalDate())) {
                yesterday.add(vo);
            } else if (sessionDate.isAfter(now.minusDays(7).toLocalDate())) {
                last7Days.add(vo);
            } else if (sessionDate.isAfter(now.minusDays(30).toLocalDate())) {
                last30Days.add(vo);
            }
        }
        HashMap<String, List> map = new HashMap<>();
        map.put("today", today);
        map.put("yesterday", yesterday);
        map.put("last7Days", last7Days);
        map.put("last30Days", last30Days);
        return Result.success(map);
    }

    /**
     * 查询具体的聊天记录
     */
    @GetMapping("{id}")
    @LoginRequired
    public ResponseEntity<Result<List<ChatRecordVo>>> record(@PathVariable String id){
        // 只能查询自己的聊天记录，防越权攻击
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        Session session = sessionService.getById(id);
        if (session == null || session.getUserId() != loginEntity.getUserId()){
            return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error("No access to the resources"));
        }
        return messageService.record(id);
    }

    /**
     * 查询某用户会话下的聊天记录
     */
    @GetMapping("chat/{id}")
    public Result getChat(@PathVariable String id){
        ResponseEntity<Result<List<ChatRecordVo>>> record = messageService.record(id);
        return record.getBody();
    }
}
