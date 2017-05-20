package com.philip.SimultaneousInterpretation;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;


public class ConversationManager {
    private final static String TAG = "ConversationManager";

    private static ConversationManager sConvInst;

    private final static String SENDER_ID = "abc";

    private final static String AUTH_MSG = "Basic dGVzdDAwMTpwd2Q="; //"test001:pwd"; Auth message
    private final static String SERVER_URL = "http://localhost:5000/api/v1.0/synctrans/conversation";
    private final static String SERVER_URL_GET = "http://localhost:5000/api/v1.0/synctrans/";

    private final static int POST_FLG = 0;
    private final static int GET_FLG = 1;

    private ConversationManager() {
    }

    public static ConversationManager getInstance() {
        if (sConvInst == null) {
            sConvInst = new ConversationManager();

        }
        return sConvInst;
    }

    public String postByUrl(String str, String lang) {

        String url = SERVER_URL;
        String currMsg = "{\"sender_id\":\"" + SENDER_ID + "\" ,\"message\":\"" + str + "\" ,\"lang\":\"" + lang + "\"}";
        Log.d(TAG, "postByUrl: " + currMsg);
        JSONObject res = http(url, currMsg, POST_FLG);
        if (res == null || res.get("message") == null) return "FATAL_ERROR";
        return res.get("message").toString();
    }

    public String getByUrl(String lang, String senderId) {

        String url = SERVER_URL_GET + senderId + "/" + lang;
        String currMsg = "";
        JSONObject res = http(url, currMsg, GET_FLG);
        if (res != null && res.get("message") != null)
            return res.get("message").toString();
        else
            return "";
    }

    private JSONObject http(String url, String body, int flag) {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse result;
            HttpRequestBase request;
            StringEntity params = new StringEntity(body);
            if (flag == POST_FLG) {
                HttpPost request0 = new HttpPost(url);
                request0.setEntity(params);
                request = request0;
            } else {
                HttpGet request0 = new HttpGet(url);
                request = request0;
            }
            Log.d("ConversationManager: ", "http: " + request.toString());
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", AUTH_MSG);

            result = httpClient.execute(request);


            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            try {
                JSONParser parser = new JSONParser();
                Object resultObject = parser.parse(json);

                if (resultObject instanceof JSONArray) {
                    JSONArray array = (JSONArray) resultObject;
                    for (Object object : array) {
                        JSONObject obj = (JSONObject) object;
                        if (obj != null) {
                            if (obj.get("sender_id") != null) Log.d("arr res(sender_id): ", obj.get("sender_id").toString());
                            if (obj.get("sender_id") != null) Log.d("arr res(message): ", obj.get("message").toString());
                        }
                    }
                    return (JSONObject)array.get(0);

                } else if (resultObject instanceof JSONObject) {
                    JSONObject obj = (JSONObject) resultObject;
                    if (obj.get("sender_id") != null) Log.d("arr res(sender_id): ", obj.get("sender_id").toString());
                    if (obj.get("sender_id") != null) Log.d("arr res(message): ", obj.get("message").toString());

                    return (JSONObject)resultObject;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
