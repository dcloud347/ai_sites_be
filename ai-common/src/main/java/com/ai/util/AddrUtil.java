package com.ai.util;

import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import io.ipinfo.api.model.IPResponse;
/**
 * @author 刘晨
 */
public class AddrUtil {

    public static String getAddress(String ip){
        IPinfo ipinfo = new IPinfo.Builder().setToken("8678ba158f6456").build();
        IPResponse response;
        try {
            response = ipinfo.lookupIP(ip);
        } catch (RateLimitedException e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }
    public static void main(String[] args) {
//        getAddr();
        String address = getAddress("4.234.8.238");
        System.out.println(address);
    }
}

