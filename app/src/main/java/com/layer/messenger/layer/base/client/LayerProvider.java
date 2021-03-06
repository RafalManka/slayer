package com.layer.messenger.layer.base.client;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.picasso.requesthandlers.MessagePartRequestHandler;
import com.layer.messenger.BuildConfig;
import com.layer.messenger.app.App;
import com.layer.messenger.app.dao.UserDao;
import com.layer.messenger.layer.base.auth.AuthenticationCallback;
import com.layer.messenger.layer.base.auth.AuthenticationProvider;
import com.layer.messenger.layer.base.auth.model.Credentials;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

/**
 * Created by rafal on 7/28/16.
 */
public class LayerProvider {

    @Nullable
    private static LayerClient sLayerClient;
    @Nullable
    private static AuthenticationProvider sAuthProvider;
    /**
     * Reference to an object that will allow you to fetch participants
     */
    @Nullable
    private static ParticipantProvider sParticipantProvider;
    /**
     * Application's context set in App's onCreate
     */
    @Nullable
    private static Context sContext;

    /**
     * Initialize layer framework using App context. Set logging only for Debug environment.
     *
     * @param app Application instance from Apps onCreate
     */
    public static void init(@NonNull App app) {
        sContext = app;
        // Enable verbose logging in debug builds
        if (BuildConfig.DEBUG) { // if environment is debug, log everything.
            com.layer.atlas.util.Log.setLoggingEnabled(true);
            com.layer.messenger.util.Log.setAlwaysLoggable(true);
            LayerClient.setLoggingEnabled(app, true);
        }
        // Allow the LayerClient to track app state
        LayerClient.applicationCreated(app);
    }

    /**
     * Gets or creates a LayerClient, using a default set of LayerClient.Options and flavor-specific
     * App ID and Options from the `generateLayerClient` method.  Returns `null` if the flavor was
     * unable to create a LayerClient (due to no App ID, etc.).
     *
     * @return New or existing LayerClient, or `null` if a LayerClient could not be constructed.
     */
    @NonNull
    public synchronized static LayerClient getInstance() throws Exception {
        if (sLayerClient == null) {
            // Custom options for constructing a LayerClient
            LayerClient.Options options = new LayerClient.Options()
                    /* Fetch the minimum amount per conversation when first authenticated */
                    .historicSyncPolicy(LayerClient.Options.HistoricSyncPolicy.FROM_LAST_MESSAGE)
                    /* Automatically download text and ThreePartImage info/preview */
                    .autoDownloadMimeTypes(Arrays.asList(
                            TextCellFactory.MIME_TYPE,
                            ThreePartImageUtils.MIME_TYPE_INFO,
                            ThreePartImageUtils.MIME_TYPE_PREVIEW));

            // Allow flavor to specify Layer App ID and customize Options.
            sLayerClient = generateLayerClient(sContext, options);

            // Flavor was unable to generate Layer Client (no App ID, etc.)
            if (sLayerClient == null) {
                throw new Exception("layer could not be initialized");
            }

            /* Register AuthenticationProvider for handling authentication challenges */
            sLayerClient.registerAuthenticationListener(getAuthenticationProvider());
        }
        return sLayerClient;
    }

    /**
     * Create new instance on {@link LayerClient}
     *
     * @param context App context
     * @param options Layer options
     * @return new instance of layer client
     */
    private static LayerClient generateLayerClient(Context context, LayerClient.Options options) {
        // If no App ID is set yet, return `null`; we'll launch the AppIdScanner to get one.
        options.googleCloudMessagingSenderId(BuildConfig.GCM_SENDER_ID);
        return LayerClient.newInstance(context, BuildConfig.LAYER_APP_ID, options);
    }

    /**
     * Return reference to global instance of {@link AuthenticationProvider}
     * Lazy instantiate if not exist
     *
     * @return global instance of {@link AuthenticationProvider}
     * @throws Exception
     */
    public static AuthenticationProvider getAuthenticationProvider() throws Exception {
        if (sAuthProvider == null) {
            sAuthProvider = generateAuthenticationProvider(sContext);
            // If we have cached credentials, try authenticating with Layer
            LayerClient layerClient = getInstance();
            if (sAuthProvider.hasCredentials()) layerClient.authenticate();
        }
        return sAuthProvider;
    }

    /**
     * convenience method for creating new instance
     *
     * @param context app context
     * @return new instance
     */
    public static AuthenticationProvider generateAuthenticationProvider(Context context) {
        return new AuthenticationProvider(context);
    }

    /**
     * Deauthenticates with Layer and clears cached AuthenticationProvider credentials.
     *
     * @param callback AuthenticationCallback to receive deauthentication success and failure.
     */
    @SuppressWarnings("unused")
    public static void deauthenticate(@Nullable final Util.DeauthenticationCallback callback) throws Exception {
        LayerClient instance = getInstance();
        Util.deauthenticate(instance, new Util.DeauthenticationCallback() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDeauthenticationSuccess(LayerClient client) {
                try {
                    getAuthenticationProvider().setCredentials(null);
                    if (callback != null) {
                        callback.onDeauthenticationSuccess(client);
                    }
                } catch (Exception e) {
                    Log.e("Layer could not be initialized");
                }
            }

            @Override
            public void onDeauthenticationFailed(LayerClient client, String reason) {
                if (callback != null) {
                    callback.onDeauthenticationFailed(client, reason);
                }
            }
        });
    }

    /**
     * Get provider of participants that are available to start a conversation with.
     *
     * @return global instance of Participants provider
     * @throws Exception if provider could not be created.
     */
    @NonNull
    public static ParticipantProvider getParticipantProvider() throws Exception {
        if (sParticipantProvider == null) {
            sParticipantProvider = generateParticipantProvider(sContext);
        }
        return sParticipantProvider;
    }

    /**
     * Create instance of User Data Access Object.
     *
     * @param context App's context
     * @return instance of UserDao
     */
    @NonNull
    public static ParticipantProvider generateParticipantProvider(Context context) {
        return new UserDao(context).setup();
    }

    //==============================================================================================
    // Identity Provider Methods
    //==============================================================================================

    /**
     * Routes the user to the proper Activity depending on their authenticated state.  Returns
     * `true` if the user has been routed to another Activity, or `false` otherwise.
     *
     * @param from Activity to route from.
     * @return `true` if the user has been routed to another Activity, or `false` otherwise.
     */
    public static boolean routeLogin(Activity from) throws Exception {
        return getAuthenticationProvider().routeLogin(getInstance(), from);
    }

    /**
     * Authenticates with the AuthenticationProvider and Layer, returning asynchronous results to
     * the provided authenticationCallback.
     *
     * @param credentials            Credentials associated with the current AuthenticationProvider.
     * @param authenticationCallback AuthenticationCallback to receive authentication results.
     */
    @SuppressWarnings("unchecked")
    public static void authenticate(Credentials credentials, AuthenticationCallback authenticationCallback) throws Exception {
        LayerClient client = getInstance();
        getAuthenticationProvider()
                .setCredentials(credentials)
                .setCallback(authenticationCallback);
        client.authenticate();
    }

    /**
     * Add request handler to Picasso during initial config in App's onCreate method.
     *
     * @param builder Picasso builder
     */
    public static void addRequestHandler(Picasso.Builder builder) {
        try {
            builder.addRequestHandler(new MessagePartRequestHandler(LayerProvider.getInstance()));
        } catch (Exception e) {
            Log.e("Layer could not be initialized");
        }
    }
}
