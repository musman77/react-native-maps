package com.airbnb.android.react.maps.osmdroid;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.airbnb.android.react.maps.R;
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
import android.util.TypedValue;
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
    private boolean _isCompletePath=false;

    public OsmMapAnimationMarker(Context context) {
        super(context);
        this.mContext = context;
    }



    @Override
    public void setImage(String uri) {
        if (uri != null) {
            TypedValue value = new TypedValue();
            if (uri == null || uri == "") {
                throw new NullPointerException("url");
            }  else if (uri.startsWith("http://") || uri.startsWith("https://") ||
                    uri.startsWith("file://")) {
                _isCompletePath=true;
            }else{
                int resourceId = getImage(uri);
                getResources().getValue(resourceId, value, true);
            }

            if (value != null && value.toString().toLowerCase().contains(".gif")) {
                Toast toast = Toast.makeText(mContext, "Contains Gif", Toast.LENGTH_LONG);
                this.setGif(uri);
            } else {
                Toast toast = Toast.makeText(mContext, "Without Gif", Toast.LENGTH_LONG);
                super.setImage(uri);
            }

        }
    }

    public void setGif(String url) {
        if (handler == null && runnable == null) {
            handler = new Handler();
            runnable = new Runnable(){
                public void run() {
                    //boolean isDisplayed = marker.isDisplayed();
                    if (standardGifDecoder != null) {
                        standardGifDecoder.advance();
                        Bitmap b = standardGifDecoder.getNextFrame();
                        BitmapDrawable bd = new BitmapDrawable(b);
                        iconBitmapDrawable = bd;
                        if(marker != null)
                        marker.setIcon(bd);
                        if(mapView != null)
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

        Glide.with(this.mContext).asGif()
                .load(_isCompletePath ? url : getImage(url))
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

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable",this.mContext.getPackageName());
        return drawableResourceId;
    }

//
//    public void onResume()
//    {
//        handler.removeCallbacks(runnable);
//    }

}
