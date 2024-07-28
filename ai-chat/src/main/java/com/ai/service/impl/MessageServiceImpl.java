package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.config.SpeakerConfig;
import com.ai.dto.ChatDto;
import com.ai.entity.File;
import com.ai.entity.Message;
import com.ai.entity.Session;
import com.ai.enums.RedisPrefixEnum;
import com.ai.enums.Role;
import com.ai.enums.Type;
import com.ai.exceptions.CustomException;
import com.ai.mapper.MessageMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IFileService;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.CommonUtil;
import com.ai.util.Gpt3Util;
import com.ai.util.Result;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatApiVo;
import com.ai.vo.ChatVo;
import com.ai.vo.MessageApiVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author 潘越
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

    private final List<String> vision_models = List.of(new String[]{"gpt-4-turbo", "gpt-4o"});

    // 判断标题是否修改重新总结
    private boolean isPastTitle(String title) {
        return "new chat".equals(title);
    }

    private String clear(String text){
        return StringEscapeUtils.escapeJson(text);
    }

    // 解析gpt的回复
    private JSONObject analysis(String text) throws CustomException{
        JSONObject jsonObject = JSON.parseObject(text);
        if (jsonObject.getJSONObject("error") != null){
            throw new CustomException("GPT Error.");
        }
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        return choice.getJSONObject("message");
    }

    private boolean isImage(String filename){
        filename = filename.toLowerCase();
        // 判断文件名是否以图片格式的扩展名结束
        return filename.endsWith(".png") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".webp") ||
                filename.endsWith(".gif");
    }

    @Override
    public ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto, HttpServletRequest request) throws CustomException {
        String model;
        switch (chatDto.getMode()){
            case "gpt3.5" -> model = "gpt-3.5-turbo";
            case "gpt4" -> model = "gpt-4-turbo";
            case "gpt-4o" -> model = "gpt-4o";
            default -> throw new CustomException("Unrecognised models " + chatDto.getMode());
        }
        if(chatDto.getFileId()!=null && !vision_models.contains(model)){
            throw new CustomException(model+" have no vision capabilities!");
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        if(chatDto.getSessionId()!=null && sessionService.getById(chatDto.getSessionId()).getUserId()!=loginEntity.getUserId()){
            throw new CustomException("No access to this session!");
        }
        ChatApiVo chatApiVo = new ChatApiVo().setModel(model);
        if (chatDto.getSessionId() == null){
            // 新建对话
            Session session = new Session();
            session.setTitle(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setStartTime(LocalDateTime.now()).setUserId(loginEntity.getUserId());
            session.setType(chatDto.getType());
            sessionService.save(session);
            chatDto.setSessionId(session.getId());
            if(Type.ROBOT.equals(chatDto.getType())){
                Message message = new Message();
                message.setRole(Role.system).setSessionId(session.getId()).setContent("Please reply in a short response").setCreateTime(LocalDateTime.now());
                this.save(message);
            }
        }

        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        // 指定只查询content和role字段
        messageQueryWrapper.select("content", "role","id")
                .eq("session_id", chatDto.getSessionId())
                // 按创建时间升序排序
                .orderByAsc("create_time");
        // 查询出之前的聊天记录，并发回给chatgpt
        this.list(messageQueryWrapper).forEach(message -> {
            MessageApiVo messageApiVo = new MessageApiVo().setRole(message.getRole().toString());
            messageApiVo.addTextContent(message.getContent());
            QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
            fileQueryWrapper.select("filename","url")
                    .eq("message_id", message.getId());
            fileService.list(fileQueryWrapper).forEach(file -> {
                if(isImage(file.getFilename())){
                    messageApiVo.addImageContent(file.getUrl());
                }
            });
            chatApiVo.addMessage(messageApiVo);
        });
        MessageApiVo messageApiVo = new MessageApiVo().setRole(Role.user.toString());
        messageApiVo.addTextContent(chatDto.getContent());
        if(chatDto.getFileId()!=null){
            chatDto.getFileId().forEach(fileId -> {
                File file = fileService.getById(fileId);
                if(isImage(file.getFilename())){
                    messageApiVo.addImageContent(file.getUrl());
                }
            });
        }
        chatApiVo.addMessage(messageApiVo);
        // 使单次对话不会太长
        if (chatApiVo.getMessages().size() > 20){
            List<MessageApiVo> messages = chatApiVo.getMessages().subList(0,chatApiVo.getMessages().size()-20);
            messages.clear();
            chatApiVo.setMessages(messages);
        }
        // 发送消息
        String chat = gpt3Util.chat(chatApiVo);
        if (chat == null){
            throw new CustomException("Network Error");
        }
        JSONObject msg = analysis(chat);
        Role role = Role.valueOf(msg.getString("role"));
        String content = msg.getString("content");
        // 保存聊天记录
        Message message = new Message(chatDto);
        message.setRole(Role.user);
        this.save(message);
        // 保存gpt的回复
        ChatVo chatVo = new ChatVo();
        chatVo.setMessage(content).setSessionId(chatDto.getSessionId()).setModel(chatDto.getMode());
        Message message1 = new Message(content.strip(), chatVo.getSessionId());
        message1.setModel(model);
        message1.setRole(role);
        chatApiVo.addTextMessage(clear(message1.getContent()),message1.getRole().toString());
        this.save(message1);
        if(Type.ROBOT.equals(chatDto.getType())){
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
            chatApiVo.addTextMessage("According to the content of the previous chat with me," +
                    " give me a summary of a suitable title,I just want the title, other words, symbols do not want," +
                    " within 20 words",Role.user.toString());
            // 开始总结
            String title = gpt3Util.chat(chatApiVo);
            JSONObject msg1 = analysis(title);
            if (msg1 != null) {
                session.setTitle(msg1.getString("content"));
            }
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
