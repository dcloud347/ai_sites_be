package com.ai.model;

import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Payload {
    Integer accountId;
    LoginType loginType;
    String iss;
    Date issueDate;
    Date expireDate;
    JwtType jwtType;

    public Map<String,Object> toMap() {
        Map<String,Object> map = new HashMap<>();
        map.put("accountId",accountId);
        map.put("loginType",loginType.toString());
        map.put("iss",iss);
        map.put("issueDate",issueDate.toString());
        map.put("expireDate",expireDate.toString());
        map.put("jwtType",jwtType);
        return map;
    }

    public Payload(Claims claims){
        this.accountId = (Integer) claims.get("accountId");
        this.loginType = LoginType.valueOf((String) claims.get("loginType"));
        this.iss = (String) claims.get("iss");
        System.out.println(claims.get("issueDate"));

        this.jwtType =  JwtType.valueOf((String) claims.get("jwtType"));
    }

}
