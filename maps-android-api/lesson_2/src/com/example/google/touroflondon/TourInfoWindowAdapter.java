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

package com.example.google.touroflondon;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import android.content.Context;
import android.view.View;

import java.util.Map;

/**
 * An Info Window Adapter.
 */
public class TourInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    /**
     * A map from the title to the PointOfInterest object containing more
     * information about the POI.
     */
    private final Map<String, PointOfInterest> mData;

    public TourInfoWindowAdapter(Context context, Map<String, PointOfInterest> poiData) {
        this.mData = poiData;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final PointOfInterest poi = mData.get(marker.getTitle());
        return null;
    }
}
