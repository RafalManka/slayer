package com.layer.messenger.layer.base.auth.model;

import com.layer.messenger.app.dao.UserUtils;
import com.layer.messenger.app.dao.api.OnAuthenticationFailedListener;
import com.layer.messenger.app.dao.api.ParticipantsRequestCallback;
import com.layer.sdk.LayerClient;

import org.json.JSONObject;

/**
 * Container for storing user credentials to authenticate with Layer.
 *
 * @see com.layer.messenger.app.dao.api.UserRequestHandler#startRequestAuthenticate(LayerClient, JSONObject, OnAuthenticationFailedListener)
 * @see com.layer.messenger.app.dao.api.UserRequestHandler#startRequestParticipants(ParticipantsRequestCallback)
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
