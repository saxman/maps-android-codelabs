/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.touroflondon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * An Info Window Adapter that returns a custom info window with a thumbnail and description about each Point of
 * Interest.
 */
public class TourInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final int CACHE_SIZE_BYTES = 4 * 1024 * 1024; // 4 MB

    /**
     * The template view for the custom info contents. We can reuse this view whenever getInfoContents is called because
     * the map just takes a static snapshot and renders that to the screen.
     */
    private final View mContents;

    /** A map from the title to the PointOfInterest object containing more information about the POI. */
    private final Map<String, PointOfInterest> mData;

    /** An in-memory cache of the thumbnail images of the points of interest. */
    private final LruCache<PointOfInterest, Bitmap> mThumbnails;

    /** The request queue used to add the thumbnail image requests. */
    private final RequestQueue mRequestQueue;

    public TourInfoWindowAdapter(Context context, Map<String, PointOfInterest> poiData) {
        // Inflate the custom info contents view just once and reuse it each time an info window needs to be
        // generated.
        mContents = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_info_contents, null);

        // We maintain an in-memory LRU cache of the thumbnails so that they can easily be recalled again.
        mThumbnails = new LruCache<PointOfInterest, Bitmap>(CACHE_SIZE_BYTES) {
            @Override
            protected int sizeOf(PointOfInterest key, Bitmap value) {
                return value.getByteCount();

            }};

        // Setup the request queue for fetching the thumbnail images of the Points of Interest.
        mRequestQueue = Volley.newRequestQueue(context);
        mRequestQueue.start();

        this.mData = poiData;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // We don't want to provide a new bubble, just the contents of the bubble.  Returning null here causes the
        // getInfoContents to be called and the returned View to be added inside the default bubble.
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final PointOfInterest poi = mData.get(marker.getTitle());

        // If the data has not yet been loaded, we may not be able to get the POI. (Note it happens that this is not
        // actually possible with the current design because a marker can only be clicked once the data is loaded, but
        // we include this just to be safe.)
        if (poi == null) {
            return null;
        }

        // Fetch the thumbnail if we don't have one already.
        if (mThumbnails.get(poi) == null) {
            mRequestQueue.add(
                    new ImageRequest(poi.mPictureUrl, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            mThumbnails.put(poi, bitmap);
                            // Don't refresh the info window if the info window is not currently shown.
                            if (marker.isInfoWindowShown()) {
                                marker.showInfoWindow();
                            };
                        }
                    }, 256, 256, Config.ARGB_4444, null));
        }

        // Return the rendered view.
        return render(poi);
    }

    /**
     * Renders the POI to the contents view.
     */
    private View render(PointOfInterest poi) {
        Bitmap bitmap = mThumbnails.get(poi);
        if (bitmap != null) {
            // If we have already downloaded the image, set the image and the attribute.
            ((ImageView) mContents.findViewById(R.id.thumbnail)).setImageBitmap(bitmap);
            ((TextView) mContents.findViewById(R.id.caption)).setText(poi.mPictureAttr);
        } else {
            // If we don't yet have the bitmap, set a placeholder image and attribute.
            ((ImageView) mContents.findViewById(R.id.thumbnail)).setImageResource(R.drawable.ic_launcher);
            ((TextView) mContents.findViewById(R.id.caption)).setText(R.string.infowindow_loading);
        }
        // Set the title and description.
        ((TextView) mContents.findViewById(R.id.title)).setText(poi.mTitle);
        ((TextView) mContents.findViewById(R.id.snippet)).setText(poi.mDescription);
        return mContents;
    }
}
