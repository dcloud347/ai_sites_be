package com.ai.controller;

import com.ai.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/model")
public class ModelController {

    @GetMapping
    public Result<List<String>> getModel(){
        ArrayList<String> list = new ArrayList<>();
        list.add("gpt3.5");
        list.add("gpt4");
        list.add("dall3");
        return Result.success(list);
    }

}
