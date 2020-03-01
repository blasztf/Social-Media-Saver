package com.blaszt.socialmediasaver2.module.instagram;

//import org.brunocvcunha.instagram4j.Instagram4j;
//import org.brunocvcunha.instagram4j.requests.InstagramGetUserReelMediaFeedRequest;
//import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
//import org.brunocvcunha.instagram4j.requests.payload.ImageMeta;
//import org.brunocvcunha.instagram4j.requests.payload.InstagramSearchUsernameResult;
//import org.brunocvcunha.instagram4j.requests.payload.InstagramStoryItem;
//import org.brunocvcunha.instagram4j.requests.payload.InstagramUserReelMediaFeedResult;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleInstaStoryDep {

//    private static final String URL_VALIDATOR = "https?://instagram.com/([a-zA-Z0-9_.]+)";
//
//    private static final int MEDIA_IMAGE = 1;
//    private static final int MEDIA_VIDEO = 2;
//
//    private String username;
//    private Instagram4j instagram;
//
//    public ModuleInstaStoryDep(String url) throws URLNotValid {
//        url = sanitizeUrl(url);
//        if (!isValid(url)) {
//            throw new URLNotValid(url);
//        }
//        else {
//            setUsername(url);
//        }
//    }
//
//    ModuleInstaStoryDep() {
//
//    }
//
//    private void loginInstagram() {
//        instagram = Instagram4j.builder()
//                .username("ins.bot")
//                .password("911930347cc0d3fe3f6990925fd62eae")
//                .build();
//        instagram.setup();
//
//        try {
//            instagram.login();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    String[] getStoriesCollections() {
//        String[] collections = new String[0];
//        List<InstagramStoryItem> stories = null;
//
//        loginInstagram();
//
//        long userPk = getUsernamePk();
//
//        if (userPk != -1) {
//            stories = getUserStories(userPk);
//
//            if (stories != null) {
//
//                collections = new String[stories.size()];
//
//                for (int i = 0; i < stories.size(); i++) {
//
//                    switch (stories.get(i).getMedia_type()) {
//                        case MEDIA_IMAGE:
//                            collections[i] = chooseBestUrlCandidate(stories.get(i).getImage_versions2().getCandidates());
//                            break;
//                        case MEDIA_VIDEO:
//                            collections[i] = chooseBestUrlCandidate(stories.get(i).getVideo_versions());
//                            break;
//                    }
//                }
//            }
//        }
//
//        return collections;
//    }
//
//    private String chooseBestUrlCandidate(List<ImageMeta> list) {
//        ImageMeta bestImageMeta = null;
//
//        for (ImageMeta imageMeta : list) {
//            if (bestImageMeta == null) {
//                bestImageMeta = imageMeta;
//            }
//            else {
//                if ((bestImageMeta.getWidth() + bestImageMeta.getHeight()) < (imageMeta.getWidth() + imageMeta.getHeight())) {
//                    bestImageMeta = imageMeta;
//                }
//            }
//        }
//
//        return bestImageMeta.getUrl();
//    }
//
//    private List<InstagramStoryItem> getUserStories(long userPk) {
//        InstagramUserReelMediaFeedResult result;
//
//        List<InstagramStoryItem> items = null;
//
//        try {
//            result = instagram.sendRequest(new InstagramGetUserReelMediaFeedRequest(userPk));
//            if ("ok".equals(result.getStatus())) {
//                items = result.getItems();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return items;
//    }
//
//    private long getUsernamePk() {
//        long usernamePk = -1;
//
//        InstagramSearchUsernameResult searchUsernameResult;
//
//        try {
//            searchUsernameResult = instagram.sendRequest(new InstagramSearchUsernameRequest(username));
//            if ("ok".equals(searchUsernameResult.getStatus())) {
//                usernamePk = searchUsernameResult.getUser().getPk();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return usernamePk;
//    }
//
//    private String sanitizeUrl(String url) {
//        return url.replaceAll("\\?.+", "");
//    }
//
//    void setUsername(String url) {
//        url = sanitizeUrl(url);
//        Pattern pattern = Pattern.compile(URL_VALIDATOR);
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            username = matcher.group(1);
//        }
//        else {
//            username = null;
//        }
//    }
//
//    boolean isValid(String url) {
//        url = sanitizeUrl(url);
//        return url.matches(URL_VALIDATOR);
//    }
//
//    public static class URLNotValid extends Exception {
//
//        URLNotValid(String message) {
//            super("URL not valid : " + message);
//        }
//
//    }
}
