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

package com.example.android.touroflondon.data;

import android.provider.BaseColumns;

/**
 * Contract for TourDbHelper database access defining table and column names.
 */
public class TourContract {

    public static abstract class PoiEntry implements BaseColumns{
        public static final String TABLE_NAME = "poi";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_LOCATION_LAT = "latitude";
        public static final String COLUMN_NAME_LOCATION_LNG = "longitude";
        public static final String COLUMN_NAME_PICTURE_URL = "pictureUrl";
        public static final String COLUMN_NAME_PICTURE_ATTR = "pictureAttr";

        //prevent instantiation
        private PoiEntry() {}
    }

    public static abstract class RouteEntry implements BaseColumns{
        public static final String TABLE_NAME = "route";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";

        //prevent instantiation
        private RouteEntry() {}
    }

}
