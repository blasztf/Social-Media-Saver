package com.blaszt.socialmediasaver2.plugin;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.blaszt.socialmediasaver2.helper.data.GetParamsBuilder;
import com.blaszt.socialmediasaver2.helper.data.VolleyRequest;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ModPluginNet extends PluginNet {
    private ModPlugin.ContextInjector mContext;

    public ModPluginNet() {

    }

    @Override
    public String getResponse(String url, PluginNet.Config config) {
        RequestFuture<String> future;
        VolleyRequest.StringRequest request;
        String response;
        Context context = mContext.getContext();

        config = config != null ? config : new Config();
        if (config.getMethod() == NetMethod.GET && config.getAllData() != null) {
            url = new GetParamsBuilder(config.getAllData().getMap()).build(url);
        }
        future = RequestFuture.newFuture();
        request = new VolleyRequest.StringRequest(transformNetMethod(config.getMethod()), url, future, future);
        if (config.getMethod() == NetMethod.POST && config.getAllData() != null) {
            request.setPostParams(config.getAllData().getMap());
        }

        if (config.getHeaders() != null) {
            request.setRequestHeaders(config.getHeaders().getMap());
        }

        VolleyRequest.with(context).addToQueue(request);

        try {
            response = future.get(config.getTimeout(), TimeUnit.MILLISECONDS);
            setResponseHeaders(new NetHeader(request.getResponseHeaders()), config);
            config.setCookies(new NetCookie(VolleyRequest.with(context).getCookieManager().getCookieStore().getCookies()));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private int transformNetMethod(NetMethod method) {
        if (method == NetMethod.POST) {
            return Request.Method.POST;
        }
        return Request.Method.GET;
    }

    void injectContext(ModPlugin.ContextInjector context) {
        mContext = context;
    }
}
