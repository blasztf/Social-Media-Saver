package com.blaszt.socialmediasaver2.plugin.helper.storage;

import com.blaszt.socialmediasaver2.plugin.helper.Helper;

public interface StorageCache extends Helper {
    public boolean write(String key, String data);
    public String read(String key);
}
