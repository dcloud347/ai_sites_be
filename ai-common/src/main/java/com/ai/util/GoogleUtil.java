package com.ai.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;

public class GoogleUtil {
    private static final String googleClientId = "442601140790-3ouk3tulslkf8mpvv0dhv688porktg04.apps.googleusercontent.com";

    public static String get_email(String token){
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null){
                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload.getEmail();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }



}
