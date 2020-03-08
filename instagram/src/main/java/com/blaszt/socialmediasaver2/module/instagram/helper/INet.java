package com.blaszt.socialmediasaver2.module.instagram.helper;

import com.blaszt.modulelinker.Responder;

import java.util.HashMap;
import java.util.Map;

public class INet {

//    private Responder.Options opts;
    private Responder responder;

    public static synchronized INet use(Responder responder) {
        return new INet(responder);
    }

    private INet(Responder responder) {
        this.responder = responder;
    }

    public Responder asGet(Map<String, String> data, Responder.Options opts) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Accept", "*/*");
        headers.put("Cookie2", "$Version=1");
        headers.put("Accept-Language", "en-US");
        headers.put("User-Agent", IConstant.USER_AGENT);

        opts
                .setMethod(Responder.Options.Method.GET)
                .setRequestHeaders(headers);

        if (data != null) {
            opts.setData(data);
        }
        else opts.setData(null);

        return responder.apply(opts);
    }

    public Responder asPost(Map<String, String> data, Responder.Options opts) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Accept", "*/*");
        headers.put("Cookie2", "$Version=1");
        headers.put("Accept-Language", "en-US");
        headers.put("X-IG-Capabilities", "3boBAA==");
        headers.put("X-IG-Connection-Type", "WIFI");
        headers.put("X-IG-Connection-Speed", "-1kbps");
        headers.put("X-IG-App-ID", "567067343352427");
        headers.put("User-Agent", IConstant.USER_AGENT);

        opts
                .setMethod(Responder.Options.Method.POST)
                .setRequestHeaders(headers);

        if (data != null) {
            opts.setData(data);
        }
        else opts.setData(null);

        return responder.apply(opts);
    }
}
