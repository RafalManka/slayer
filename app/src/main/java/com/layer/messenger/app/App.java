package com.layer.messenger.app;

import android.app.Application;

import com.layer.messenger.layer.providers.client.LayerClientProvider;
import com.squareup.picasso.Picasso;

/**
 * App provides static access to a LayerClient and other Atlas and Messenger context, including
 * AuthenticationProvider, ParticipantProvider, Participant, and Picasso.
 * <p>
 * App.Flavor allows build variants to target different environments, such as the Atlas Demo and the
 * open source Rails Identity Provider.  Switch flavors with the Android Studio `Build Variant` tab.
 * When using a flavor besides the Atlas Demo you must manually set your Layer App ID and GCM Sender
 * ID in that flavor's Flavor.java.
 *
 * @see LayerClientProvider
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LayerClientProvider.init(this); // initialize Layer with app context
        initPicasso(this); // init Picasso with dependencies
    }

    /**
     * Initialize Picasso with global dependencies.
     *
     * @param app App's context
     */
    private void initPicasso(App app) {
        // Picasso with custom RequestHandler for loading from Layer MessageParts.
        Picasso.Builder builder = new Picasso.Builder(app); // Create builder with application context
        LayerClientProvider.addRequestHandler(builder); // set Layer request handler
        Picasso.setSingletonInstance(builder.build()); // save for global use.
    }

}
