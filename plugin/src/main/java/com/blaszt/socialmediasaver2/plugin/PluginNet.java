package com.blaszt.socialmediasaver2.plugin;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class PluginNet {

    public PluginNet() {

    }

    public abstract String getResponse(String url, Config config);

    public String getResponse(String url) {
        return getResponse(url, null);
    }

    protected void addResponseHeader(String name, String value, Config config) {
        config.addResponseHeader(name, value);
    }

    protected void setResponseHeaders(NetHeader headers, Config config) {
        config.setResponseHeaders(headers);
    }

    public enum NetMethod {
        GET,
        POST
    }

    private static class NetMapIterator<V> implements Iterator<Map.Entry<String, V>> {
        private Set<Map.Entry<String, V>> entry;

        public NetMapIterator(NetMap<V> vs) {
            entry = vs.getInternalMap().entrySet();
        }

        @Override
        public boolean hasNext() {
            return entry.iterator().hasNext();
        }

        @Override
        public Map.Entry<String, V> next() {
            return entry.iterator().next();
        }
    }

    private static abstract class NetMap<V> implements Iterable<Map.Entry<String, V>> {
        Map<String, V> mInternalMap;

        public NetMap() {
            mInternalMap = new HashMap<>();
        }

        public NetMap(Map<String, V> map) {
            mInternalMap = new HashMap<>(map);
        }

        public void add(String name, V value) {
            mInternalMap.put(name, value);
        }

        public V get(String name) {
            return mInternalMap.get(name);
        }

        public boolean contains(String name) {
            return mInternalMap.containsKey(name);
        }

        public void merge(NetMap<V> source) {
            for (Map.Entry<String, V> entry : source) {
                add(entry.getKey(), entry.getValue());
            }
        }

        private Map<String, V> getInternalMap() {
            return mInternalMap;
        }

        public Map<String, V> getMap() {
            return new HashMap<>(getInternalMap());
        }

        @Override
        public Iterator<Map.Entry<String, V>> iterator() {
            return new NetMapIterator<>(this);
        }
    }

    public static class NetHeader extends NetMap<String> {
        public NetHeader(Map<String, String> map) {
            super(map);
        }

        public NetHeader() {

        }
    }

    public static class NetData extends NetMap<String> {

    }

    public static class NetCookie extends NetMap<HttpCookie> {
        public NetCookie(Collection<? extends HttpCookie> collection) {
            for (HttpCookie cookie : collection) {
                add(cookie.getName(), cookie);
            }
        }

        private NetCookie() {

        }

        public void add(HttpCookie value) {
            add(value.getName(), value);
        }
    }

    public static class Config {
        private int mStatusCode;
        private NetMethod mMethod;
        private int mTimeout; // seconds.
        private NetHeader mRequestHeaders;
        private NetHeader mResponseHeaders;
        private NetCookie mCookies;
        private NetData mData;

        public Config() {
            mStatusCode = -1;
            mMethod = NetMethod.GET;
            mTimeout = 30000; // millis
            mRequestHeaders = null;
            mResponseHeaders = null;
            mCookies = null;
            mData = null;
        }

//        public static Config loadDefault() {
//            return new Config();
//        }

        protected void setStatusCode(int statusCode) {
            mStatusCode = statusCode;
        }

        public int getStatusCode() {
            return mStatusCode;
        }

        public Config setMethod(NetMethod method) {
            mMethod = method;
            return this;
        }

        public NetMethod getMethod() {
            return mMethod;
        }

        public Config setTimeout(int millis) {
            mTimeout = millis;
            return this;
        }

        public int getTimeout() {
            return mTimeout;
        }

        public Config addHeader(String name, String value) {
            if (mRequestHeaders == null) {
                mRequestHeaders = new NetHeader();
            }
            mRequestHeaders.add(name, value);
            return this;
        }

        public String getHeader(String name) {
            if (mRequestHeaders == null) {
                return null;
            }
            else {
                return mRequestHeaders.get(name);
            }
        }

        public NetHeader getHeaders() {
            return mRequestHeaders;
        }

        public Config setHeaders(NetHeader headers) {
            mRequestHeaders = headers;
            return this;
        }

        private void addResponseHeader(String name, String value) {
            if (mResponseHeaders == null) {
                mResponseHeaders = new NetHeader();
            }
            mResponseHeaders.add(name, value);
        }

        public String getResponseHeader(String name) {
            if (mResponseHeaders == null) {
                return null;
            }
            else {
                return mResponseHeaders.get(name);
            }
        }

        public NetHeader getResponseHeaders() {
            return mResponseHeaders;
        }

        private void setResponseHeaders(NetHeader headers) {
            mResponseHeaders = headers;
        }

        public Config addCookie(HttpCookie cookie) {
            if (mCookies == null) {
                mCookies = new NetCookie();
            }
            mCookies.add(cookie);
            return this;
        }

        public NetCookie getCookies() {
            return mCookies;
        }

        public Config setCookies(NetCookie cookies) {
            mCookies = cookies;
            return this;
        }

        public Config addData(String name, String value) {
            if (mData == null) {
                mData = new NetData();
            }
            mData.add(name, value);
            return this;
        }

        public String getData(String name) {
            if (mData == null) {
                return null;
            }
            else {
                return mData.get(name);
            }
        }

        public NetData getAllData() {
            return mData;
        }

        public Config setData(NetData data) {
            mData = data;
            return this;
        }
    }
}