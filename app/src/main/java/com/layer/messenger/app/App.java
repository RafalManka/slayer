package com.layer.messenger.app;

import android.app.Application;
import android.content.Context;

import com.layer.messenger.layer.base.client.LayerProvider;
import com.squareup.picasso.Picasso;

/**
 * App initializes Layer, picasso and potentially other globally accessible frameworks
 * @see LayerProvider
 */
public class App extends Application {

    /**
     * Everything that needs to be done, whe the App starts
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LayerProvider.init(this); // initialize Layer with app context
        initPicasso(this); // init Picasso with dependencies
    }

    /**
     * Initialize Picasso with global dependencies:
     * @see LayerProvider#addRequestHandler(Picasso.Builder)
     *
     * Then set the new Picasso instace for global reference, which will be from now used
     * everywhere:
     * @see Picasso#with(Context)
     *
     * @param app App's context
     */
    private void initPicasso(App app) {
        // Picasso with custom RequestHandler for loading from Layer MessageParts.
        Picasso.Builder builder = new Picasso.Builder(app); // Create builder with application context
        LayerProvider.addRequestHandler(builder); // set Layer request handler
        Picasso.setSingletonInstance(builder.build()); // save for global use.
    }

}
