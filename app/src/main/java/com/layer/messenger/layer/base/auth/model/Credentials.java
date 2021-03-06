package com.layer.messenger.layer.base.auth.model;

import com.layer.messenger.app.dao.UserUtils;
import com.layer.messenger.app.dao.api.APIRequestHandler;
import com.layer.messenger.app.dao.api.OnAuthenticationFailedListener;
import com.layer.messenger.app.dao.api.ParticipantsRequestCallback;
import com.layer.sdk.LayerClient;

import org.json.JSONObject;

/**
 * Container for storing user credentials to authenticate with Layer.
 *
 * @see APIRequestHandler#startRequestAuthenticate(LayerClient, JSONObject, OnAuthenticationFailedListener)
 * @see APIRequestHandler#startRequestParticipants(ParticipantsRequestCallback)
 */
public class Credentials {

    private final String mLayerAppId = UserUtils.getProjectId();
    private final String mUserName;

    public Credentials(String userName) {
        mUserName = userName;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getLayerAppId() {
        return mLayerAppId;
    }

}
