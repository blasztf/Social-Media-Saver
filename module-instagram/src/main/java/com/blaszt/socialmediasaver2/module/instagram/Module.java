package com.blaszt.socialmediasaver2.module.instagram;


import com.blaszt.socialmediasaver2.module.instagram.helper.ModuleStory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class Module extends com.blaszt.socialmediasaver2.module_base.Base {

    private ModuleStory moduleInstaStory;
//    HttpLogger log = n ew HttpLogger("in");

    public Module() {
        moduleInstaStory = new ModuleStory();
    }

    @Override
    public String getBaseDir() {
        return getName();
    }

    @Override
    public String getName() {
        return "Instagram";
    }

    @Override
    protected String getEncodedImage() {
        return "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";
    }

    @Override
    public String[] findMediaURL(String url) {
        String[] mediaCollections;

        if (moduleInstaStory.isValid(url)) {
//            log.writeLog("Module :: It is stories!");
            mediaCollections = moduleInstaStory.findMediaURL(url);
        }
        else {
//            log.writeLog("Module :: It is not stories!");
            try {
                String response = getResponder().getResponse(appendQuery(url, "__a=1"));
                JsonObject container = new JsonParser().parse(response).getAsJsonObject();
                container = container.get("graphql").getAsJsonObject();
                container = container.get("shortcode_media").getAsJsonObject();

                mediaCollections = getMediaCollections(container);
            } catch (NullPointerException e) {
                mediaCollections = new String[0];
            }
        }

        return mediaCollections;
    }

    @Override
    public boolean check(String url) {
        return isValid(url);
    }

    @Override
    protected boolean isValid(String url) {
        return url.matches("https?://(www\\.)?instagram\\.com/p/[a-zA-Z0-9_\\-]{11,}/?.*") || moduleInstaStory.check(url);
    }

    private String appendQuery(String uriString, String queryString) {
        String newQuery;
        URI uri = URI.create(uriString);
        newQuery = uri.getQuery();
        newQuery = newQuery == null ? queryString : (newQuery + "&" + queryString);
        try {
            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
            return uri.toString();
        } catch (URISyntaxException e) {
            return uriString + "?&" + queryString;
        }
    }

    private String[] getMediaCollections(JsonObject container) {
        String[] collections;
        // just one photo on the post
        if (!container.has("edge_sidecar_to_children")) {
            // is video
            if (container.has("is_video") && container.get("is_video").getAsBoolean()) {
                collections = new String[]{getMediaVideo(container)};
            }
            // is image
            else {
                collections = new String[]{getMediaPhoto(container)};
            }
        }
        // more than one photo / video on the post
        else {
            JsonArray array;
            JsonObject node;
            container = container.getAsJsonObject("edge_sidecar_to_children");
            if (container != null && (array = container.getAsJsonArray("edges")) != null) {
                collections = new String[array.size()];
                String media;
                for (int i = 0, l = array.size(); i < l; i++) {
                    node = array.get(i).getAsJsonObject().getAsJsonObject("node");
                    if (node.has("is_video") && node.get("is_video").getAsBoolean()) {
                        // is video
                        media = getMediaVideo(node);
                    }
                    else {
                        // is photo
                        media = getMediaPhoto(node);
                    }
                    // add media to collections
                    collections[i] = media;
                }
            } else {
                collections = new String[0];
            }
        }
        return collections;
    }

    private String getMediaPhoto(JsonObject container) {
        return container.get("display_url").getAsString();
    }

    private String getMediaVideo(JsonObject container) {
        return container.get("video_url").getAsString();
    }
}
