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

import com.example.google.R;
import com.example.google.touroflondon.data.MapLoaderCallbacks;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/** An interactive map fragment that shows a tour of London. */
public class TourMapFragment extends MapFragment
        implements MapLoaderCallbacks.MapDataLoader, GoogleMap.OnInfoWindowClickListener {

    /** Key for the map type in the saved instance state. */
    public static final String KEY_MAP_TYPE = "map_type";

    /** Callbacks for loading data into this TourMapFragment */
    private MapLoaderCallbacks mLoaderCallbacks;

    /** The Google Map object. */
    private GoogleMap mMap;

    /**
     * The saved instance state passed in in onCreate that we need later for
     * setupMapIfNeeded.
     */
    private Bundle mSavedInstanceState;

    /** Starting position for the camera. */
    public static final LatLng LONDON = new LatLng(51.5, -0.12);

    /**
     * A map from the title of the place to the PointOfInterest object
     * containing more details about it.
     */
    private final Map<String, PointOfInterest> mPoiData = new HashMap<String, PointOfInterest>();

    /** A map from the title of the marker to the marker itself. */
    private final Map<String, Marker> mPoiMarkers = new HashMap<String, Marker>();

    /** Used for generating random bearings to make camera animations cooler! */
    private final Random mRandom = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;

        // Initialise loader callbacks, used to notify this fragment once data
        // has been loaded from the database
        mLoaderCallbacks = new MapLoaderCallbacks(this, getActivity());

        // For the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // See the javadoc for this method for why we should call this here as
        // well..
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play
     * services APK is correctly installed) and the map has not already been
     * instantiated.
     * <p>
     * If it isn't installed {@link MapFragment} will show a prompt for the user
     * to install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and
     * correctly installing/updating/enabling the Google Play services. Since
     * the FragmentActivity may not have been completely destroyed during this
     * process (it is likely that it would only be stopped or paused),
     * {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // We can now set up the map.

                if (mSavedInstanceState == null) {
                    // The map automatically saves the camera position so we
                    // only want to set it if we are starting
                    // fresh.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LONDON, 12));
                } else {
                    // Set the map type to the type that was saved.
                    mMap.setMapType(mSavedInstanceState.getInt(KEY_MAP_TYPE,
                            GoogleMap.MAP_TYPE_NORMAL));
                }

                // Disable the on screen zoom controls.
                mMap.getUiSettings().setZoomControlsEnabled(false);

                // load data
                LoaderManager lm = getLoaderManager();
                lm.initLoader(MapLoaderCallbacks.TOKEN_POI, null, mLoaderCallbacks);
                lm.initLoader(MapLoaderCallbacks.TOKEN_ROUTE, null, mLoaderCallbacks);

                // Set a custom info window adapter.
                TourInfoWindowAdapter adapter = new TourInfoWindowAdapter(getActivity(), mPoiData);
                mMap.setInfoWindowAdapter(adapter);

                // Set an on info window click listener.
                mMap.setOnInfoWindowClickListener(this);

                // Turn on the my location layer.
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_map_center:
                showAllPois();
                return true;
            case R.id.menu_map_type:
                showMapTypeSelector();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MAP_TYPE, mMap.getMapType());
    }

    /** Moves the camera back to a position which includes all the POIs. */
    private void showAllPois() {
        // Build a bounding box containing all of the POIs.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        Iterator<Map.Entry<String, Marker>> iter = mPoiMarkers.entrySet().iterator();
        while (iter.hasNext()) {
            Marker m = iter.next().getValue();
            builder.include(m.getPosition());
        }
        // Create a camera update that includes all the POIs with a padding of
        // 100px.
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
        mMap.animateCamera(update);
    }

    /**
     * Show a dialog to allow the user to choose which map type they want.
     */
    private void showMapTypeSelector() {
        SelectMapTypeDialog dialog = new SelectMapTypeDialog();
        dialog.show(getFragmentManager(), "maptype");
    }

    /**
     * Called when a POI is selected from the list of POIs.
     * 
     * @param title The title of the POI.
     */
    public void onPoiSelected(String title) {
        Marker marker = mPoiMarkers.get(title);
        if (marker != null) {
            // Construct a camera position. We use an arbitrary bearing because
            // it makes the camera animation look
            // cooler!
            CameraPosition camera = new CameraPosition.Builder()
                    .bearing(mRandom.nextFloat() * 360)
                    .zoom(18f)
                    .tilt(45f)
                    .target(marker.getPosition())
                    .build();

            // Animate the camera to that position using a custom duration of 2
            // seconds.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera), 2000, null);

            // Show the info window.
            marker.showInfoWindow();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // Do a web search for the title of the POI.
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, marker.getTitle());
        startActivity(intent);
    }

    /**
     * Called when a selection is made from the select map type dialog.
     * 
     * @param type One of the constants listed in {@link SelectMapTypeDialog}.
     */
    public void onSelectMapTypeDialogSelected(int type) {
        // Get the map type constant that corresponds to the option chosen.
        int mapType = GoogleMap.MAP_TYPE_NORMAL;
        switch (type) {
            case SelectMapTypeDialog.TYPE_NORMAL:
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                break;
            case SelectMapTypeDialog.TYPE_HYBRID:
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case SelectMapTypeDialog.TYPE_SATELLITE:
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case SelectMapTypeDialog.TYPE_TERRAIN:
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
        }

        // Set that map type on the map.
        mMap.setMapType(mapType);
    }

    /**
     * Called to add a point of interest to the map.
     */
    public void addPoi(PointOfInterest poi) {
        MarkerOptions options = new MarkerOptions()
                .position(poi.mLocation)
                .title(poi.mTitle)
                .snippet(poi.mDescription);

        // Choose a custom icon for the POI according to its type.
        options.icon(BitmapDescriptorFactory.fromResource(poi.mType.mResId));

        // Add the marker to the map.
        Marker marker = mMap.addMarker(options);

        // Add the marker to some data sets.
        mPoiData.put(marker.getTitle(), poi);
        mPoiMarkers.put(marker.getTitle(), marker);
    }

    /**
     * Called with a list of LatLng route coordinates.
     */
    public void addRoute(ArrayList<LatLng> list) {
    }
}
