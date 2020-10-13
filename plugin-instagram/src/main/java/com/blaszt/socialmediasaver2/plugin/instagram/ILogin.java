package com.blaszt.socialmediasaver2.plugin.instagram;

import com.blaszt.socialmediasaver2.plugin.PluginNet;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.util.Map;

public class ILogin {

    private PluginNet mPluginNet;

    public static synchronized ILogin with(PluginNet pluginNet) {
        return new ILogin(pluginNet);
    }

    private ILogin(PluginNet pluginNet) {
        mPluginNet = pluginNet;
    }

    public boolean login(String username, String passwordOrCookie) {
        String uuid = IUtil.getGeneric().generateUuid(true);
        String csrfToken = getCsrfToken();

        String url;
        String response;
        String usernamePk;

        String status;
        boolean isLoggedIn = false;

        LoginPayload loginPayload = new LoginPayload(username, passwordOrCookie, uuid, csrfToken);

        PluginNet.Config config = new PluginNet.Config();
        INet.asPost(config, loginPayload);

        url = IConstant.API_URL + IConstant.API_LOGIN;
        response = mPluginNet.getResponse(url, config);

        status = IDataUtil.getStatus(response);

        if ("ok".equalsIgnoreCase(status)) {
            isLoggedIn = true;
        }

        // Simulate user log in.
        usernamePk = IDataUtil.getPk(response);

        SyncFeaturesPayload syncFeaturesPayload = new SyncFeaturesPayload(uuid, usernamePk, csrfToken);

        INet.asPost(config, syncFeaturesPayload);
        url = IConstant.API_URL + IConstant.API_SYNC_FEATURES;
        response = mPluginNet.getResponse(url, config);

        INet.asGet(config);
        url = IConstant.API_URL + IConstant.API_AUTO_COMPLETE_USER_LIST;
        response = mPluginNet.getResponse(url, config);

        INet.asGet(config);
        url = IConstant.API_URL + IConstant.API_GET_INBOX;
        response = mPluginNet.getResponse(url, config);

        INet.asGet(config);
        url = IConstant.API_URL + IConstant.API_GET_RECENT_ACTIVITY;
        response = mPluginNet.getResponse(url, config);

        ///////////////////
        
        return isLoggedIn;
    }

    private String getCsrfToken() {
        PluginNet.Config config = new PluginNet.Config();
        mPluginNet.getResponse(IConstant.API_URL + String.format(IConstant.API_FETCH_HEADERS, IUtil.getGeneric().generateUuid(false)), config);

        for (Map.Entry<String, HttpCookie> entryCookie : config.getCookies()) {
            if ("csrftoken".equalsIgnoreCase(entryCookie.getKey())) {
                return entryCookie.getValue().getValue();
            }
        }

        return null;
    }

    class Payload {

        PluginNet.NetData build() {
            String key;
            Field[] fields = this.getClass().getFields();
            PluginNet.NetData payload = new PluginNet.NetData();

//            print("_.:MEMBER-PAYLOAD:._", 0);
            for (Field field : fields) {
                try {
                    key = convertCamelToSnake(field.getName());
//                    print(field.getName() + " : " + key + " => " + (String) field.get(this), 2);
                    payload.add(key, (String) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return payload;
        }

        private String convertCamelToSnake(String var) {
            StringBuilder str = new StringBuilder(var);
            for (int i = 0; i < str.length(); i++) {
                if (Character.isUpperCase(str.charAt(i))) {
                    str.insert(i, '_');
                    i++;
                }
            }
            return str.toString().toLowerCase();
        }
    }

    class LoginPayload extends Payload {
        public String username;
        public String password;
        public String guid;
        public String deviceId;
        public String phoneId;
        public String loginAttemptAccount;
        public String _csrftoken;

        LoginPayload(String uname, String password, String uuid, String csrfToken) {
            this.username = uname;
            this.password = password;
            this.guid = uuid;
            this.deviceId = IUtil.getHash().generateDeviceId(username, password);
            this.phoneId = IUtil.getGeneric().generateUuid(true);
            this.loginAttemptAccount = "0";
            this._csrftoken = csrfToken;
        }
    }

    class SyncFeaturesPayload extends Payload {
        public String _uuid;
        public String _csrftoken;
        public String _uid;
        public String id;
        public String experiments;

        SyncFeaturesPayload(String uuid, String userId, String csrftoken) {
            _uuid = uuid;
            _uid = userId;
            id = userId;
            _csrftoken = csrftoken;
            experiments = IConstant.DEVICE_EXPERIMENTS;
        }
    }
}
