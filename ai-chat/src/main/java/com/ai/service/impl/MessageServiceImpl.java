package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.dto.ChatDto;
import com.ai.entity.Message;
import com.ai.entity.Session;
import com.ai.mapper.MessageMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.Gpt3Util;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Resource
    private ISessionService sessionService;

    @Resource
    private Gpt3Util gpt3Util;
    @Override
    public ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto) {
        String model;
        switch (chatDto.getMode()){
            case "gpt3.5" -> model = "gpt-3.5-turbo";
            case "gpt4" -> model = "gpt-4-turbo-preview";
            case "dall3" -> model = "dall-e-3";
            default -> {return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error("不认识的模型" + chatDto.getMode()));}
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        ArrayList<String> list = new ArrayList<>();
        if (chatDto.getSessionId() == null){
            // 新建对话
            Session session = new Session();
            session.setTitle(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setStartTime(LocalDateTime.now()).setUserId(loginEntity.getUserId());
            sessionService.save(session);
            chatDto.setSessionId(session.getId());
        }else {
            QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
            // 指定只查询content和role字段
            queryWrapper.select("content", "role")
                    .eq("session_id", chatDto.getSessionId())
                    // 按创建时间升序排序
                    .orderByAsc("create_time");
            // 查询出之前的聊天记录，并发回给chatgpt
            this.list(queryWrapper).forEach(message -> list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", message.getRole(), message.getContent())));
        }
        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user", chatDto.getContent()));

        // 发送消息
        String chat = gpt3Util.chat(list, model);
        System.out.println(chat);
        if (chat == null){
            return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error("网络异常"));
        }
        JSONObject jsonObject = JSON.parseObject(chat);
        if (jsonObject.getJSONObject("error") != null){
            return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error(jsonObject.getJSONObject("error").get("message").toString()));
        }
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        JSONObject msg = choice.getJSONObject("message");
        String role = msg.getString("role");
        String content = msg.getString("content");
        // 保存聊天记录
        Message message = new Message(chatDto);
        message.setRole("user").setUserId(loginEntity.getUserId());
        this.save(message);
        // 保存gpt的回复
        ChatVo chatVo = new ChatVo();
        chatVo.setMessage(content).setSessionId(chatDto.getSessionId());

        Message message1 = new Message(chatVo);
        message1.setUserId(loginEntity.getUserId()).setRole(role);
        this.save(message1);
        return ResponseEntity.ok(Result.success(chatVo));
    }

    @Override
    public ResponseEntity<Result<List<ChatRecordVo>>> record(String id) {
        ArrayList<ChatRecordVo> chatRecordVos = new ArrayList<>();
        List<Message> messages = this.list(new QueryWrapper<Message>().eq("session_id", id));
        messages.forEach(message -> chatRecordVos.add(new ChatRecordVo(message)));
        return ResponseEntity.ok(Result.success(chatRecordVos));
    }
}
