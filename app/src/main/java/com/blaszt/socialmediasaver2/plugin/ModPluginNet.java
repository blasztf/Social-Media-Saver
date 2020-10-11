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

        VolleyRequest.with(context).addToQueue(request);

        try {
            response = future.get(config.getTimeout(), TimeUnit.MILLISECONDS);
            setResponseHeaders(new NetHeader(request.getResponseHeaders()), config);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            response = null;
        }

        return response;
    }

    private int transformNetMethod(NetMethod method) {
        switch (method) {
            case POST:
                return Request.Method.POST;
            default:
                return Request.Method.GET;
        }
    }

    void injectContext(ModPlugin.ContextInjector context) {
        mContext = context;
    }
}
