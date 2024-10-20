package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.dto.ChatDto;
import com.ai.dto.MessageDto;
import com.ai.entity.Session;
import com.ai.exceptions.CustomException;
import com.ai.model.LoginEntity;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.*;
import com.ai.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息表 前端控制器
 * </p>
 *
 * @author 刘晨
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
    @PostMapping("get-response")
    @LoginRequired
    public ResponseEntity<Result<ChatVo>> chat(@RequestBody ChatDto chatDto, HttpServletRequest request){
        return messageService.chat(chatDto, request);
    }

    /**
     * 添加聊天消息
     */
    @PostMapping
    @LoginRequired
    public Result<Map<String,Object>> addMessages(@RequestBody MessageDto messageDto, HttpServletRequest request){
        return messageService.addMessage(messageDto, request);
    }

    @PostMapping(value = "/stream-chatting")
    @LoginRequired
    public SseEmitter streamChatting(@RequestBody ChatDto chatDto, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onTimeout(emitter::complete);
        //构建chatApiVo
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        ChatApiVo chatApiVo = messageService.getChatApiVo(chatDto,loginEntity).setStream(true).setIncludeUsage(true);
        Gpt3Util.addUtils(chatApiVo);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest chatRequest =  Gpt3Util.getChatRequest(chatApiVo);

        //开启线程
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            ChatResponse chatResponse = new ChatResponse();
            CompletableFuture<Void> responseFuture = client.sendAsync(chatRequest, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> response.body().forEach(line -> {
                        if(line.isEmpty()){
                            return;
                        }
                        if(!line.startsWith("data: ")){
                            chatResponse.setSuccess(false);
                            return;
                        }
                        String jsonData = line.substring(6).trim();
                        if(jsonData.equals("[DONE]"))return;
                        JSONObject jsonObject = JSON.parseObject(jsonData);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        JSONObject usage = jsonObject.getJSONObject("usage");
                        //处理token
                        if(usage!=null){
                            chatResponse.setTotal_tokens(usage.getInteger("total_tokens"));
                        }
                        //处理delta
                        JSONObject delta;
                        if(choices.isEmpty())return;

                        JSONObject choice = choices.getJSONObject(0);
                        String finish_reason=choice.getString("finish_reason");
                        if(finish_reason!=null){
                            chatResponse.addFinishedReason(finish_reason);
                        }
                        delta = choice.getJSONObject("delta");

                        JSONArray tool_calls = delta.getJSONArray("tool_calls");
                        if(tool_calls!=null){
                            for(int i=0; i<tool_calls.size(); i++){
                                JSONObject tool_call = tool_calls.getJSONObject(i);
                                ToolCallResponse toolCallResponse = chatResponse.getToolCall(tool_call.getInteger("index"));

                                String id = tool_call.getString("id");
                                if(id!=null){
                                    toolCallResponse.addId(id);
                                }

                                String type = tool_call.getString("type");
                                if(type!=null){
                                    toolCallResponse.addType(type);
                                }

                                JSONObject function = tool_call.getJSONObject("function");
                                if(function!=null){
                                    FunctionResponse functionResponse = toolCallResponse.getFunction();
                                    String name = function.getString("name");
                                    if(name!=null){
                                        functionResponse.addName(name);
                                    }
                                    String arguments = function.getString("arguments");
                                    if(arguments!=null){
                                        functionResponse.addArguments(arguments);
                                    }
                                }
                            }
                        }
                        String role = delta.getString("role");
                        if(role!=null){
                            chatResponse.addRole(role);
                        }
                        String content = delta.getString("content");
                        if(content!=null){
                            chatResponse.addContent(content);
                        }
                        try {
                            Map<String,Object> map = new HashMap<>();
                            map.put("delta",delta);
                            Result<Map<String,Object>> result = Result.success(map);
                            emitter.send(JSONObject.toJSON(result).toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            // 等待完成
            responseFuture.join();
            if(chatResponse.isSuccess()){
                //保存聊天信息
                ChatVo chatVo = messageService.afterChat(chatDto,chatApiVo,chatResponse,loginEntity,request);
                try {
                    Result<Object> result = Result.success(chatVo);
                    emitter.send(JSONObject.toJSON(result).toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else{
                try {
                    Result<Object> result = Result.error("GPT Error");
                    emitter.send(JSONObject.toJSON(result).toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // Complete the SSE stream
            emitter.complete();
        });

        service.shutdown();
        return emitter;
    }
    /**
     * 查询我的所有存档记录
     */
    @GetMapping("archive")
    @LoginRequired
    public Result<List<SessionVo>> archive(){
        List<SessionVo> sessionVos = new ArrayList<>();
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        sessionService.list(new QueryWrapper<Session>().eq("user_id", loginEntity.getUserId()).eq("archive", true).orderByDesc("id"))
                .forEach(session -> sessionVos.add(new SessionVo(session)));
        return Result.success(sessionVos);
    }
    /**
     * 查询我的所有会话记录
     */
    @GetMapping
    @LoginRequired
    public Mono<Result<Map<String, List<SessionVo>>>> list(HttpServletRequest request) {
        // 获取用户登录信息
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 获取用户的 IP 地址
        String ipAddr = CommonUtil.getIpAddr(request);
        // 获取用户时区的 Mono 对象
        Mono<String> timeZoneMono = messageService.getTimeZone(ipAddr);
        // 异步处理
        return timeZoneMono.map(timeZoneId -> {
            // 获取当前时间，转换为用户时区时间
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZoneId));
            // 定义各个时间段的SessionVo列表
            List<SessionVo> today = new ArrayList<>();
            List<SessionVo> yesterday = new ArrayList<>();
            List<SessionVo> last7Days = new ArrayList<>();
            List<SessionVo> last30Days = new ArrayList<>();
            // 查询用户的所有会话，并按 ID 降序排列
            List<SessionVo> sessionVos = new ArrayList<>();
            sessionService.list(new QueryWrapper<Session>().eq("user_id", loginEntity.getUserId()).eq("archive", false).orderByDesc("id"))
                    .forEach(session -> sessionVos.add(new SessionVo(session)));
            // 遍历会话列表，将时间转换为用户时区，并进行分类
            for (SessionVo vo : sessionVos) {
                // 将 session 的 startTime 转换为用户时区时间
                LocalDateTime sessionStartTime = vo.getStartTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(timeZoneId)).toLocalDateTime();
                LocalDate sessionDate = sessionStartTime.toLocalDate();
                // 根据时间段将会话分类
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
            // 将分类结果放入 map
            Map<String, List<SessionVo>> map = new HashMap<>();
            map.put("today", today);
            map.put("yesterday", yesterday);
            map.put("last7Days", last7Days);
            map.put("last30Days", last30Days);
            // 返回结果
            return Result.success(map);
        });
    }

    /**
     * 查询聊天记录
     */
    @GetMapping("{id}")
    @LoginRequired
    public Mono<ResponseEntity<Result<List<ChatRecordVo>>>> record(@PathVariable String id, HttpServletRequest request) throws CustomException {
        // 只能查询自己的聊天记录，防越权攻击
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        Session session = sessionService.getById(id);
        if (session == null || session.getUserId() != loginEntity.getUserId()){
            throw new CustomException("No access to the resources");
        }
        String ipAddr = CommonUtil.getIpAddr(request);
        // 获取用户所在时区的 Mono 对象（Mono<String> 表示时区 ID）
        Mono<String> timeZoneMono = messageService.getTimeZone(ipAddr);
        // 获取聊天记录
        List<ChatRecordVo> record = messageService.record(id);
        // 处理异步时间转换
        return timeZoneMono.map(timeZoneId -> {
            // 将每条记录的时间从 UTC 转换为用户所在时区的时间
            List<ChatRecordVo> updatedRecords = record.stream().map(chatRecordVo -> {
                // 假设 chatRecordVo 中的 createTime 是 LocalDateTime 格式
                LocalDateTime utcCreateTime = chatRecordVo.getCreateTime();
                // 转换 UTC 时间为用户时区时间
                ZonedDateTime utcZonedDateTime = utcCreateTime.atZone(ZoneId.of("UTC"));
                ZonedDateTime userZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of(timeZoneId));
                // 更新 Vo 的 createTime 为用户时区的时间
                chatRecordVo.setCreateTime(userZonedDateTime.toLocalDateTime());
                return chatRecordVo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Result.success(updatedRecords));
        });
    }

    /**
     * 分享会话
     */
    @PostMapping("share")
    @LoginRequired
    public Result share(@RequestParam String id){
        return messageService.share(id);
    }
    /**
     * 获取分享的会话
     */
    @GetMapping("share")
    public Result<List<ChatRecordVo>> getShare(@RequestParam String code){
        return messageService.getShare(code);
    }

    /**
     * 文字转语音
     */
    @GetMapping("text-to-speech")
    public Result textToSpeech(String text){
//        byte[] bytes = new SpeechUtils().textToSpeech(text, "tts-1", "alloy");
//        String url = ossUtils.uploadFile(bytes, "speech/" + CommonUtil.generateUUID() + ".mp4", media_container);
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("url", url);
//        return Result.success(map);
        return null;
    }
}
