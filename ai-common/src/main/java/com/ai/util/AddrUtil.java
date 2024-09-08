package com.ai.util;

import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import io.ipinfo.api.model.IPResponse;

/**
 * @author 刘晨
 */
public class AddrUtil {

    public final static IPinfo ipinfo = new IPinfo.Builder().setToken("8678ba158f6456").build();


    public static String getAddress(String ip){
        IPResponse response;
        try {
            response = ipinfo.lookupIP(ip);
        } catch (RateLimitedException e) {
            throw new RuntimeException(e);
        }
        return response.getCity();
    }
    public static void main(String[] args) {
        String address = getAddress("4.234.8.238");
        System.out.println(address);
    }
}

