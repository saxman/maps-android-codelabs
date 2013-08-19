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

import com.google.android.gms.maps.model.LatLng;

/**
 * A simple struct containing information about a point of interest (POI).
 */
public class PointOfInterest {

    /**
     * Type of the point of interest.
     */
    public enum Type {
        MUSEUM(30, R.drawable.marker_museum),
        SHOPPING(210, R.drawable.marker_mall),
        LANDMARK(270, R.drawable.marker_landmark);

        /** Hue of the marker to use for this type of POI. */
        final float mHue;

        /** ID of the custom drawable to use as the marker image. */
        final int mResId;

        private Type(float hue, int resId) {
            this.mHue = hue;
            this.mResId = resId;
        }
    }

    /** Title of the POI. */
    public final String mTitle;

    /** A short (1-2 sentence) description of the POI. */
    public final String mDescription;

    /** General category to which this POI belongs. */
    public final Type mType;

    /** The location of this POI. */
    public final LatLng mLocation;

    /** The URL to a thumbnail image of this POI. */
    public final String mPictureUrl;

    /** A copyright attribution for the thumbnail image. */
    public final String mPictureAttr;

    public PointOfInterest(String title,
            String description,
            Type type,
            String pictureAttr,
            String picture,
            LatLng location) {
        this.mTitle = title;
        this.mDescription = description;
        this.mType = type;
        this.mPictureAttr = pictureAttr;
        this.mPictureUrl = picture;
        this.mLocation = location;
    }
}
