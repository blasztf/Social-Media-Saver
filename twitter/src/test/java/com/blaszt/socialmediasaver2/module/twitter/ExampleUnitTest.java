package com.blaszt.socialmediasaver2.module.twitter;

import com.blaszt.modulelinker.Responder;
import com.blaszt.modulelinker.helper.HttpLogger;

import org.junit.Test;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final String HEADER_AUTHORIZATION = "Bearer AAAAAAAAAAAAAAAAAAAAAPYXBAAAAAAACLXUNDekMxqa8h%2F40K4moUkGsoc%3DTYfbDKbT3jJPCEVnMYqilB28NHfOPqkca3qaAxGfsyKCs0wRbw";
    private static final String HEADER_CONNECTION = "keep-alive";
    private static final String HEADER_HOST = "api.twitter.com";
    //    private static final String HEADER_USER_AGENT = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)";
    private static final String HEADER_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.133 Safari/537.36";
    private static final String HEADER_TE = "Trailers";
    private static final String HEADER_HOST_ = "twitter.com";
    private static final String HEADER_UPGRADE_INSECURE_REQUESTS = "1";
    private static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String HEADER_ACCEPT_ENCODING = "gzip, deflate, br";
    private static final String HEADER_ACCEPT_LANGUAGE = "id,en-US;q=0.7,en;q=0.3";

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testTwitter() {
        String url = "https://twitter.com/photojiaenic/status/1231563056108339205?s=19";

        HashMap<String, String> headers = new HashMap<>();
//        headers.put("Connection", HEADER_CONNECTION);
        headers.put("User-Agent", "");
//        headers.put("TE", HEADER_TE);
//        headers.put("Upgrade-Insecure-Requests", HEADER_UPGRADE_INSECURE_REQUESTS);
//        headers.put("Host", HEADER_HOST_);
//        headers.put("Origin", "https://twitter.com");
//        headers.put("Accept", HEADER_ACCEPT);
//        headers.put("Accept-Encoding", HEADER_ACCEPT_ENCODING);
//        headers.put("Accept-Language", HEADER_ACCEPT_LANGUAGE);
//        headers.put("Referer", url);

        Responder.Options options = Responder.Options.create();

        options.reset()
                .requestResponseCookies(true)
                .setRequestHeaders(headers);
//        System.out.println("Collecting cookie...");
        String response = Responder.with(null).apply(options).getResponse(url);
        System.out.println(response);
        for (HttpCookie cookie : options.getResponseCookies()) {
            System.out.println(cookie.getName() + " : " + cookie.getValue());
        }
        System.out.println("Done");
//        String config = String.format("https://api.twitter.com/2/timeline/conversation/%s.json", url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?")));
//        System.out.println("Config : "  + config);
//        System.out.println(Responder.with(null).apply(options).getResponse(config));
    }
}