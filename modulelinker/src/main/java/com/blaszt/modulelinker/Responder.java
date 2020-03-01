package com.blaszt.modulelinker;

import android.app.IntentService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.blaszt.modulelinker.helper.HttpLogger;
import com.blaszt.modulelinker.helper.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Responder {
    private static Responder mInstance;

//    private WeakReference<Context> context;

    private CookieManager cookieManager;
    private Options options;
//    private BackTask task;

    public static synchronized Responder with(IntentService context) {
//        if (mInstance == null/* || mInstance.getContext() == null*/) {
            mInstance = new Responder(context);
//        }

        mInstance.options = Options.create();

        return mInstance;
    }

    private Responder(Context context) {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
//        this.context = new WeakReference<>(context);
//        HttpURLConnection.setFollowRedirects(true);
    }

    private Responder() {
    }

//    private Context getContext() {
//        return context.get();
//    }
//
//    private void clearContext() {
//        context.clear();
//    }

    public Responder apply(Options options) {
        this.options = options;
        return this;
    }

    public String getResponse(String url) {
        String response;
        response = getResponse(options.method, url, options, null);
        return response;
    }

//    private void getResponse(String url, Listener listener) {
//        if (task == null) {
//            task = new BackTask(getContext());
//        }
//
//        if (!task.isAlive()) {
//            task.start();
//        }
//
//        Message message = BackTask.backHandler.obtainMessage();
//        Bundle data = new Bundle();
//        data.putString(BackTask.BUNDLE_URL, url);
//        data.putSerializable(BackTask.BUNDLE_OPTIONS, options);
//        data.putSerializable(BackTask.BUNDLE_LISTENER, listener);
//        message.setData(data);
//        BackTask.backHandler.sendMessage(message);
//    }
//
//    private static class BackTask extends Thread {
//        private static final String BUNDLE_URL = "BackTask.BUNDLE_URL";
//        private static final String BUNDLE_OPTIONS = "BackTask.BUNDLE_OPTIONS";
//        private static final String BUNDLE_LISTENER = "BackTask.BUNDLE_LISTENER";
//        private static final String BUNDLE_RESPONSE = "BackTask.BUNDLE_RESPONSE";
//
//        static Handler frontHandler;
//        static Handler backHandler;
//
//        BackTask(Context context) {
//            super();
//            setName("BackTask");
//            frontHandler = new FrontHandler(context.getMainLooper());
//        }
//
//        @Override
//        public void run() {
//            Looper.prepare();
//            backHandler = new BackHandler();
//            Looper.loop();
//        }
//
//        private static class FrontHandler extends Handler {
//            private FrontHandler(Looper looper) {
//                super(looper);
//            }
//
//            @Override
//            public void handleMessage(Message msg) {
//                String response;
//                Listener listener;
//                if (msg.getData() != null
//                        && (listener = (Listener) msg.getData().getSerializable(BUNDLE_LISTENER)) != null) {
//                    response = msg.getData().getString(BUNDLE_RESPONSE);
//                    if (response != null) {
//                        listener.onSuccess(response);
//                    } else {
//                        listener.onError(msg.arg1);
//                    }
//                }
//            }
//        }
//
//        private static class BackHandler extends Handler {
//            @Override
//            public void handleMessage(Message msg) {
//                String url;
//                Options options;
//                Listener listener;
//                if (frontHandler != null
//                        && msg.getData() != null
//                        && (url = msg.getData().getString(BUNDLE_URL)) != null
//                        && (listener = (Listener) msg.getData().getSerializable(BUNDLE_LISTENER)) != null) {
//
//                    if ((options = (Options) msg.getData().getSerializable(BUNDLE_OPTIONS)) == null) {
//                        options = Options.create();
//                    }
//                    String response = new Responder().apply(Options.newCopy(options)).getResponse(url);
//
//                    Message message = frontHandler.obtainMessage();
//                    Bundle data = new Bundle();
//                    data.putString(BUNDLE_RESPONSE, response);
//                    data.putSerializable(BUNDLE_LISTENER, listener);
//                    message.setData(data);
//                    message.arg1 = options.errorCode;
//                    frontHandler.sendMessage(message);
//                }
//            }
//        }
//    }

    private String getResponse(int method, String url, Options options, Listener listener) {
        String response = null;
        StringBuilder cookies = new StringBuilder();
        int responseCode = 0;

        HttpLogger httpLogger = new HttpLogger("resp");
        httpLogger.writeLog("::URL::\n ||\n V\n" + url);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);

            if (method == Options.Method.POST && options.requestHeaders != null) {
                options.requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            }

            setConnectionMethod(connection, method);
            setConnectionHeaders(connection, options.requestHeaders);
            setConnectionTimeout(connection, options.timeout);
            setConnectionData(connection, options.data);

            if ((responseCode = connection.getResponseCode()) == HttpURLConnection.HTTP_OK) {
                //read response
                response = convertStreamToString(connection.getInputStream());
                options.errorCode = responseCode;

                Map<String, List<String>> headerFields = connection.getHeaderFields();
                supplyRequestHeaders(headerFields, options.headers);
                supplyRequestCookies(headerFields, cookieManager, options.cookies);
            }
            else{
////                 is redirected?
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                {
                    url = connection.getHeaderField("Location");
                    if (url.startsWith("/")) {
                        url = String.format("%s://%s%s", connection.getURL().getProtocol(), connection.getURL().getHost(), url);
                    }

//                    System.out.println("LOG_URL : " + url);
//                    System.out.println("LOG_RESPONSE : " + response);

                    for (HttpCookie cookie : options.cookies) {
//                        System.out.println("LOG_COOKIE : " + cookie.toString());
                        cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                    }

                    // Maintain cookies?
                    options.requestHeaders.put("Cookie", cookies.toString());
                    response = getResponse(method, url, options, listener);
                }
                else {
                    options.errorCode = responseCode;
                }
            }
            httpLogger.writeLog("::Response::\n ||\n V\n" + response);
            options.errorCode = responseCode;
        } catch (MalformedURLException e) {
            httpLogger.writeLog("::Malformed_URL::\n ||\n V\n" + httpLogger.getMessageLog(e));
            options.errorCode = -1;
        } catch (IOException e) {
            httpLogger.writeLog("::IO_Error::\n ||\n V\n" + httpLogger.getMessageLog(e));
            options.errorCode = -1;
        } catch (Exception e) {
            httpLogger.writeLog("::IO_Error::\n ||\n V\n" + httpLogger.getMessageLog(e));
            options.errorCode = -1;
        }

        IOUtils.disconnectQuietly(connection);

        if (listener != null) {
            if (response != null) {
                listener.onSuccess(response);
            } else {
                listener.onError(responseCode);
            }
            return null;
        } else {
            return response;
        }
    }

    @Nullable
    private String convertStreamToString(@Nullable InputStream is) {
        String result = null;
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                result = builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                result = builder.toString();
            }

            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
        }

        return result;
    }

    private void setConnectionMethod(HttpURLConnection connection, int method) {
        String requestMethod;
        switch (method) {
            case Options.Method.POST:
                requestMethod = "POST";
                break;
            case Options.Method.HEAD:
                requestMethod = "HEAD";
                break;
            case Options.Method.PUT:
                requestMethod = "PUT";
                break;
            case Options.Method.OPTIONS:
                requestMethod = "OPTIONS";
                break;
            case Options.Method.DELETE:
                requestMethod = "DELETE";
                break;
            case Options.Method.TRACE:
                requestMethod = "TRACE";
                break;
            default:
                requestMethod = "GET";
                break;
        }

        try {
            connection.setRequestMethod(requestMethod);
        } catch (ProtocolException e) {
            // ignore;
        }
    }

    private void setConnectionTimeout(HttpURLConnection connection, int timeout) {
        if (timeout >= 0) {
            connection.setConnectTimeout(timeout * 1000);
        }
    }

    private void setConnectionHeaders(@NonNull HttpURLConnection connection, @Nullable Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setConnectionData(@NonNull HttpURLConnection connection, @Nullable Map<String, String> data) {
        OutputStream os;
        String parsedData = parseData(data);

        if (parsedData != null) {
            connection.setDoOutput(true);
            try {
                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(parsedData.getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String parseData(Map<String, String> data) {
        StringBuilder parsedData = null;
        if (data == null) return null;
        else {
            parsedData = new StringBuilder();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                parsedData.append(entry.getKey()).append('=').append(entry.getValue());
                parsedData.append('&');
            }
            return parsedData.deleteCharAt(parsedData.length()-1).toString();
        }
    }

    private void supplyRequestHeaders(Map<String, List<String>> responseHeaders, Map<String, String> requestedHeaders) {
        if (requestedHeaders != null) {
            requestedHeaders.putAll(parseHeadersResponse(responseHeaders));
        }
    }

    private void supplyRequestCookies(Map<String, List<String>> responseHeaders, CookieManager cookieManager, List<HttpCookie> requestedCookies) {
        if (requestedCookies != null) {
            List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
            requestedCookies.addAll(cookies);
            requestedCookies.addAll(parseCookiesResponse(responseHeaders));
        }
    }

    @NonNull
    private Map<String, String> parseHeadersResponse(@Nullable Map<String, List<String>> responseHeaders) {
        Map<String, String> headers = new HashMap<>();

        if (responseHeaders != null) {
            List<String> listHeaders;
            for (Map.Entry<String, List<String>> responseHeader : responseHeaders.entrySet()) {
                listHeaders = responseHeader.getValue();
                if (listHeaders != null && !listHeaders.isEmpty()) {
                    headers.put(responseHeader.getKey(), listHeaders.get(listHeaders.size() - 1));
                }
            }
        }

        return headers;
    }

    @NonNull
    private List<HttpCookie> parseCookiesResponse(@Nullable Map<String, List<String>> responseHeaders) {
        List<HttpCookie> result = new ArrayList<>();

        if (responseHeaders != null) {
            List<String> listCookies;
            for (Map.Entry<String, List<String>> responseHeader : responseHeaders.entrySet()) {
                if ("set-cookie".equalsIgnoreCase(responseHeader.getKey())) {
                    listCookies = responseHeader.getValue();
                    if (listCookies != null) {
                        for (String cookieString : listCookies) {
                            result.add(HttpCookie.parse(cookieString).get(0));
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }

    interface Listener extends Serializable {
        /**
         * Serial version UID.
         */
        public static final long serialVersionUID = 8820059631275299140L;

        void onSuccess(String response);

        void onError(int errorStatus);
    }

    public static final class Options implements Serializable {

        public static final class Method {
            public static final int GET = 0;
            public static final int POST = 1;
            public static final int HEAD = 2;
            public static final int OPTIONS = 3;
            public static final int PUT = 4;
            public static final int DELETE = 5;
            public static final int TRACE = 6;
        }

        /**
         * Serial version UID.
         */
        public static final long serialVersionUID = 6745503757829161932L;

        private int errorCode = 0;
        private int method;
        private int timeout; // seconds.
        private Map<String, String> requestHeaders;
        private Map<String, String> headers;
        private ArrayList<HttpCookie> cookies;
        private Map<String, String> data;

        public static Options create() {
            return new Options();
        }

        private Options() {
            reset();
        }

        public Options setMethod(int method) {
            this.method = method;
            return this;
        }

        public Options setTimeout(int seconds) {
            this.timeout = seconds;
            return this;
        }

        public Options setRequestHeaders(Map<String, String> headers) {
            this.requestHeaders = headers;
            return this;
        }

        public Options setData(Map<String, String> data) {
            this.data = data;
            return this;
        }

        public Options requestResponseHeaders(boolean request) {
            if (request) {
                headers = new HashMap<>();
            } else {
                headers = null;
            }
            return this;
        }

        public Options requestResponseCookies(boolean request) {
            if (request) {
                cookies = new ArrayList<>();
            } else {
                cookies = null;
            }

            return this;
        }

        public Map<String, String> getResponseHeaders() {
            return headers;
        }

        public List<HttpCookie> getResponseCookies() {
            return cookies;
        }

        public int getErrorCode() { return errorCode; }

        public Options reset() {
            return setMethod(Method.GET)
                    .setRequestHeaders(null)
                    .setTimeout(30)
                    .requestResponseCookies(false)
                    .requestResponseHeaders(false);
        }

        private static Options newCopy(Options options) {
            Options newOptions = new Options();
            newOptions.method = options.method;
            newOptions.timeout = options.timeout;
            newOptions.errorCode = options.errorCode;
            newOptions.requestHeaders = options.requestHeaders != null ? new HashMap<>(options.requestHeaders) : null;
            newOptions.headers = options.headers != null ? new HashMap<>(options.headers) : null;
            newOptions.cookies = options.cookies != null ? new ArrayList<>(options.cookies) : null;
            return newOptions;
        }
    }
}
