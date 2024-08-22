package com.ai.util;

import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import com.ai.exceptions.ServerException;
import com.ai.model.Payload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.*;


/**
 * @author 刘晨
 */
public class JwtUtil {
    //私钥 / 生成签名的时候使用的秘钥secret，一般可以从本地配置文件中读取，切记这个秘钥不能外露，只在服务端使用，在任何场景都不应该流露出去。
    // 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
    private final static String secretString = "R6lHAPJW75UMQ5cmplQIgsgkgzn0hn7asGnp0O97lpk=";
    private final static SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));

    // 过期时间（单位秒）/ 12小时
    private final static Long access_token_expiration = 3600L * 6;

    private final static Long refresh_token_expiration = 3600L * 24 * 30;

    //jwt签发者
    private final static String jwt_iss = "api.acumenbot.ai";

    //jwt所有人
    private final static String subject = "acumenbot.ai";

    /**
     * 创建jwt
     *
     * @return 返回生成的jwt token
     */
    public static String generateJwtToken(Integer accountId, LoginType loginType, JwtType jwtType) {

        // 头部 map / Jwt的头部承载，第一部分
        // 可不设置 默认格式是{"alg":"HS256"}
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HMAC-SHA");
        header.put("typ", "JWT");
        Date issuerDate = new Date();
        Date expireDate;
        if(jwtType.equals(JwtType.access_token)){
            expireDate = new Date(System.currentTimeMillis() + access_token_expiration * 1000);
        }else{
            expireDate = new Date(System.currentTimeMillis() + refresh_token_expiration * 1000);
        }

        //私有声明 / 自定义数据，根据业务需要添加
        Payload payload = new Payload(accountId,loginType,jwt_iss,issuerDate,expireDate,jwtType);

        Map<String, Object> claims = payload.toMap();
            /*	iss: jwt签发者
                sub: jwt所面向的用户
                aud: 接收jwt的一方
                exp: jwt的过期时间，这个过期时间必须要大于签发时间
                nbf: 定义在什么时间之前，该jwt都是不可用的.
                iat: jwt的签发时间
                jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击
            */
        return Jwts.builder().header().add(header).and().claims(claims).id(UUID.randomUUID().toString()).issuedAt(issuerDate).expiration(expireDate).subject(subject).signWith(key).compact();
    }

    /**
     * 从jwt中获取 载荷 信息
     *
     * @return payload
     */
    public static Payload getPayloadFromJwt(String jwt) throws ServerException {
        if(jwt==null || jwt.isEmpty()){
            throw new ServerException("Token not Provided");
        }
        Claims claims;
        try{
            claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
        }catch (io.jsonwebtoken.ExpiredJwtException e){
            throw new ServerException("Token Expired");
        }catch (MalformedJwtException | SignatureException e){
            throw new ServerException("Token Invalid");
        }
        return new Payload(claims);
    }
}

