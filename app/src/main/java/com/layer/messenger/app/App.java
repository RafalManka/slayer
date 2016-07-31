package com.layer.messenger.app;

import android.app.Application;

import com.layer.atlas.provider.ParticipantProvider;
import com.layer.messenger.layer.providers.auth.AuthenticationProvider;
import com.layer.messenger.layer.providers.client.LayerClientProvider;
import com.layer.messenger.layer.providers.picasso.PicassoProvider;
import com.layer.sdk.LayerClient;
import com.squareup.picasso.Picasso;

/**
 * App provides static access to a LayerClient and other Atlas and Messenger context, including
 * AuthenticationProvider, ParticipantProvider, Participant, and Picasso.
 * <p/>
 * App.Flavor allows build variants to target different environments, such as the Atlas Demo and the
 * open source Rails Identity Provider.  Switch flavors with the Android Studio `Build Variant` tab.
 * When using a flavor besides the Atlas Demo you must manually set your Layer App ID and GCM Sender
 * ID in that flavor's Flavor.java.
 *
 * @see LayerClient
 * @see ParticipantProvider
 * @see Picasso
 * @see AuthenticationProvider
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LayerClientProvider.init(this);
        PicassoProvider.init(this);
    }

}
