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

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import com.example.android.touroflondon.PointOfInterest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MapLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Callbacks for processed data loaded from MapLoaderCallbacks.
     */
    public interface MapDataLoader {
        public void addPoi(PointOfInterest poi);
        public void addRoute(ArrayList<LatLng> list);
    }


    private MapDataLoader mCallback;
    private Context mContext;
    public static final int TOKEN_POI = 0x1;
    public static final int TOKEN_ROUTE = 0x2;


    public MapLoaderCallbacks(MapDataLoader loader, Context context) {
        mCallback = loader;
        mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        switch (id) {
            case TOKEN_POI:
                return new PoiCursorLoader(mContext, PoiQuery.PROJECTION);
            case TOKEN_ROUTE:
                return new RouteCursorLoader(mContext);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TOKEN_POI: {
                loadPois(cursor);
                break;
            }
            case TOKEN_ROUTE: {
                loadRoute(cursor);
                break;
            }
        }
    }

    private void loadRoute(Cursor cursor) {
        cursor.moveToFirst();
        ArrayList<LatLng> list = new ArrayList<LatLng>(cursor.getCount());
        while (!cursor.isAfterLast()) {
            // Extract fields from the cursor and add to the list.
            final double lat = cursor.getDouble(RouteCursorLoader.RouteQuery.ROUTE_LAT);
            final double lng = cursor.getDouble(RouteCursorLoader.RouteQuery.ROUTE_LNG);
            list.add(new LatLng(lat, lng));
            cursor.moveToNext();
        }
        // Add the route to the map.
        mCallback.addRoute(list);
    }

    private void loadPois(Cursor cursor) {
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // Extract fields from the cursor and construct the POI object.
            String title = cursor.getString(PoiQuery.POI_TITLE);
            String description = cursor.getString(PoiQuery.POI_DESCRIPTION);
            PointOfInterest.Type type = PointOfInterest.Type.valueOf(cursor.getString(PoiQuery.POI_TYPE));
            LatLng location = new LatLng(cursor.getDouble(PoiQuery.POI_LOCATION_LAT),
                    cursor.getDouble(PoiQuery.POI_LOCATION_LNG));
            String pictureAttr = cursor.getString(PoiQuery.POI_PICTURE_ATTR);
            String pictureUrl = cursor.getString(PoiQuery.POI_PICTURE_URL);

            PointOfInterest poi = new PointOfInterest(title,
                    description,
                    type,
                    pictureAttr,
                    pictureUrl,
                    location);

            // Add the POI to the map.
            mCallback.addPoi(poi);
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Do nothing.
    }

    /**
     * Projection for POI query.
     */
    private interface PoiQuery {

        String[] PROJECTION = {
                TourContract.PoiEntry.COLUMN_NAME_TITLE,
                TourContract.PoiEntry.COLUMN_NAME_DESCRIPTION,
                TourContract.PoiEntry.COLUMN_NAME_TYPE,
                TourContract.PoiEntry.COLUMN_NAME_LOCATION_LAT,
                TourContract.PoiEntry.COLUMN_NAME_LOCATION_LNG,
                TourContract.PoiEntry.COLUMN_NAME_PICTURE_ATTR,
                TourContract.PoiEntry.COLUMN_NAME_PICTURE_URL
        };

        final int POI_TITLE = 0;
        final int POI_DESCRIPTION = 1;
        final int POI_TYPE = 2;
        final int POI_LOCATION_LAT = 3;
        final int POI_LOCATION_LNG = 4;
        final int POI_PICTURE_ATTR = 5;
        final int POI_PICTURE_URL = 6;
    }


}
