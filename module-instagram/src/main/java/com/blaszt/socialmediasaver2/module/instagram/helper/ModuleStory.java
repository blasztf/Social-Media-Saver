package com.blaszt.socialmediasaver2.module.instagram.helper;

import com.blaszt.socialmediasaver2.module.instagram.api.IConstant;
import com.blaszt.socialmediasaver2.module.instagram.api.IDataUtil;
import com.blaszt.socialmediasaver2.module.instagram.api.ILogin;
import com.blaszt.socialmediasaver2.module.instagram.api.INet;
import com.blaszt.socialmediasaver2.module_base.Base;
import com.blaszt.socialmediasaver2.module_base.ModuleNotInjected;
import com.blaszt.socialmediasaver2.module_base.Responder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleStory extends Base {
    private static final String URL_VALIDATOR = "https?://(www\\.)?instagram\\.com/([a-zA-Z0-9_.]+)/?";

    private static final int MEDIA_IMAGE = 1;
    private static final int MEDIA_VIDEO = 2;

    private String username;

    boolean isLoggedIn = false;


    public ModuleStory() {

    }

    @Override
    public String getBaseDir() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    protected String getEncodedImage() {
        return null;
    }

    private void loginInstagram() {
        //log.writeLog("Module Story :: Logging in...");
        isLoggedIn = ILogin.with(getResponder())
                .login("ins.bot", "911930347cc0d3fe3f6990925fd62eae");
    }

    @Override
    public String[] findMediaURL(String url) throws ModuleNotInjected {
        super.findMediaURL(url);
        setUsername(url);
        return getStoriesCollections();
    }

    private String[] getStoriesCollections() {
        String[] collections = new String[0];
        List<String> stories;

        loginInstagram();
//log.writeLog("Module Story :: Is logged in => " + (isLoggedIn ? "true" : "false"));
        String userPk = getUsernamePk();
//log.writeLog("Module Story :: Log in user pk => " + userPk);
        if (userPk != null) {
            stories = getUserStories(userPk);

            if (stories != null) {

                collections = stories.toArray(new String[0]);
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
        String response = INet.use(getResponder()).asGet(null, Responder.Options.create()).getResponse(url);

        return parseReelResponse(response);
    }

    private String getUsernamePk() {
        String url = IConstant.API_URL + String.format(IConstant.API_SEARCH_USERNAME, username);
        String response = INet.use(getResponder()).asGet(null, Responder.Options.create()).getResponse(url);

//        System.out.println(response);

        return IDataUtil.getPk(response);
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("\\?.+", "");
    }

    private void setUsername(String url) {
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

    @Override
    public boolean isValid(String url) {
        url = sanitizeUrl(url);
        return url.matches(URL_VALIDATOR);
    }

    @Override
    public boolean check(String url) {
        return isValid(url);
    }
}
