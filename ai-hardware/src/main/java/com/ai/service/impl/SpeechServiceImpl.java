package com.ai.service.impl;
import com.ai.aspect.LoginAspect;
import com.ai.dto.SpeechTextDto;
import com.ai.entity.Speech;
import com.ai.mapper.SpeechMapper;
import com.ai.model.LoginEntity;
import com.ai.service.ISpeechService;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.util.SpeechUtils;
import com.ai.vo.SpeechAudioVo;
import com.ai.vo.SpeechTextVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class SpeechServiceImpl extends ServiceImpl<SpeechMapper, Speech> implements ISpeechService {

    SpeechUtils speechUtils = new SpeechUtils();


    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    @Override
    public ResponseEntity<Result<SpeechTextVo>> transcript(MultipartFile file) {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);
        String model = "whisper-1";
        byte[] audioData;
        try {
            audioData = file.getBytes();
        }catch (IOException e) {
            return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error("无法获取音频内容"));
        }
        String result = speechUtils.speechToText(audioData,model,fileExtension);
        try {
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.has("error")) {
                // 如果存在error对象，表示失败
                JSONObject error = jsonResponse.getJSONObject("error");
                return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error(error.getString("message")));
            }
        } catch (Exception e) {
            // 如果不是JSON格式，表示直接返回字符串，视为成功
        }
        SpeechTextVo speechTextVo = new SpeechTextVo().setContent(result);
        Speech speech = new Speech().setContent(result).setUserId(loginEntity.getUserId()).setModel(model);
        speech = speech.setCreateTime(LocalDateTime.now()).setConversionType("stt");
        this.save(speech);
        return ResponseEntity.ok(Result.success(speechTextVo));
    }

    @Override
    public ResponseEntity<Result<SpeechAudioVo>> read(SpeechTextDto speechTextdto) {
        return null;
    }
}
