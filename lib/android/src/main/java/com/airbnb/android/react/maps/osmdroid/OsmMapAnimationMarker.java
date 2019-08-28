package com.airbnb.android.react.maps.osmdroid;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ViewGroupManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.*;
import com.bumptech.glide.request.transition.*;
import com.bumptech.glide.request.target.*;
import com.bumptech.glide.gifdecoder.StandardGifDecoder;
import org.osmdroid.views.MapView;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

public class OsmMapAnimationMarker extends  OsmMapMarker {


    private DataSource<CloseableReference<CloseableImage>> dataSource;

    private final int interval = 200; // 1 Second
    private Handler handler = null;
    private Runnable runnable = null;
    private StandardGifDecoder standardGifDecoder = null;
    private boolean isTimerRunning = true;
    //private MapView mapView;
    private  Context mContext;
    public OsmMapAnimationMarker(Context context) {
        super(context);
        this.mContext = context;
    }



    @Override
    public void setImage(String uri) {
        Toast.makeText(mContext,"setImage call",Toast.LENGTH_LONG).show();
        if (uri != null && uri.contains(".gif") ) {
            Toast.makeText(mContext,"Hello Javatpoint with Gif",Toast.LENGTH_LONG).show();
            this.setGif(uri);
        }else {
            Toast.makeText(mContext,"Hello Javatpoint Without .gif",Toast.LENGTH_LONG).show();
            super.setImage(uri);
        }
    }

    public void setGif(String url) {
        if (url == null || url == "") {
            throw new NullPointerException("url");
        }

        //final MarkerEx that = this;

        if (handler == null && runnable == null) {
            handler = new Handler();
            runnable = new Runnable(){
                public void run() {
                    //boolean isDisplayed = marker.isDisplayed();
                    if (standardGifDecoder != null) {
                        standardGifDecoder.advance();
                        Bitmap b = standardGifDecoder.getNextFrame();

                        //b.setWidth(b.getWidth() / 2);
                        //b.setHeight(b.getHeight() / 2);

                        BitmapDrawable bd = new BitmapDrawable(b);
                        iconBitmapDrawable = bd;
                        if(marker != null)
                        marker.setIcon(bd);
                        mapView.invalidate();
                    }

                    if (isTimerRunning) {
                        handler.postDelayed(runnable, interval);
                    }
                }
            };
            handler.postAtTime(runnable, System.currentTimeMillis()+interval);
            handler.postDelayed(runnable, interval);
        }

        //final MapView mView = this.mMapView;
        Glide.with(this.mContext).asGif()
       // Glide.(c).asGif()
                .load(url)
                //.load(R.drawable.head_crown1)
                .into(new CustomTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        try {
                            Object GifState = resource.getConstantState();
                            Field frameLoader = GifState.getClass().getDeclaredField("frameLoader");
                            frameLoader.setAccessible(true);
                            Object gifFrameLoader = frameLoader.get(GifState);

                            Field gifDecoder = gifFrameLoader.getClass().getDeclaredField("gifDecoder");
                            gifDecoder.setAccessible(true);

                            standardGifDecoder = (StandardGifDecoder) gifDecoder.get(gifFrameLoader);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

}
