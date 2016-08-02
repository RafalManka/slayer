package com.layer.messenger.layer.base.auth;

/**
 * AuthenticationCallback for handling authentication success and failure.
 */
public interface AuthenticationCallback {
    void onSuccess(AuthenticationProvider provider, String userId);

    void onError(AuthenticationProvider provider, String error);
}