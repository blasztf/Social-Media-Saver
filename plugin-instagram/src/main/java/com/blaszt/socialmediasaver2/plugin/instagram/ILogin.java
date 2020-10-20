package com.blaszt.socialmediasaver2.plugin.instagram;

import com.blaszt.socialmediasaver2.plugin.PluginNet;
import com.blaszt.socialmediasaver2.plugin.helper.storage.StorageCache;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.util.Map;

public class ILogin {
    private PluginStories mPlugin;
    private LimitedData mLimitedData;
    private boolean mIsLoggedIn;

    private class LimitedData {
        private String uuid;
        private String csrfToken;
        private String usernamePk;
        private long mTimeMillis;

        private LimitedData() {

        }

        private boolean isNotAllInitialized() {
            return uuid == null || csrfToken == null || usernamePk == null;
        }

        private boolean timeout() {
            return isNotAllInitialized() || (System.currentTimeMillis() - mTimeMillis) > (60 * 60 * 1000);
        }

        private void clockUp() {
            mTimeMillis = System.currentTimeMillis();
        }
    }

    ILogin(PluginStories plugin) {
        mPlugin = plugin;
        mLimitedData = new LimitedData();
    }

    boolean login(String username, String password, String referenceUrl) {
        String url;
        String response;

        String status;

        PluginNet.Config config = new PluginNet.Config();

        loadLimitedData();
        if (getLimitedData().timeout()) {
            getLimitedData().uuid = IUtil.getGeneric().generateUuid(true);
            getLimitedData().csrfToken = getCsrfToken(referenceUrl);
            getLimitedData().csrfToken = getCsrfToken();

            LoginPayload loginPayload = new LoginPayload(username, password, getLimitedData().uuid, getLimitedData().csrfToken);

            INet.asPost(config, loginPayload);
            url = IConstant.API_URL + IConstant.API_LOGIN;
            response = mPlugin.getPluginNet().getResponse(url, config);

            status = IDataUtil.getStatus(response);

            if ("ok".equalsIgnoreCase(status)) {
                mIsLoggedIn = true;
                getLimitedData().usernamePk = IDataUtil.getPk(response);
                getLimitedData().clockUp();
                saveLimitedData();
            }
        }

        // Simulate user log in.
        if (mIsLoggedIn) {
            SyncFeaturesPayload syncFeaturesPayload = new SyncFeaturesPayload(getLimitedData().uuid, getLimitedData().usernamePk, getLimitedData().csrfToken);

            INet.asPost(config, syncFeaturesPayload);
            url = IConstant.API_URL + IConstant.API_SYNC_FEATURES;
            response = mPlugin.getPluginNet().getResponse(url, config);

            INet.asGet(config);
            url = IConstant.API_URL + IConstant.API_AUTO_COMPLETE_USER_LIST;
            response = mPlugin.getPluginNet().getResponse(url, config);

//            INet.asGet(config);
//            url = IConstant.API_URL + IConstant.API_GET_INBOX;
//            response = mPlugin.getPluginNet().getResponse(url, config);
//            mPlugin.getHelper(StorageCache.class).write("resap_get_inbox", response);
//
//            INet.asGet(config);
//            url = IConstant.API_URL + IConstant.API_GET_RECENT_ACTIVITY;
//            response = mPlugin.getPluginNet().getResponse(url, config);
//            mPlugin.getHelper(StorageCache.class).write("resap_get_recent_activity", response);

            ///////////////////
        }
        
        return mIsLoggedIn;
    }

    boolean alreadyLoggedIn() {
        return mIsLoggedIn;
    }

    private String getCsrfToken() {
        String csrfToken;

        csrfToken = getCsrfToken(IConstant.API_URL + String.format(IConstant.API_FETCH_HEADERS, IUtil.getGeneric().generateUuid(false)));

        return csrfToken;
    }

    private String getCsrfToken(String url) {
        PluginNet.Config config = new PluginNet.Config();
        mPlugin.getPluginNet().getResponse(url, config);

        for (Map.Entry<String, HttpCookie> entryCookie : config.getCookies()) {
            if ("csrftoken".equalsIgnoreCase(entryCookie.getKey())) {
                return entryCookie.getValue().getValue();
            }
        }

        return null;
    }

    private LimitedData getLimitedData() {
        return mLimitedData;
    }

    private void saveLimitedData() {
        StorageCache storageCache = mPlugin.getHelper(StorageCache.class);
        if (getLimitedData().csrfToken != null) storageCache.write("il_csrf", getLimitedData().csrfToken);
        storageCache.write("il_uuid", getLimitedData().uuid);
        storageCache.write("il_upk", getLimitedData().usernamePk);
        storageCache.write("il_time", String.valueOf(getLimitedData().mTimeMillis));
    }

    private void loadLimitedData() {
        String timeMillis;

        StorageCache storageCache = mPlugin.getHelper(StorageCache.class);
        getLimitedData().csrfToken = storageCache.read("il_csrf");
        getLimitedData().uuid = storageCache.read("il_uuid");
        getLimitedData().usernamePk = storageCache.read("il_upk");
        getLimitedData().mTimeMillis = (timeMillis = storageCache.read("il_time")) != null ? Long.parseLong(timeMillis) : 0;
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
