package com.layer.messenger.layer.providers.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.layer.messenger.app.DemoLoginActivity;
import com.layer.messenger.app.ResumeActivity;
import com.layer.messenger.app.dao.api.OnAuthenticationFailedListener;
import com.layer.messenger.app.dao.api.UserRequestHandler;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoAuthenticationProvider implements AuthenticationProvider<DemoAuthenticationProvider.Credentials>, OnAuthenticationFailedListener {
    private final SharedPreferences mPreferences;
    private Callback mCallback;

    public DemoAuthenticationProvider(Context context) {
        mPreferences = context.getSharedPreferences(DemoAuthenticationProvider.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    @Override
    public AuthenticationProvider<Credentials> setCredentials(Credentials credentials) {
        if (credentials == null) {
            mPreferences.edit().clear().apply();
            return this;
        }
        mPreferences.edit().putString("name", credentials.getUserName()).apply();
        return this;
    }

    @Override
    public boolean hasCredentials() {
        return mPreferences.contains("name");
    }

    @Override
    public AuthenticationProvider<Credentials> setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public void onAuthenticated(LayerClient layerClient, String userId) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Authenticated with Layer, user ID: " + userId);
        layerClient.connect();
        if (mCallback != null) {
            mCallback.onSuccess(this, userId);
        }
    }

    @Override
    public void onDeauthenticated(LayerClient layerClient) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Deauthenticated with Layer");
    }

    @Override
    public void onAuthenticationChallenge(LayerClient layerClient, String nonce) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received challenge: " + nonce);
        respondToChallenge(layerClient, nonce);
    }

    @Override
    public void onAuthenticationError(LayerClient layerClient, LayerException e) {
        String error = "Failed to authenticate with Layer: " + e.getMessage();
        if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
        if (mCallback != null) {
            mCallback.onError(this, error);
        }
    }

    @Override
    public boolean routeLogin(LayerClient layerClient, Activity from) {

        if ((layerClient != null) && layerClient.isAuthenticated()) {
            // The LayerClient is authenticated: no action required.
            if (Log.isLoggable(Log.VERBOSE)) Log.v("No authentication routing required");
            return false;
        }

        if ((layerClient != null) && hasCredentials()) {
            // With a LayerClient and cached provider credentials, we can resume.
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Routing to resume Activity using cached credentials");
            }
            Intent intent = new Intent(from, ResumeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ResumeActivity.EXTRA_LOGGED_IN_ACTIVITY_CLASS_NAME, from.getClass().getName());
            intent.putExtra(ResumeActivity.EXTRA_LOGGED_OUT_ACTIVITY_CLASS_NAME, DemoLoginActivity.class.getName());
            from.startActivity(intent);
            return true;
        }

        // We have a Layer App ID but no cached provider credentials: routing to Login required.
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Routing to login Activity");
        Intent intent = new Intent(from, DemoLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
        return true;
    }

    @Override
    public void onError(String error) {
        if (mCallback != null) {
            mCallback.onError(DemoAuthenticationProvider.this, error);
        }
    }

    private void respondToChallenge(LayerClient layerClient, String nonce) {
        Credentials credentials = new Credentials(mPreferences.getString("name", null));
        if (credentials.getUserName() == null || credentials.getLayerAppId() == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No stored credentials to respond to challenge with");
            }
            return;
        }

        try {
            JSONObject params = new JSONObject()
                    .put("nonce", nonce)
                    .put("name", credentials.getUserName());

            UserRequestHandler.startRequestAuthenticate(layerClient, params, DemoAuthenticationProvider.this);
        } catch (JSONException e) {
            Log.e("Something went wrong While trying to authenticate in Layer.", e);
        }
    }

    public static class Credentials {
        private final String mUserName;

        public Credentials(String userName) {
            mUserName = userName;
        }

        public String getUserName() {
            return mUserName;
        }

        public String getLayerAppId() {
            return "com.layer.messenger.providerdemo";
        }
    }
}

