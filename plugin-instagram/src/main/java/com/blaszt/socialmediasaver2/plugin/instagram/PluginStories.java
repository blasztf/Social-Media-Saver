package com.blaszt.socialmediasaver2.plugin.instagram;

import com.blaszt.socialmediasaver2.plugin.Plugin;
import com.blaszt.socialmediasaver2.plugin.PluginNet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginStories extends Plugin {
    private static final String URL_VALIDATOR = "https?://(www\\.)?instagram\\.com/([a-zA-Z0-9_.]+)/?";

    private static final int MEDIA_IMAGE = 1;
    private static final int MEDIA_VIDEO = 2;

    public PluginStories(PluginNet pluginNet) {
        super(pluginNet);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String[] getMediaURLs(String url) {
        return getStoriesCollections(url);
    }

    @Override
    public boolean isURLValid(String url) {
        url = sanitizeUrl(url);
        return url.matches(URL_VALIDATOR);
    }

    @Override
    protected PluginNet getPluginNet() {
        return super.getPluginNet();
    }

    public String[] getStoriesCollections(String url) {
        String[] collections = new String[0];
        List<String> stories;
        String userPk;
        String username = findUsername(url);

        if (ILogin.with(getPluginNet()).login("ins.bot", "911930347cc0d3fe3f6990925fd62eae")) {
            userPk = getUsernamePk(username);
            if (userPk != null) {
                stories = getUserStories(userPk);
                if (stories != null) {
                    collections = stories.toArray(new String[0]);
                }
            }
        }

        return collections;
    }

    private List<String> parseReelResponse(String response) {
        int mediaType;
        JsonArray list;

        List<String> result = new ArrayList<>();
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

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
        PluginNet.Config config = new PluginNet.Config();
        INet.asGet(config);

        String url = IConstant.API_URL + String.format(IConstant.API_USER_STORY_FEED, usernamePk);
        String response = getPluginNet().getResponse(url, config);

        return parseReelResponse(response);
    }

    private String getUsernamePk(String username) {
        PluginNet.Config config = new PluginNet.Config();
        INet.asGet(config);

        String url = IConstant.API_URL + String.format(IConstant.API_SEARCH_USERNAME, username);
        String response = getPluginNet().getResponse(url, config);

        return IDataUtil.getPk(response);
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("\\?.+", "");
    }

    public String findUsername(String url) {
        String username;
        url = sanitizeUrl(url);
        Pattern pattern = Pattern.compile(URL_VALIDATOR);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            username = matcher.group(2);
        }
        else {
            username = null;
        }

        return username;
    }
}
