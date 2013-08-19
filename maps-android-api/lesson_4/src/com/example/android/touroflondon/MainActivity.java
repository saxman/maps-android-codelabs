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

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.example.android.touroflondon.data.TourDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * The main activity launched on startup.
 * This Activity handles the two fragments that make up this application: TourMapFragment and PoiListFragment. It also ensures that Google Play Services
 * are available on startup and adds an option for the About dialog to the ActionBar.
 */
public class MainActivity extends Activity implements PoiListFragment.OnPoiSelectedListener, SelectMapTypeDialog.MapTypeDialogListener {

    // The MapFragment
    private TourMapFragment mMapFragment = null;

    // Fragment showing a list of POIs
    private PoiListFragment mPoiListFragment = null;

    // True if the tablet UI is used (both fragment next to each other)
    private boolean mIsTablet = false;

    // The POI menu item for the phone layout
    private MenuItem mPoiMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise the layout, using the resource manager to distinguish between a phone (layout/) and tablet (layout-large/)
        setContentView(R.layout.activity_main);

        // Retrieve the fragments that were defined in the activity_main layout
        mMapFragment = (TourMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map);
        mPoiListFragment = (PoiListFragment) getFragmentManager().findFragmentById(R.id.fragment_poi);

        // This is a tablet layout (with two fragments on the screen at the same time) if the PoiListFragment
        // has already been loaded.
        mIsTablet = (mPoiListFragment != null);


        // load data into database if required
        if (!Util.hasDataLoaded(getApplicationContext())) {
            loadData();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Verify that Google Play Services is available
        int playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (playStatus != ConnectionResult.SUCCESS) {
            // Google Play services is not available, prompt user and close application when dialog is dismissed
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this, 0);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            dialog.show();

            // Hide all active fragments
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(mMapFragment);
            if (mPoiListFragment != null) {
                ft.hide(mPoiListFragment);
            }
            ft.commit();

        } else {

            // Make sure active fragments are shown when returning from Play Services dialog interaction
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.show(mMapFragment);
            if (mPoiListFragment != null) {
                ft.show(mPoiListFragment);
            }
            ft.commit();
        }
    }

    /**
     * Parses the tour.json file from the raw resources and imports it into
     * the database.
     */
    private void loadData() {
        try {
            // Parse file into JSONObject
            JSONObject data = Util.readRoute(getApplicationContext());

            // Initialise databse helper
            TourDbHelper dbHelper = new TourDbHelper(getApplicationContext());

            // Insert POIs and route
            dbHelper.loadPois(data.getJSONArray("pointsOfInterest"));
            dbHelper.loadRoute(data.getJSONArray("route"));

            // Mark data as loaded
            Util.setDataLoaded(getApplicationContext(), true);

        } catch (IOException e) {
           throw new IllegalStateException("Could not access tour.json file.",e);
        } catch (JSONException e) {
            throw new IllegalStateException("Could not parse tour.json file.",e);
        }
    }

    /**
     * Create the option menu. Compiled from the common menu description (common.xml).
     * In addition, when in the phone layout, the phone.xml menu description is also appended.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.common, menu);

        // Load special action items for the phone layout.
        if (!mIsTablet) {
            inflater.inflate(R.menu.phone, menu);
            if (mPoiListFragment != null && mPoiListFragment.isVisible()) {
                mPoiMenuItem = menu.findItem(R.id.menu_poi);
                mPoiMenuItem.setVisible(false); //hide by default
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Called when an option item is selected.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_poi:{
                // Show the POI fragment

                // Initialise if required
                if (mPoiListFragment == null) {
                    mPoiListFragment = new PoiListFragment();
                }

                // Display the fragment
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mPoiListFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null).commit();

                // Hide the POI action item
                item.setVisible(false);

                // Enable up navigation from POI fragment
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setTitle(R.string.menu_poi_home_action);

                return true;
            }
            case android.R.id.home: {
                // Home action item, close POI fragment
                closePoiFragment();
                return true;
            }
            case R.id.menu_about:
                // Display about dialog
                new AboutDialog().show(getFragmentManager(),null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A POI has been selected from the PoiListFragment.
     * Notify the map and close the POI fragment.
     *
     * @param id Selected poi
     */
    @Override
    public void onPoiSelected(String id) {
        if (!mIsTablet) {
            // Phone layout: Need to return back to MapFragment first and display action item again
            closePoiFragment();
        }
        mMapFragment.onPoiSelected(id);
    }

    /**
     * Notify the TourMapFragment to display a dialog to select the map type.
     * @param type
     */
    @Override
    public void onSelectMapTypeDialogSelected(int type) {
        mMapFragment.onSelectMapTypeDialogSelected(type);
    }

    /**
     * Returns from the PoiFragment back to the MapFragment and reset the display
     * (enable the poi action item and disable the home button).
     */
    private void closePoiFragment() {
        // Return to TourMapFragment
        getFragmentManager().popBackStack();

        // Reset action bar, enable item and reset home button
        mPoiMenuItem.setVisible(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(R.string.app_name);
    }
}
