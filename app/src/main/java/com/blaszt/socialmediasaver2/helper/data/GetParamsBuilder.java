package com.blaszt.socialmediasaver2.helper.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class GetParamsBuilder {
    private StringBuilder mBuilder;

    public GetParamsBuilder() {
        mBuilder = new StringBuilder();
    }

    public GetParamsBuilder(Map<String, String> params) {
        this();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            append(entry.getKey(), entry.getValue());
        }
    }

    public GetParamsBuilder append(String name, String value) {
        mBuilder.append(name).append("=").append(value).append("&");
        return this;
    }

    public String build(String url) {
        String newQuery = mBuilder.replace(mBuilder.length() - 1, mBuilder.length(), "").toString();
        URI uri = URI.create(url);
        String query = uri.getQuery();

        query = query == null ? newQuery : (query + "&" + newQuery);

        try {
            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
            return uri.toString();
        } catch (URISyntaxException e) {
            return String.format("%s?&%s", url, newQuery);
        }
    }
}
