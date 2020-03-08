package com.blaszt.socialmediasaver2.module.instagram;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testILogin() {
//        Responder resp = Responder.with(null);

//        boolean res = ILogin.with(resp)
//                .login("ins.bot", "911930347cc0d3fe3f6990925fd62eae");

        ModuleStory ms = new ModuleStory();
        ms.setup();
        ms.setUsername("https://instagram.com/doodlyz1?abc");
        String[] coll = ms.getStoriesCollections();
        for (String c : coll) {
            System.out.println(c);
        }
//        System.out.println(res);
    }

//    Instagram4j instagram;
//
//    @Test
//    public void testInsta4J() {
//        InstagramUserReelMediaFeedResult result;
//        InstagramGetReelsTrayFeedResult result1;
//
//        instagram = Instagram4j.builder()
//                .username("ins.bot")
//                .password("911930347cc0d3fe3f6990925fd62eae")
//                .build();
//        instagram.setup();
//
//        try {
//            instagram.login();
//        }
//        catch (ClientProtocolException e) {
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (instagram.isLoggedIn()) {
//            System.out.println("Insta logged in!");
//
//            try {
//                long uPK = i4jGetUserPK("doodlyz1");
//
//                if (uPK != 0L) {
//                    System.out.println("User PK : " + uPK);
//                    result = instagram.sendRequest(new InstagramGetUserReelMediaFeedRequest(uPK));
//                    System.out.println("Reel status : " + result.getStatus());
////                    result1 = instagram.sendRequest(new InstagramGetReelsTrayFeedRequest());
////                    System.out.println("Deep link uri :" + result.items.get(0).getStory_cta().get(0).getLinks().get(0).getDeeplinkUri());
////                    System.out.println("Web uri : " + result.items.get(0).getStory_cta().get(0).getLinks().get(0).getWebUri());
////                    System.out.println("Adaction : " + result.items.get(0).getAdaction());
//                    printAttribute(result);
//                }
//                else {
//                    System.err.println("Failed to obtain user");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            System.err.println("Can't login!");
//        }
//    }
//
//    private void printAttribute(InstagramUserReelMediaFeedResult res) {
//        int i = 0;
//        for (InstagramStoryItem item : res.getItems()) {
//            System.out.println("Item [" + (i++) + "]:");
//            System.out.println(" " + item.toString());
//            if ("IMAGE".equals(i4jGetMediaType(item.getMedia_type()))) {
//                System.out.println(" Story url : " + i4jChooseBestImageCandidate(item.getImage_versions2()));
//            }
//            else {
//                System.out.println(" Story url : " + i4jChooseBestCandidate(item.getVideo_versions()));
//            }
////            for (Map.Entry<String, Object> entry : item.getAttribution().entrySet()) {
////                System.out.println("  " + entry.getKey());
////            }
//        }
//    }
//
//    private void stringify(List<InstagramStoryItem> list) {
//        for (InstagramStoryItem item : list) {
//            System.out.println(item.getAdaction());
//            System.out.println(item.getLink_text());
//        }
//    }
//
//    private long i4jGetUserPK(String user) {
//        long userPK = 0;
//
//        InstagramSearchUsernameResult result;
//        InstagramReelsTrayFeedResult res; res.
//
//        try {
//            result = instagram.sendRequest(new InstagramSearchUsernameRequest(user));
//            InstagramUser iUser = result.getUser();
//            System.out.println("Search user status : " + result.getStatus());
//            userPK = iUser.getPk();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return userPK;
//    }
//
//    private String i4jGetMediaType(int mediaType) {
//        switch (mediaType) {
//            case 1:
//                return "IMAGE";
//            case 2:
//                return "VIDEO";
//                default:
//                    return "UNKNOWN";
//        }
//    }
//
//    private String i4jChooseBestImageCandidate(ImageVersions list) {
//
//        return i4jChooseBestCandidate(list.getCandidates());
//    }
//    private String i4jChooseBestCandidate(List<ImageMeta> list) {
//        ImageMeta timMeta = null;
//        for (ImageMeta imMeta : list) {
//            if (timMeta == null) {
//                timMeta = imMeta;
//            }
//            else {
//                if ((timMeta.getHeight() + timMeta.getWidth()) < (imMeta.getHeight() + imMeta.getWidth())) {
//                    timMeta = imMeta;
//                }
//            }
//        }
//
//        return timMeta.getUrl();
//    }
}