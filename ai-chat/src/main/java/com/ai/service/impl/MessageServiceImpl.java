package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.config.SpeakerConfig;
import com.ai.dto.ChatDto;
import com.ai.entity.File;
import com.ai.entity.Message;
import com.ai.entity.Session;
import com.ai.enums.RedisPrefixEnum;
import com.ai.mapper.MessageMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IFileService;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.CommonUtil;
import com.ai.util.Gpt3Util;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatVo;
import com.ai.vo.UploadVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private Gpt3Util gpt3Util;

    @Resource
    private IFileService fileService;

    // 判断标题是否修改重新总结
    private boolean isPastTitle(String title) {
        return "new chat".equals(title);
    }

    private String clear(String text){
        return StringEscapeUtils.escapeJson(text);
    }

    // 解析gpt的回复
    private JSONObject analysis(String text){
        JSONObject jsonObject = JSON.parseObject(text);
        if (jsonObject.getJSONObject("error") != null){
            System.out.println("gpt出错");
            System.out.println(jsonObject.getJSONObject("error").get("message").toString());
            return null;
        }
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        return choice.getJSONObject("message");
    }
    @Override
    public ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto, HttpServletRequest request) {
        String model;
        switch (chatDto.getMode()){
            case "gpt3.5" -> model = "gpt-3.5-turbo";
            case "gpt4" -> model = "gpt-4-turbo-preview";
            case "gpt-4o" -> model = "gpt-4o";
            default -> {return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("Unrecognised models" + chatDto.getMode()));}
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        ArrayList<String> list = new ArrayList<>();
        if (chatDto.getSessionId() == null){
            // 新建对话
            Session session = new Session();
            session.setTitle(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setStartTime(LocalDateTime.now()).setUserId(loginEntity.getUserId());
            session.setType(chatDto.getType());
            sessionService.save(session);
            chatDto.setSessionId(session.getId());
            if("speaker".equals(chatDto.getType())){
                list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "system", "Please reply in a short response"));
            }
        }else {
            QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
            // 指定只查询content和role字段
            queryWrapper.select("content", "role")
                    .eq("session_id", chatDto.getSessionId())
                    // 按创建时间升序排序
                    .orderByAsc("create_time");
            // 查询出之前的聊天记录，并发回给chatgpt
            this.list(queryWrapper).forEach(message -> {
                list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", message.getRole(), clear(message.getContent())));
                if (message.getFileId() != null){
                    // 把文件带上去聊天
                    list.add(String.format("{\"role\": \"%s\", \"content\": \"The user has uploaded a file with ID: %s,this is the ID of multiple files, separated by English commas\"}", "system", message.getFileId()));
                }
            });
        }
        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user", chatDto.getContent()));
        if (chatDto.getFileId() != null){
            // 把文件带上去聊天
            list.add(String.format("{\"role\": \"%s\", \"content\": \"The user has uploaded a file with ID: %s,this is the ID of multiple files, separated by English commas\"}", "system", chatDto.getFileId()));
        }
        // 使单次对话不会太长
        if (list.size() > 20){
            list.subList(0, list.size() - 20).clear();
        }
        // 发送消息
        String chat = gpt3Util.chat(list, model);
        if (chat == null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("Network Error"));
        }
        JSONObject msg = analysis(chat);
        if (msg == null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("gpt error"));
        }
        String role = msg.getString("role");
        String content = msg.getString("content");
        // 保存聊天记录
        Message message = new Message(chatDto);
        message.setRole("user").setUserId(loginEntity.getUserId());
        this.save(message);
        // 如果有图片信息，保存文件的聊天记录
        if(chatDto.getFileId()!=null){
            List<String> stringList = chatDto.getFileId();
            stringList.forEach(s -> {
                // 获取文件信息
                System.out.println(s);
                File file = fileService.getOne(new QueryWrapper<File>().eq("id", s));
                Message fileMessage = new Message(file);
                fileMessage.setRole("user");
                fileMessage.setUserId(loginEntity.getUserId());
                fileMessage.setSessionId(chatDto.getSessionId());
                System.out.println(fileMessage);
                this.save(fileMessage);
            });
        }
        // 保存gpt的回复
        ChatVo chatVo = new ChatVo();
        chatVo.setMessage(content).setSessionId(chatDto.getSessionId()).setModel(chatDto.getMode());
        System.out.println(content.strip());
        Message message1 = new Message(content.strip(), chatVo.getSessionId());
        message1.setModel(model);
        message1.setUserId(loginEntity.getUserId()).setRole(role);
        list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", message1.getRole(), clear(message1.getContent())));
        this.save(message1);
        if("speaker".equals(chatDto.getType())){
            // 音箱新建会话，需要保存会话id, 放在这个位置，每一次发送聊天，都会刷新保存时间，防止突然过期
            String key = RedisPrefixEnum.SPEAKER_SESSION.getPrefix() + loginEntity.getUserId();
            redisTemplate.opsForValue().set(key, message.getSessionId(), SpeakerConfig.sessionActive, TimeUnit.MINUTES);
        }
        // 更新对话时间
        String ip = CommonUtil.getIpAddr(request);
        Session session = sessionService.getById(message1.getSessionId());
        Mono<LocalDateTime> dateTime = this.sessionService.getTimeZone(ip);
        LocalDateTime localDateTime = dateTime.block();
        session.setStartTime(localDateTime);
        // 总结标题
        if (isPastTitle(session.getTitle())){
            list.add(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", "user",
                    "According to the content of the previous chat with me, give me a summary of a suitable title," +
                            " I just want the title, other words, symbols do not want, within 20 words"));
            // 开始总结
            String title = gpt3Util.chat(list, model);
            JSONObject msg1 = analysis(title);
            if (msg1 == null){
                return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("gpt error"));
            }
            String content1 = msg1.getString("content");
            System.out.println(content1);
            session.setTitle(content1);
        }
        sessionService.updateById(session);
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
