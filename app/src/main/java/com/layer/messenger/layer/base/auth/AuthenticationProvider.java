package com.layer.messenger.layer.base.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.layer.messenger.app.LoadingActivity;
import com.layer.messenger.app.LoginActivity;
import com.layer.messenger.app.dao.api.OnAuthenticationFailedListener;
import com.layer.messenger.app.dao.api.APIRequestHandler;
import com.layer.messenger.layer.base.auth.model.Credentials;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationProvider implements LayerAuthenticationListener.BackgroundThread.Weak, OnAuthenticationFailedListener {
    private final SharedPreferences mPreferences;
    private AuthenticationCallback mAuthenticationCallback;

    public AuthenticationProvider(Context context) {
        mPreferences = context.getSharedPreferences(AuthenticationProvider.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public AuthenticationProvider setCredentials(Credentials credentials) {
        if (credentials == null) {
            mPreferences.edit().clear().apply();
            return this;
        }
        mPreferences.edit().putString("name", credentials.getUserName()).apply();
        return this;
    }

    public boolean hasCredentials() {
        return mPreferences.contains("name");
    }

    public AuthenticationProvider setCallback(AuthenticationCallback authenticationCallback) {
        mAuthenticationCallback = authenticationCallback;
        return this;
    }

    public void onAuthenticated(LayerClient layerClient, String userId) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Authenticated with Layer, user ID: " + userId);
        layerClient.connect();
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onSuccess(this, userId);
        }
    }

    public void onDeauthenticated(LayerClient layerClient) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Deauthenticated with Layer");
    }

    /**
     * You should never cache the nonce or Identity Token. Both are designed to be used once, and once only.
     * If a user has been authenticated and you request a nonce, the nonce will come back null.
     * Layer will cache the user so will only need to re-authenticate if the user has been deauthenticated.
     */
    public void onAuthenticationChallenge(LayerClient layerClient, String nonce) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received challenge: " + nonce);
        respondToChallenge(layerClient, nonce);
    }

    public void onAuthenticationError(LayerClient layerClient, LayerException e) {
        String error = "Failed to authenticate with Layer: " + e.getMessage();
        if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onError(this, error);
        }
    }

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
            Intent intent = new Intent(from, LoadingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(LoadingActivity.EXTRA_LOGGED_IN_ACTIVITY_CLASS_NAME, from.getClass().getName());
            intent.putExtra(LoadingActivity.EXTRA_LOGGED_OUT_ACTIVITY_CLASS_NAME, LoginActivity.class.getName());
            from.startActivity(intent);
            return true;
        }

        // We have a Layer App ID but no cached provider credentials: routing to Login required.
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Routing to login Activity");
        Intent intent = new Intent(from, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
        return true;
    }

    @Override
    public void onError(String error) {
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onError(AuthenticationProvider.this, error);
        }
    }

    /**
     * Nonce have just been received from Layer. Now its the time to register our user in our APp
     *
     * @param layerClient {@link LayerClient}
     * @param nonce       to be used only once and should never be stored inside app.
     */
    private void respondToChallenge(LayerClient layerClient, String nonce) {
        Credentials credentials = new Credentials(mPreferences.getString("name", null));
        if (credentials.getUserName() == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No stored credentials to respond to challenge with");
            }
            return;
        }

        try {
            JSONObject params = new JSONObject()
                    .put("nonce", nonce)
                    .put("name", credentials.getUserName());

            APIRequestHandler.startRequestAuthenticate(layerClient, params, AuthenticationProvider.this);
        } catch (JSONException e) {
            Log.e("Something went wrong While trying to authenticate in Layer.", e);
        }
    }

}

