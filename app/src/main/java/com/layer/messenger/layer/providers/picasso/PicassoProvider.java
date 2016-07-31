package com.layer.messenger.layer.providers.picasso;

import com.layer.atlas.util.picasso.requesthandlers.MessagePartRequestHandler;
import com.layer.messenger.app.App;
import com.layer.messenger.layer.providers.client.LayerClientProvider;
import com.layer.messenger.util.Log;
import com.squareup.picasso.Picasso;

/**
 * Created by rafal on 7/28/16.
 */
public class PicassoProvider {

    private static Picasso sPicasso;

    public static Picasso getInstance() {
        return sPicasso;
    }

    public static void init(App app) {
        // Picasso with custom RequestHandler for loading from Layer MessageParts.
        Picasso.Builder builder = new Picasso.Builder(app);
        try {
            builder.addRequestHandler(new MessagePartRequestHandler(LayerClientProvider.getInstance()));
        } catch (Exception e) {
            Log.e("Layer could not be initialized");
        }
        sPicasso = builder.build();
    }
}
