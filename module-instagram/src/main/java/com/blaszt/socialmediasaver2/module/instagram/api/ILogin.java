package com.blaszt.socialmediasaver2.module.instagram.api;

import com.blaszt.socialmediasaver2.module_base.Responder;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

//import dev.niekirk.com.instagram4android.util.InstagramGenericUtil;

public class ILogin {

    private Responder responder;

//    private HttpLogger log = new HttpLogger("ilogin");

    public static synchronized ILogin with(Responder responder) {
        return new ILogin(responder);
    }

    private ILogin(Responder responder) {
        this.responder = responder;
//        Thread.setDefaultUncaughtExceptionHandler(log);
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

        Responder.Options opts = Responder.Options.create()
                .setTimeout(30)
                .requestResponseCookies(true);

        url = IConstant.API_URL + IConstant.API_LOGIN;
        response = INet.use(responder).asPost(loginPayload.build(), opts).getResponse(url);

//        log.writeLog("Error code : " + opts.getErrorCode());
//        log.writeLog("Error message : " + opts.getErrorMessage());

        status = IDataUtil.getStatus(response);

        if ("ok".equalsIgnoreCase(status)) {
            isLoggedIn = true;
        }

        // Simulate user log in.
        usernamePk = IDataUtil.getPk(response);

        SyncFeaturesPayload syncFeaturesPayload = new SyncFeaturesPayload(uuid, usernamePk, csrfToken);

        url = IConstant.API_URL + IConstant.API_SYNC_FEATURES;
        response = INet.use(responder).asPost(syncFeaturesPayload.build(), opts).getResponse(url);

        url = IConstant.API_URL + IConstant.API_AUTO_COMPLETE_USER_LIST;
        response = INet.use(responder).asGet(null, opts).getResponse(url);

        url = IConstant.API_URL + IConstant.API_GET_INBOX;
        response = INet.use(responder).asGet(null, opts).getResponse(url);

        url = IConstant.API_URL + IConstant.API_GET_RECENT_ACTIVITY;
        response = INet.use(responder).asGet(null, opts).getResponse(url);

        ///////////////////



        return isLoggedIn;
    }

    private String getCsrfToken() {

        Responder.Options opts = Responder.Options.create()
                .requestResponseCookies(true);

        responder.apply(opts).getResponse(IConstant.API_URL + String.format(IConstant.API_FETCH_HEADERS, IUtil.getGeneric().generateUuid(false)));

        for (HttpCookie cookie : opts.getResponseCookies()) {
            if ("csrftoken".equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void print(String str, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }

        System.out.println(str);
    }

    class Payload {

        Map<String, String> build() {
            String key = "";
            Field[] fields = this.getClass().getFields();
            Map<String, String> payload = new HashMap<>();

//            print("_.:MEMBER-PAYLOAD:._", 0);
            for (Field field : fields) {
                try {
                    key = convertCamelToSnake(field.getName());
//                    print(field.getName() + " : " + key + " => " + (String) field.get(this), 2);
                    payload.put(key, (String) field.get(this));
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
