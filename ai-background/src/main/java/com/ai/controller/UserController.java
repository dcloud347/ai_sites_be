package com.ai.controller;

import com.ai.annotation.RoleRequired;
import com.ai.feign.UserService;
import com.ai.util.ExcelUtil;
import com.ai.util.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("list")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result userList(@RequestParam Integer current, Integer size){
        return userService.userList(current, size);
    }
    @GetMapping("excel")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public ResponseEntity userListToExcel(@RequestParam Integer current, Integer size){
        Result<List<HashMap<String, String>>> result = userService.userList(current, size);
        ArrayList<List<String>> arrayLists = new ArrayList<>();
        List<HashMap<String, String>> data = result.getData();
        for (HashMap<String, String> map: data){
            ArrayList<String> list = new ArrayList<>();
            // 这个顺序要和下面的表格头顺序一致
            list.add(map.get("nick"));
            list.add(map.get("email"));
            list.add(map.get("password"));
            list.add(map.get("code"));
            arrayLists.add(list);
        }
        return ExcelUtil.genExcel(new String[]{"昵称", "邮箱", "密码", "音箱设备"}, arrayLists);
    }

    @GetMapping("{id}")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result<Object> getData(@PathVariable String id){
        return userService.userData(id);
    }
}
