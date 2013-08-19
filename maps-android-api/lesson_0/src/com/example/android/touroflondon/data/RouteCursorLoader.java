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

import android.content.Context;
import android.database.Cursor;
import com.example.android.touroflondon.data.TourContract.RouteEntry;

/**
 * An instance of the {@link SqlCursorLoader} that returns a loader from
 * {@link TourDbHelper#getAllPoi()} that includes all POI entries in the
 * database.
 */
public class RouteCursorLoader extends SqlCursorLoader {

    public RouteCursorLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        return new TourDbHelper(this.getContext()).getRoute(RouteQuery.PROJECTION);
    }

    /**
     * Projection and fields for the route query.
     */
    public interface RouteQuery {

        String[] PROJECTION = {
                RouteEntry.COLUMN_NAME_LAT,
                RouteEntry.COLUMN_NAME_LNG
        };

        final int ROUTE_LAT = 0;
        final int ROUTE_LNG = 1;
    }
}
