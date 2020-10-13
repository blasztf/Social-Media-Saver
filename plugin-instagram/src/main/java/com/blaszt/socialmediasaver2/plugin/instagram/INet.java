package com.blaszt.socialmediasaver2.plugin.instagram;

import com.blaszt.socialmediasaver2.plugin.PluginNet;

public class INet {

    public static void asGet(PluginNet.Config config, ILogin.Payload payload) {
        config.setMethod(PluginNet.NetMethod.GET)
                .addHeader("Connection", "close")
                .addHeader("Accept", "*/*")
                .addHeader("Cookie2", "$Version=1")
                .addHeader("Accept-Language", "en-US")
                .addHeader("User-Agent", IConstant.USER_AGENT)
                .setData(payload.build());
    }

    public static void asPost(PluginNet.Config config, ILogin.Payload payload) {
        config.setMethod(PluginNet.NetMethod.POST)
                .addHeader("Connection", "close")
                .addHeader("Accept", "*/*")
                .addHeader("Cookie2", "$Version=1")
                .addHeader("Accept-Language", "en-US")
                .addHeader("X-IG-Capabilities", "3boBAA==")
                .addHeader("X-IG-Connection-Type", "WIFI")
                .addHeader("X-IG-Connection-Speed", "-1kbps")
                .addHeader("X-IG-App-ID", "567067343352427")
                .addHeader("User-Agent", IConstant.USER_AGENT)
                .setData(payload.build());
    }

    public static void asGet(PluginNet.Config config) {
        asGet(config, null);
    }
}
