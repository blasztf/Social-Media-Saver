package com.blaszt.socialmediasaver2.module.instagram;

import com.blaszt.modulelinker.Responder;
import com.blaszt.modulelinker.helper.HttpLogger;
import com.blaszt.socialmediasaver2.module.instagram.helper.IConstant;
import com.blaszt.socialmediasaver2.module.instagram.helper.IDataUtil;
import com.blaszt.socialmediasaver2.module.instagram.helper.ILogin;
import com.blaszt.socialmediasaver2.module.instagram.helper.INet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleStory {
    private static final String URL_VALIDATOR = "https?://(www\\.)?instagram\\.com/([a-zA-Z0-9_.]+)/?";

    private static final int MEDIA_IMAGE = 1;
    private static final int MEDIA_VIDEO = 2;

    private String username;
    private Responder responder;

    private boolean isLoggedIn = false;

    HttpLogger log = new HttpLogger("in.ms");

    public ModuleStory() {

    }

    public void setup() {
        if (responder == null) {
            responder = Responder.with(null);
        }
        //log.writeLog("Module Story :: Instantiate module story");
    }

    private void loginInstagram() {
        //log.writeLog("Module Story :: Logging in...");
        isLoggedIn = ILogin.with(responder)
                .login("ins.bot", "911930347cc0d3fe3f6990925fd62eae");
    }

    public String[] getStoriesCollections() {
        String[] collections = new String[0];
        List<String> stories = null;

        loginInstagram();
//log.writeLog("Module Story :: Is logged in => " + (isLoggedIn ? "true" : "false"));
        String userPk = getUsernamePk();
//log.writeLog("Module Story :: Log in user pk => " + userPk);
        if (userPk != null) {
            stories = getUserStories(userPk);

            if (stories != null) {

                collections = stories.toArray(new String[0]);
                //log.writeLog("Module Story :: Items => ");
                for (String c : collections) {
                    //log.writeLog("  " + c);
                }
            }
        }

        return collections;
    }

    private List<String> parseReelResponse(String response) {
        int mediaType;
        JsonArray list;

        List<String> result = new ArrayList<>();
        JsonObject root = new JsonParser().parse(response).getAsJsonObject();

        if (!root.get("reel").isJsonNull()) {
            root = root.get("reel").getAsJsonObject();
            list = root.get("items").getAsJsonArray();

            for (JsonElement element : list) {
                mediaType = element.getAsJsonObject().get("media_type").getAsInt();
                switch (mediaType) {
                    case MEDIA_IMAGE:
                        result.add(element.getAsJsonObject()
                                .get("image_versions2").getAsJsonObject()
                                .get("candidates").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("url").getAsString());
                        break;
                    case MEDIA_VIDEO:
                        result.add(element.getAsJsonObject()
                                .get("video_versions").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("url").getAsString());
                        break;
                }
            }
        }

        return result;
    }

    private List<String> getUserStories(String usernamePk) {
        String url = IConstant.API_URL + String.format(IConstant.API_USER_STORY_FEED, usernamePk);
        String response = INet.use(responder).asGet(null, Responder.Options.create()).getResponse(url);

        return parseReelResponse(response);
    }

    private String getUsernamePk() {
        String url = IConstant.API_URL + String.format(IConstant.API_SEARCH_USERNAME, username);
        String response = INet.use(responder).asGet(null, Responder.Options.create()).getResponse(url);

//        System.out.println(response);

        return IDataUtil.getPk(response);
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("\\?.+", "");
    }

    public void setUsername(String url) {
        url = sanitizeUrl(url);
        Pattern pattern = Pattern.compile(URL_VALIDATOR);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            username = matcher.group(2);
        }
        else {
            username = null;
        }
        //log.writeLog("Module Story :: Search " + username + " stories...");
    }

    public boolean isValid(String url) {
        url = sanitizeUrl(url);
        return url.matches(URL_VALIDATOR);
    }
}
