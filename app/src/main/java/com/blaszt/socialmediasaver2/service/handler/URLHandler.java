package com.blaszt.socialmediasaver2.service.handler;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.blaszt.socialmediasaver2.data.SMSContent;
import com.blaszt.socialmediasaver2.plugin.ModPlugin;
import com.blaszt.socialmediasaver2.plugin.ModPluginEngine;

import java.util.Locale;

public class URLHandler extends HandlerService<URLHandler.Result> {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public URLHandler(String name) {
        super(name);
    }

    @Override
    String getAction() {
        return SMSContent.Intent.ACTION_HANDLE_URL;
    }

    @Override
    void onHandle(@NonNull Intent intent) {
        NotificationTicket ticket;
        URLHandler.Result result;
        int pluginIndex = -1;
        ModPlugin plugin = null;
        String url = intent.getStringExtra(SMSContent.Intent.EXTRA_URL);

        if (url != null) {
            ticket = requestTicket();
            ticket.setContent("SMS2 Status", "Finding correct plugin...");
            giveTicket(ticket);

            for (ModPlugin modPlugin : ModPluginEngine.getInstance(this).each()) {
                ++pluginIndex;
                if (modPlugin.isURLValid(url)) {
                    plugin = modPlugin;
                    ticket.setContent("SMS2 Status", String.format("Found plugin : %s", plugin.getName()));
                    giveTicket(ticket);
                    break;
                }
            }

            if (shouldCallback()) {
                if (plugin != null) {
                    String[] mediaURLs = plugin.use(this).getMediaURLs(url);
                    ticket.setContent("SMS2 Status", String.format(Locale.getDefault(), "Total media urls : %d", mediaURLs.length));
                    giveTicket(ticket);

                    result = new URLHandler.Result(mediaURLs, pluginIndex);
                }
                else {
                    result = new URLHandler.Result();
                }
                getHandler().onHandled(result);
            }
        }
    }

    public static class Result {
        public final String[] mediaURLs;
        public final int pluginIndex;

        private Result() {
            mediaURLs = null;
            pluginIndex = -1;
        }

        private Result(String[] urls, int index) {
            mediaURLs = urls;
            pluginIndex = index;
        }
    }
}
