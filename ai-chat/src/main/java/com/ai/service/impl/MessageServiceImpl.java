package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.config.SpeakerConfig;
import com.ai.dto.ChatDto;
import com.ai.dto.MessageDto;
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
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private IFileService fileService;

    @Resource
    private UserService userService;

    private final List<String> vision_models = List.of(new String[]{"gpt-4-turbo", "gpt-4o"});
    private final WebClient webClient;

    public MessageServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://ip-api.com").build();
    }
    // 判断标题是否修改重新总结
    private boolean isPastTitle(String title) {
        return "new chat".equals(title);
    }

    private String clear(String text){
        return StringEscapeUtils.escapeJson(text);
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

    private String getModel(String model_){
        String model;
        if(model_==null){
            throw new CustomException("No specified model!");
        }
        switch (model_){
            case "gpt3.5" -> model = "gpt-3.5-turbo";
            case "gpt4" -> model = "gpt-4-turbo";
            case "gpt-4o" -> model = "gpt-4o";
            case "gpt-4o-mini"-> model = "gpt-4o-mini";
            case "o1-preview"->model = "o1-preview";
            case "o1-mini" -> model = "o1-mini";
            default -> throw new CustomException("Unrecognised models " + model_);
        }
        return model;
    }

    public ChatApiVo getChatApiVo(ChatDto chatDto, LoginEntity loginEntity) throws CustomException{
        String model = getModel(chatDto.getModel());
        if (chatDto.getSessionId() == null){
            throw new CustomException("No specific Session!");
        }
        Session session = sessionService.getById(chatDto.getSessionId());
        if(session==null){
            throw new CustomException("Session doesn't exist!");
        }
        if(session.getUserId()!=loginEntity.getUserId()){
            throw new CustomException("No access to this session!");
        }
        ChatApiVo chatApiVo = new ChatApiVo().setModel(model);

        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        // 查询指定字段
        messageQueryWrapper.select("content", "role","id","tool_call","tool_call_id")
                .eq("session_id", chatDto.getSessionId())
                // 按创建时间升序排序
                .orderByAsc("create_time");
        // 查询出之前的聊天记录，并发回给chatgpt
        this.list(messageQueryWrapper).forEach(message -> {
            MessageApiVo messageApiVo = new MessageApiVo();
            switch (message.getRole()){
                case user -> {
                    messageApiVo.setRole(message.getRole().toString());
                    messageApiVo.addTextContent(message.getContent());
                    QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
                    fileQueryWrapper.select("filename","url")
                            .eq("message_id", message.getId());
                    List<File> files = fileService.list(fileQueryWrapper);
                    if(!files.isEmpty() && !vision_models.contains(model)){
                        throw new CustomException(model+" has no vision capability");
                    }
                    files.forEach(file -> {
                        if(isImage(file.getFilename())){
                            messageApiVo.addImageContent(file.getUrl());
                        }
                    });
                }
                case system -> {
                    messageApiVo.setRole(message.getRole().toString());
                    messageApiVo.addTextContent(message.getContent());
                }
                case assistant -> {
                    messageApiVo.setRole(message.getRole().toString());
                    messageApiVo.addTextContent(message.getContent());
                    messageApiVo.setTool_calls(message.getToolCall());
                }
                case tool -> {
                    messageApiVo.setRole(message.getRole().toString());
                    messageApiVo.addTextContent(message.getContent());
                    messageApiVo.setTool_call_id(message.getToolCallId());
                }
                default -> throw new CustomException("Unrecognised Role " + chatDto.getModel());
            }
            chatApiVo.addMessage(messageApiVo);
        });
        // 使单次对话不会太长
        if (chatApiVo.getMessages().size() > 20){
            List<MessageApiVo> messages = chatApiVo.getMessages().subList(0,chatApiVo.getMessages().size()-20);
            messages.clear();
            chatApiVo.setMessages(messages);
        }
        return chatApiVo;
    }

    public ChatVo afterChat(ChatDto chatDto,ChatApiVo chatApiVo, ChatResponse chatResponse,
                           LoginEntity loginEntity, HttpServletRequest request){

        String finishReason = chatResponse.getFinishReason();
        // 保存gpt的回复

        Message message = new Message(chatResponse);
        message.setModel(chatApiVo.getModel());
        message.setSessionId(chatDto.getSessionId());
        message.setType(loginEntity.getType());
        if(chatResponse.getToolCalls()!=null){
            message.setToolCall(new JSONArray(chatResponse.getToolCalls()));
        }
        this.save(message);

        //构建返回对象
        ChatVo chatVo = new ChatVo();
        chatVo.setToolCalls(chatResponse.getToolCalls()).setFinishReason(chatResponse.getFinishReason());
        chatVo.setMessage(chatResponse.getContent()).setSessionId(chatDto.getSessionId()).setModel(chatDto.getModel());

        // 音箱新建会话，需要保存会话id, 放在这个位置，每一次发送聊天，都会刷新保存时间，防止突然过期
        if(Type.ROBOT.equals(loginEntity.getType())){
            String key = RedisPrefixEnum.ROBOT_SESSION.getPrefix() + loginEntity.getUserId();
            redisTemplate.opsForValue().set(key, message.getSessionId(), SpeakerConfig.sessionActive, TimeUnit.MINUTES);
        }

        // 更新对话时间
        Session session = sessionService.getById(message.getSessionId());
        session.setStartTime(LocalDateTime.now());

        // 总结标题
        if(finishReason.equals("stop")){
            // 将回答加入聊天记录
            chatApiVo.addTextMessage(clear(message.getContent()),message.getRole().toString());
            if (isPastTitle(session.getTitle())){
                chatApiVo.addTextMessage("Based on our dialogue, give me a short headline, pick the one you think " +
                        "is most appropriate, and your answer should only provide me with the headline.",Role.user.toString());
                // 开始总结
                chatApiVo.setStream(false);
                chatApiVo.setStream_options(null);
                ChatResponse response = Gpt3Util.chat(chatApiVo);
                if (response != null) {
                    String title = response.getContent();
                    session.setTitle(title);
                    chatVo.setTitle(title);
                }
            }
        }
        sessionService.updateById(session);
        return chatVo;
    }

    @Override
    public Mono<String> getTimeZone(String ip) {
        return this.webClient.get()
                .uri("/json/{ip}", ip)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .map(this::extractTimeZoneFromResponse)
                .onErrorResume(throwable -> {
                    // Handle the error here
                    return Mono.empty();
                });
    }

    @Override
    public Result share(String id) {
        // 生成code
        String code = CommonUtil.generateUUID();
        Session session = sessionService.getById(id);
        if (session.getUserId() != LoginAspect.threadLocal.get().getUserId()){
            return Result.error("No right to share");
        }
        redisTemplate.opsForValue().set(RedisPrefixEnum.SHARE.getPrefix() + code, id, 7, TimeUnit.DAYS);
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        return Result.success(map);
    }

    @Override
    public Result<List<ChatRecordVo>> getShare(String code) {
        String id = (String) redisTemplate.opsForValue().get(RedisPrefixEnum.SHARE.getPrefix() + code);
        List<ChatRecordVo> record = this.record(id);
        return Result.success(record);
    }

    private String extractTimeZoneFromResponse(String response) throws CustomException{
        // JSON解析
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if("fail".equals(jsonObject.get("status").getAsString())){
            throw new CustomException("Local Time Parse Error");
        }
        return jsonObject.get("timezone").getAsString();
    }
    @Override
    public ResponseEntity<Result<ChatVo>> chat(ChatDto chatDto, HttpServletRequest request) {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 先检查余额是否不足
        Integer surplus = userService.getTokens(loginEntity.getUserId());
        if (surplus <= 0){
            throw new CustomException("Insufficient Balance");
        }
        ChatApiVo chatApiVo = getChatApiVo(chatDto, loginEntity);
        //加入联网以及其他可能被调用的工具
        Gpt3Util.addUtils(chatApiVo);
        // 发送消息
        ChatResponse chat = Gpt3Util.chat(chatApiVo);
        if (chat == null){
            throw new CustomException("Network Error");
        }else if(!chat.isSuccess()){
            throw new CustomException("Get Response Fail");
        }
        ChatVo chatVo = afterChat(chatDto,chatApiVo, chat, loginEntity, request);
        // 开始扣费
        Integer tokens = chat.getTotal_tokens();
        userService.setTokens(tokens, loginEntity.getUserId());
        chatVo.setSurplus(userService.getTokens(loginEntity.getUserId()));
        return ResponseEntity.ok(Result.success(chatVo));
    }

    @Override
    public Result<Map<String,Object>> addMessage(MessageDto messageDto, HttpServletRequest request) {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();

        if(messageDto.getRole()!=Role.user && messageDto.getRole()!=Role.tool){
            throw new CustomException("Sending message role must be either user or tool!");
        }
        //获取Session
        Session session;
        if (messageDto.getSessionId() == null){
            // 如果没有会话则新建一个
            session = new Session();
            session.setTitle("new chat").setStartTime(LocalDateTime.now()).setUserId(loginEntity.getUserId());
            session.setType(loginEntity.getType());
            sessionService.save(session);
            messageDto.setSessionId(session.getId());
            if(Type.ROBOT.equals(loginEntity.getType())){
                Message message = new Message();
                message.setRole(Role.system).setSessionId(session.getId()).setContent("You are a chatting robot that is trying to make friends with the user" +
                        ",please reply in a short response and do not include long or complex url in your response!").setCreateTime(LocalDateTime.now()).setType(loginEntity.getType());
                this.save(message);
            }
        }else{
            session = sessionService.getById(messageDto.getSessionId());
            if(session==null){
                throw new CustomException("Session doesn't Exist!");
            }
            if(session.getUserId()!=loginEntity.getUserId()){
                throw new CustomException("No access to this session!");
            }
        }

        //保存信息
        Message message = new Message(messageDto);
        message.setType(loginEntity.getType());
        message.setSessionId(session.getId());
        if(messageDto.getRole()==Role.tool){
            message.setToolCallId(messageDto.getToolCallId());
        }
        this.save(message);
        if(messageDto.getRole()==Role.user){
            if(messageDto.getFileId()!=null){
                messageDto.getFileId().forEach(fileId -> {
                    File file = fileService.getById(fileId);
                    file.setMessageId(message.getId());
                    fileService.updateById(file);
                });
            }
        }

        // 音箱新建会话，需要保存会话id, 放在这个位置，每一次发送聊天，都会刷新保存时间，防止突然过期
        if(Type.ROBOT.equals(loginEntity.getType())){
            String key = RedisPrefixEnum.ROBOT_SESSION.getPrefix() + loginEntity.getUserId();
            redisTemplate.opsForValue().set(key, session.getId(), SpeakerConfig.sessionActive, TimeUnit.MINUTES);
        }

        // 更新对话时间
        session.setStartTime(LocalDateTime.now());
        Map<String,Object> result = new HashMap<>();
        result.put("sessionId",session.getId());
        return Result.success(result);
    }

    @Override
    public List<ChatRecordVo> record(String id) {
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
        return chatRecordVos;
    }
}
