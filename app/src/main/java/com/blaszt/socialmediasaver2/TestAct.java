package com.blaszt.socialmediasaver2;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blaszt.modulelinker.Responder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestAct extends Activity {
    private EditText editTextU;
    private Button buttonU;
    private TextView textViewU;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test);
        editTextU = findViewById(R.id.editTextU);
        buttonU = findViewById(R.id.buttonU);
        textViewU = findViewById(R.id.textViewU);

        buttonU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextU.getText().toString();
//                String response = Responder.with(null).getResponse(url);
                editTextU.setText("Loading...");
                new Task(new Task.OnCompleteListener() {
                    @Override
                    public void onComplete(String response) {
                        textViewU.setText(response);
                        editTextU.setText("");
                    }
                }).execute(url);
            }
        });

    }

    private static class Task extends AsyncTask<String, String, String> {
        private String csrfToken = null;
        private String guestToken = null;

        private int ratelimitLimit = 0;
        private int ratelimitRemaining = 0;
        private long ratelimitReset = 0;

        private static final String HEADER_AUTHORIZATION = "Bearer AAAAAAAAAAAAAAAAAAAAAPYXBAAAAAAACLXUNDekMxqa8h%2F40K4moUkGsoc%3DTYfbDKbT3jJPCEVnMYqilB28NHfOPqkca3qaAxGfsyKCs0wRbw";
        private static final String HEADER_CONNECTION = "keep-alive";
        private static final String HEADER_HOST = "api.twitter.com";
            private static final String HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
//        private static final String HEADER_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.133 Safari/537.36";
        private static final String HEADER_TE = "Trailers";
        private static final String HEADER_HOST_ = "twitter.com";
        private static final String HEADER_UPGRADE_INSECURE_REQUESTS = "1";
        private static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        private static final String HEADER_ACCEPT_ENCODING = "gzip, deflate, br";
        private static final String HEADER_ACCEPT_LANGUAGE = "id,en-US;q=0.7,en;q=0.3";

        private OnCompleteListener mListener;

        interface OnCompleteListener {
            void onComplete(String response) ;
        }

        public Task(OnCompleteListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mListener.onComplete(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String[] mediaCollections = new String[0];
            String response;
            Responder.Options options = Responder.Options.create();
String b = "";
//return Responder.with(null).getResponse(url);
            try {
                response = requestTweet(url, options);
                b += "STATUS CODE\n " + options.getErrorCode() + "\n\n";
                b += "COOKIES\n ";
                for (HttpCookie cookie : options.getResponseCookies()) {
                    b += cookie.getName() + " => " + cookie.getValue();
                    b += "\n ";
                }
                b += "\n\n";
                b += "CSRF_TOKEN\n " + findCSRFToken(options.getResponseCookies()) + "\n\n";
                b += "RESPONSE\n " + (response != null ? "Responsive" : "Unresponsive")+ "\n\n";
//            mediaCollections = new String[]{"state code : 0", "status code : " + options.getErrorCode(), "response : " + response};
                if (isTweetContainVideo(response)) {
                    response = null;
                    if (!isGuestTokenStillValid()) {
                        b += "COOKIES\n";
                        for (HttpCookie cookie : options.getResponseCookies()) {
                            // found x-csrf-token
//                            if ("ct0".equalsIgnoreCase(cookie.getName())) {
//                                return cookie.getValue();
//                            }
                            b += " " + cookie.getName() + "\n";
                        }
                        response = requestGuestToken(options.getResponseCookies(), options);
                        b+= "GUEST TOKEN\n " + response + "\n\n";
                    }
                    response = requestDataConfig(response, url, options);
                    b += "DATA CONFIG\n " + response +"\n\n";
                    response = findPlaybackUrl(response, options);
                    b += "PLAYBACK URL\n " + response + "\n\n";

                    return b;

//                    if (response != null) {
//                        mediaCollections = new String[]{response};
//                    }

                } else if (isTweetContainPhoto(response)) {
                    mediaCollections = getPhotoUrls(response);
                }
            }
            catch (NullPointerException e) {
                // ignore
//            mediaCollections = new String[]{"state code : 2", "status code : " + options.getErrorCode(), "response : null"};
            }

            return b;
        }

        private String joinString(String[] strings) {
            String a = "";

            for (String str : strings) {
                a += str + "\n";
            }

            return a;
        }

        private boolean isTweetContainVideo(String response) {
            return response != null && response.contains("class=\"PlayableMedia");
        }

        private boolean isTweetContainPhoto(String response) {
            return response != null && response.contains("class=\"AdaptiveMedia-photoContainer");
        }

        private boolean isGuestTokenStillValid() {
            boolean valid = false;

            if (ratelimitRemaining == -1 || ratelimitRemaining > 0) {
                valid = true;
            } else {
                if (ratelimitReset == -1 || (System.currentTimeMillis() / 1000L) < ratelimitReset) {
                    valid = true;
                }
            }

            return valid;
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
                ids = new String[0];
            }
            return ids;
        }

        private String findGuestToken(String response) {
            if (response == null) return null;
            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
            if (root.has("guest_token")) {
                return root.get("guest_token").getAsString();
            } else {
                return null;
            }
        }

        private String findCSRFToken(@Nullable List<HttpCookie> cookies) {
            if (cookies == null) return null;
            for (HttpCookie cookie : cookies) {
                // found x-csrf-token
                if ("ct0".equalsIgnoreCase(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }

        private String findPlaybackUrl(String response, Responder.Options options) {
            if (response != null) {
                ratelimitLimit = Integer.parseInt(options.getResponseHeaders().get("x-rate-limit-limit"));
                ratelimitRemaining = Integer.parseInt(options.getResponseHeaders().get("x-rate-limit-remaining"));
                ratelimitReset = Long.parseLong(options.getResponseHeaders().get("x-rate-limit-reset"));

                JsonObject dataConfig, track;
                String playbackUrl = null;

                try {
                    dataConfig = new JsonParser().parse(response).getAsJsonObject();
                } catch (JsonParseException e) {
                    dataConfig = null;
                }

                if (dataConfig != null) {
                    track = dataConfig.getAsJsonObject("track");
                    if (track != null) {
                        playbackUrl = track.get("playbackUrl").getAsString();
                    }
                }

                return playbackUrl;
            }
            else {
                return null;
            }
        }

        private String[] getPhotoUrls(String response) {
            ArrayList<String> list = new ArrayList<>();
            int i = 0;

            while ((i = response.indexOf("AdaptiveMedia-photoContainer", i)) != -1) {
                i = response.indexOf("data-image-url=\"", i) + 16;
                list.add(response.substring(i, response.indexOf('"', i)));
            }
            return list.toArray(new String[list.size()]);
        }

        private String requestTweet(String url, Responder.Options options) {
            HashMap<String, String> headers = new HashMap<>();
//            headers.put("Connection", HEADER_CONNECTION);
            headers.put("User-Agent", HEADER_USER_AGENT);
            headers.put("TE", HEADER_TE);
            headers.put("Upgrade-Insecure-Requests", HEADER_UPGRADE_INSECURE_REQUESTS);
//            headers.put("Host", HEADER_HOST);
            headers.put("Accept", HEADER_ACCEPT);
//        headers.put("Accept-Encoding", HEADER_ACCEPT_ENCODING);
            headers.put("Accept-Language", HEADER_ACCEPT_LANGUAGE);
//            headers.put("Referer", "https://www.google.com/");

            options.reset()
                    .requestResponseCookies(true)
                    .setRequestHeaders(headers);

            return Responder.with(null).apply(options).getResponse(url);
        }

        private String requestDataConfig(String response, String url, Responder.Options options) {
            final String tweetId = getIds(url)[1];

            if (response != null) {
                guestToken = findGuestToken(response);
            }

            if (guestToken != null) {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", HEADER_AUTHORIZATION);
                headers.put("Connection", HEADER_CONNECTION);
                headers.put("Host", HEADER_HOST);
                headers.put("User-Agent", HEADER_USER_AGENT);
                headers.put("Referer", url);
                headers.put("x-csrf-token", csrfToken);
                headers.put("x-guest-token", guestToken);

                options.reset()
                        .setRequestHeaders(headers)
                        .requestResponseHeaders(true);

                return Responder.with(null).apply(options).getResponse(String.format("https://api.twitter.com/1.1/videos/tweet/config/%s.json", tweetId));
            } else {
                return null;
            }
        }

        private String requestGuestToken(List<HttpCookie> cookies, Responder.Options options) {
            csrfToken = findCSRFToken(cookies);

            if (csrfToken != null) {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", HEADER_AUTHORIZATION);
                headers.put("Connection", HEADER_CONNECTION);
                headers.put("Host", HEADER_HOST);
                headers.put("User-Agent", HEADER_USER_AGENT);
                headers.put("x-csrf-token", csrfToken);

                options.reset()
                        .requestResponseCookies(true)
                        .setMethod(Responder.Options.Method.POST)
                        .setRequestHeaders(headers);

                return Responder.with(null).apply(options).getResponse("https://api.twitter.com/1.1/guest/activate.json");
            } else {
                return null;
            }
        }
    }
}
