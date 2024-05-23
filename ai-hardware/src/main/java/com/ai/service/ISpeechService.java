package com.ai.service;

import com.ai.dto.SpeechTextDto;
import com.ai.entity.Speech;
import com.ai.util.Result;
import com.ai.vo.SpeechAudioVo;
import com.ai.vo.SpeechTextVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ISpeechService extends IService<Speech> {

    ResponseEntity<Result<SpeechTextVo>> transcript(MultipartFile file);
    ResponseEntity<Result<SpeechAudioVo>> read(SpeechTextDto speechTextdto);


}
