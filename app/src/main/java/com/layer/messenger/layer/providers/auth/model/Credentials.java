package com.layer.messenger.layer.providers.auth.model;

/**
 * Created by rafal on 7/31/16.
 */
public class Credentials {
    private final String mUserName;

    public Credentials(String userName) {
        mUserName = userName;
    }

    public String getUserName() {
        return mUserName;
    }

}
