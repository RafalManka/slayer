package com.layer.messenger.layer.providers.auth.model;

import com.layer.messenger.app.dao.UserUtils;

/**
 * Created by rafal on 7/31/16.
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
