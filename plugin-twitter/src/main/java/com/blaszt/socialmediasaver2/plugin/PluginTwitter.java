package com.blaszt.socialmediasaver2.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginTwitter extends Plugin {
    private static final String HEADER_AUTHORIZATION = "Bearer AAAAAAAAAAAAAAAAAAAAAPYXBAAAAAAACLXUNDekMxqa8h%2F40K4moUkGsoc%3DTYfbDKbT3jJPCEVnMYqilB28NHfOPqkca3qaAxGfsyKCs0wRbw";
    private static final String HEADER_CONNECTION = "keep-alive";
    private static final String HEADER_HOST = "twitter.com", HEADER_ORIGIN = String.format("https://%s", HEADER_HOST);
    private static final String HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20101101 Firefox/81.0";

    private LimitedData mLimitedData;

    protected PluginTwitter(PluginNet pluginNet) {
        super(pluginNet);
        mLimitedData = new LimitedData();
    }

    private static class LimitedData{
        private String authorization;
        private String csrfToken;
        private String guestToken;

        private long mTimeMillis;

        private LimitedData() {

        }

        private void clockUp() {
            mTimeMillis = System.currentTimeMillis();
        }

        private boolean isNotAllInitialized() {
            return authorization == null || csrfToken == null || guestToken == null;
        }

        private boolean timeout() {
            return isNotAllInitialized() || ((System.currentTimeMillis() - mTimeMillis) > (60 * 60 * 1000)); // One hour
        }
    }

    @Override
    protected PluginNet getPluginNet() {
        return super.getPluginNet();
    }

    @Override
    public String getName() {
        return "Twitter";
    }

    @Override
    public String getIcon() {
        return "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";
    }

    @Override
    public String[] getMediaURLs(String url) {
        String[] mediaCollections;
        String response;
        String tweetId;
        JsonObject tweetObject;
        JsonArray mediaList;
        PluginNet.Config config;

        if (getLimitedData().timeout()) {
            getLimitedData().authorization = "Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA";

            config = new PluginNet.Config();
            config.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20101101 Firefox/81.0");
            response = getPluginNet().getResponse(url, config);

            if (config.getCookies().contains("ct0")) {
                getLimitedData().csrfToken = config.getCookies().get("ct0").getValue();
            }

            config = new PluginNet.Config();
            config.setMethod(PluginNet.NetMethod.POST)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20101101 Firefox/81.0")
                    .addHeader("authorization", getLimitedData().authorization)
                    .addHeader("x-csrf-token", getLimitedData().csrfToken);
            response = getPluginNet().getResponse("https://api.twitter.com/1.1/guest/activate.json", config);

            try {
                getLimitedData().guestToken = JsonParser.parseString(response).getAsJsonObject().get("guest_token").getAsString();
            } catch (Exception e) {
                throw new RuntimeException("Response : \n" + response + "\n" + e);
            }
            getLimitedData().clockUp();
        }

        tweetId = getTweetId(url);
        config = new PluginNet.Config();
        config.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20101101 Firefox/81.0")
                .addHeader("authorization", getLimitedData().authorization)
                .addHeader("x-csrf-token", getLimitedData().csrfToken)
                .addHeader("x-guest-token", getLimitedData().guestToken);
        response = getPluginNet().getResponse(String.format("https://api.twitter.com/2/timeline/conversation/%s.json?tweet_mode=extended&include_ext_media_availability=true", tweetId), config);

        tweetObject = JsonParser.parseString(response)
                .getAsJsonObject().get("globalObjects")
                .getAsJsonObject().get("tweets")
                .getAsJsonObject().get(tweetId)
                .getAsJsonObject();

        // There is media
        if (tweetObject.has("extended_entities")) {
            mediaList = tweetObject.get("extended_entities")
                    .getAsJsonObject().get("media")
                    .getAsJsonArray();

            // Media contain video
            if (mediaList.get(0).getAsJsonObject().has("video_info")) {
                int bitrate = 0;
                int bestIndex = 0;
                int tempBitrate;
                mediaList = mediaList.get(0)
                        .getAsJsonObject().get("video_info")
                        .getAsJsonObject().get("variants")
                        .getAsJsonArray();
                for (int index = 0, len = mediaList.size(); index < len; index++) {
                    if (mediaList.get(index).getAsJsonObject().has("bitrate")) {
                        tempBitrate = mediaList.get(index).getAsJsonObject().get("bitrate").getAsInt();
                        bitrate = Math.max(tempBitrate, bitrate);
                        if (bitrate == tempBitrate) {
                            bestIndex = index;
                        }
                    }
                }
                mediaCollections = new String[] {
                        mediaList.get(bestIndex).getAsJsonObject().get("url").getAsString()
                };
            }
            else {
                // Media contains photos
                ArrayList<String> photos = new ArrayList<>();
                for (JsonElement e : mediaList) {
                    photos.add(e.getAsJsonObject().get("media_url").getAsString());
                }
                mediaCollections = photos.toArray(new String[0]);
            }
        }
        else {
            // There is no media
            mediaCollections = new String[0];
        }

        return mediaCollections;
    }

    @Override
    public boolean isURLValid(String url) {
        return url.matches("https?://(www\\.|m\\.|mobile\\.)?twitter\\.com/([a-zA-Z0-9_]+)/status/(\\d+)(\\?s=\\d+)?");
    }

    private LimitedData getLimitedData() {
        return mLimitedData;
    }

    /**
     * Get ids from url.
     *
     * @param url selected tweet.
     * @return [0]: User Id<br>[1]: Tweet Id.
     */
    private String[] getIds(String url) {
        String[] ids;
        Pattern validUrl = Pattern.compile("https?://(www\\.|m\\.|mobile\\.)?twitter\\.com/([a-zA-Z0-9_]+)/status/(\\d+)(\\?s=\\d+)?");
        Matcher match = validUrl.matcher(url);
        if (match.find()) {
            ids = new String[2];
            ids[0] = match.group(2);
            ids[1] = match.group(3);
        } else {
            ids = new String[2];
        }
        return ids;
    }

    private String getTweetId(String url) {
        return getIds(url)[1];
    }
}