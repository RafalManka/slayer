package com.layer.messenger.layer.providers.client;

import com.layer.atlas.util.Util;

/**
 * Created by rafal on 7/31/16.
 */
public interface LayerDeauthenticationCallbacks extends Util.DeauthenticationCallback {

    void onError(String s);

}