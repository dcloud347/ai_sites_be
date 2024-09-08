package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.util.GoogleUtil;
import com.ai.util.Result;
import com.ai.util.CommonUtil;
import com.ai.util.AddrUtil;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * <p>
 * 消息表 前端控制器
 * </p>
 *
 * @author 刘晨
 * @since 2024-03-14
 */
@RestController
@RequestMapping("/api/tool")
public class ToolController {

    /**
     * 查询我的所有会话记录
     */
    @GetMapping("google-search")
    @LoginRequired
    public Result<Map<String,String>> googleSearch(@RequestParam String word,Integer num, Integer start){
        Map<String, String> result = new HashMap<>();
        result.put("result",GoogleUtil.search(word,num,start));
        return Result.success(result);
    }

    /**
     * 查询我的位置
     */
    @GetMapping("get-city")
    @LoginRequired
    public Result<Map<String,String>> getCity(HttpServletRequest request){
        String ip = CommonUtil.getIpAddr(request);
        String city = AddrUtil.getAddress(ip);
        Map<String,String> result = new HashMap<>();
        result.put("result",city);
        return Result.success(result);
    }

}
