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
import com.ai.feign.UserService;
import com.ai.mapper.MessageMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IFileService;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.CommonUtil;
import com.ai.util.Gpt3Util;
import com.ai.util.Result;
import com.ai.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

    @Resource
    private UserService userService;
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

    // 解析花费的total_tokens
    private Integer getTokens(String text){
        JSONObject jsonObject = JSON.parseObject(text);
        if (jsonObject.getJSONObject("error") != null){
            throw new CustomException("GPT Error.");
        }
        JSONObject usage = jsonObject.getJSONObject("usage");
        return usage.getInteger("total_tokens");
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

    private ChatApiVo getChatApiVo(ChatDto chatDto, LoginEntity loginEntity) throws CustomException{
        String model;
        if(chatDto.getModel()==null){
            throw new CustomException("No specified model!");
        }
        switch (chatDto.getModel()){
            case "gpt3.5" -> model = "gpt-3.5-turbo";
            case "gpt4" -> model = "gpt-4-turbo";
            case "gpt-4o" -> model = "gpt-4o";
            default -> throw new CustomException("Unrecognised models " + chatDto.getModel());
        }
        if(chatDto.getFileId()!=null && !vision_models.contains(model)){
            throw new CustomException(model+" have no vision capabilities!");
        }
        if(chatDto.getSessionId()!=null && sessionService.getById(chatDto.getSessionId()).getUserId()!=loginEntity.getUserId()){
            throw new CustomException("No access to this session!");
        }
        ChatApiVo chatApiVo = new ChatApiVo().setModel(model);
        if (chatDto.getSessionId() == null){
            // 新建对话
            Session session = new Session();
            session.setTitle("new chat").setStartTime(LocalDateTime.now()).setUserId(loginEntity.getUserId());
            session.setType(loginEntity.getType());
            sessionService.save(session);
            chatDto.setSessionId(session.getId());
            if(Type.ROBOT.equals(loginEntity.getType())){
                Message message = new Message();
                message.setRole(Role.system).setSessionId(session.getId()).setContent("Please reply in a short response").setCreateTime(LocalDateTime.now()).setType(loginEntity.getType());
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
        return chatApiVo;
    }

    private ChatVo afterChat(ChatDto chatDto,ChatApiVo chatApiVo, String respondingMessage, Role role,
                           LoginEntity loginEntity, HttpServletRequest request) {
        // 保存聊天记录
        Message message = new Message(chatDto);
        message.setRole(Role.user);
        message.setType(loginEntity.getType());
        this.save(message);
        if(chatDto.getFileId()!=null){
            chatDto.getFileId().forEach(fileId -> {
                File file = fileService.getById(fileId);
                file.setMessageId(message.getId());
                fileService.updateById(file);
            });
        }
        // 保存gpt的回复
        ChatVo chatVo = new ChatVo();
        chatVo.setMessage(respondingMessage).setSessionId(chatDto.getSessionId()).setModel(chatDto.getModel());
        Message message1 = new Message(respondingMessage.strip(), chatVo.getSessionId());
        message1.setModel(chatApiVo.getModel());
        message1.setRole(role);
        message1.setType(loginEntity.getType());
        this.save(message1);
        if(Type.ROBOT.equals(loginEntity.getType())){
            // 音箱新建会话，需要保存会话id, 放在这个位置，每一次发送聊天，都会刷新保存时间，防止突然过期
            String key = RedisPrefixEnum.ROBOT_SESSION.getPrefix() + loginEntity.getUserId();
            redisTemplate.opsForValue().set(key, message.getSessionId(), SpeakerConfig.sessionActive, TimeUnit.MINUTES);
        }
        // 更新对话时间
        String ip = CommonUtil.getIpAddr(request);
        Session session = sessionService.getById(message1.getSessionId());
        Mono<LocalDateTime> dateTime = sessionService.getTimeZone(ip);
        LocalDateTime localDateTime = dateTime.block();
        session.setStartTime(localDateTime);
        // 将回答加入聊天记录
        chatApiVo.addTextMessage(clear(message1.getContent()),message1.getRole().toString());
        // 总结标题
        if (isPastTitle(session.getTitle())){
            chatApiVo.addTextMessage("Based on our dialogue, give me a short headline, pick the one you think " +
                    "is most appropriate, and your answer should only provide me with the headline.",Role.user.toString());
            // 开始总结
            chatApiVo.setStream(false);
            String title = gpt3Util.chat(chatApiVo);
            JSONObject msg1 = analysis(title);
            if (msg1 != null) {
                session.setTitle(msg1.getString("content"));
            }
        }
        sessionService.updateById(session);
        return chatVo;
    }


    @Override
    public ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto, HttpServletRequest request) throws CustomException {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 先检查余额是否不足
        Integer surplus = userService.getTokens(loginEntity.getUserId());
        if (surplus <= 0){
            throw new CustomException("Insufficient Balance");
        }
        ChatApiVo chatApiVo = getChatApiVo(chatDto, loginEntity);
        // 发送消息
        String chat = gpt3Util.chat(chatApiVo);
        if (chat == null){
            throw new CustomException("Network Error");
        }
        JSONObject msg = analysis(chat);

        Role role = Role.valueOf(msg.getString("role"));
        String content = msg.getString("content");
        ChatVo chatVo = afterChat(chatDto,chatApiVo, content,role, loginEntity, request);
        // 开始扣费
        Integer tokens = getTokens(chat);
        userService.setTokens(tokens, loginEntity.getUserId());
        chatVo.setSurplus(userService.getTokens(loginEntity.getUserId()));
        return ResponseEntity.ok(Result.success(chatVo));
    }

    @Override
    public ResponseEntity<Result<List<ChatRecordVo>>> record(String id) {
        ArrayList<ChatRecordVo> chatRecordVos = new ArrayList<>();
        List<Message> messages = this.list(new QueryWrapper<Message>().eq("session_id", id));
        messages.forEach(message -> {
            QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
            fileQueryWrapper.select("id","filename","bytes","created_at","url")
                    .eq("message_id", message.getId());
            List<FileVo> fileVos = new ArrayList<>();
            fileService.list(fileQueryWrapper).forEach(file -> fileVos.add(new FileVo(file)));
            chatRecordVos.add(new ChatRecordVo(message).setFiles(fileVos));
        });
        return ResponseEntity.ok(Result.success(chatRecordVos));
    }

    @Override
    public void streamChat(ChatDto chatDto, HttpServletRequest request, SseEmitter emitter, LoginEntity loginEntity) throws CustomException {
        ChatApiVo chatApiVo = getChatApiVo(chatDto, loginEntity).setStream(true);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest chattingRequest = gpt3Util.getChatRequest(chatApiVo);
        StringBuilder entireContent_sb = new StringBuilder();
        StringBuilder role_sb = new StringBuilder();
        try{
            HttpResponse<Stream<String>> response = client.send(chattingRequest, BodyHandlers.ofLines());
            // 处理响应体
            response.body().forEach(line -> {
                if (!line.equals("data: [DONE]") && line.startsWith("data: ")) {
                    String json = line.substring("data: ".length());
                    JSONObject jsonObject = JSON.parseObject(json);
                    JSONArray choices = jsonObject.getJSONArray("choices");
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject delta = choice.getJSONObject("delta");
                    String content = delta.getString("content");
                    String role_ = delta.getString("role");
                    if(role_ !=null){
                        role_sb.append(role_);
                    }
                    if(content != null){
                        entireContent_sb.append(content);
                        ChatVo chatVo = new ChatVo().setMessage(content).setSessionId(chatDto.getSessionId()).setModel(chatDto.getModel());
                        try {
                            emitter.send(SseEmitter.event().name("data").data(chatVo));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }catch (IOException | InterruptedException e) {
            throw new CustomException("Network Error");
        }
        Role role = Role.valueOf(role_sb.toString());
        afterChat(chatDto,chatApiVo, entireContent_sb.toString(), role, loginEntity, request);
    }
}
