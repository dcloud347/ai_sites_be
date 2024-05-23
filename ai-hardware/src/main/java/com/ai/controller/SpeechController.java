package com.ai.controller;


import com.ai.annotation.LoginRequired;
import com.ai.service.ISpeechService;
import com.ai.service.impl.SpeechServiceImpl;
import com.ai.util.Result;
import com.ai.vo.SpeechTextVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-03-12

 */
@RestController
@RequestMapping("/api/speech")
public class SpeechController {
    @Resource
    private ISpeechService speechService;

    @PostMapping("speech-to-text")
    @LoginRequired
    public ResponseEntity<Result<SpeechTextVo>> login(@RequestParam("file") MultipartFile file){
        return speechService.transcript(file);
    }
}