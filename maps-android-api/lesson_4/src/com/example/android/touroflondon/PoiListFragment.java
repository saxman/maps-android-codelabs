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
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.example.android.touroflondon.data.PoiCursorLoader;
import com.example.android.touroflondon.data.TourContract;
import com.example.android.touroflondon.data.TourContract.PoiEntry;

/**
 * A Fragment that displays a list of points of interest. Data is loaded using a
 * {@link LoaderManager} and displayed in a 2-row layout per entry (title and
 * description).
 */
public class PoiListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Adapter this ListFragment is backed by
    private SimpleCursorAdapter mAdapter;

    // Callback to Activity for user interaction
    private OnPoiSelectedListener mCallback;

    // Database projection
    private final static String[] projection = {
            PoiEntry._ID,
            PoiEntry.COLUMN_NAME_TITLE,
            PoiEntry.COLUMN_NAME_DESCRIPTION
    };

    /**
     * Callback interface that must be implemented by attaching Activity.
     */
    public interface OnPoiSelectedListener {
        /**
         * Called when the user selects a POI from the list.
         * @param id
         */
        public void onPoiSelected(String id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /*
         * Initialise the adapter and map the title and description columns to
         * the two rows in the simple 2-row layout. Initially the cursor is set
         * to null and updated once the data has been loaded in #onLoadFinished.
         */
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.tour_simple_list_item_2,
                null,
                new String[] {
                        TourContract.PoiEntry.COLUMN_NAME_TITLE,
                        TourContract.PoiEntry.COLUMN_NAME_DESCRIPTION
                },
                new int[] {
                        android.R.id.text1, android.R.id.text2
                }, 0);

        setListAdapter(mAdapter);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPoiSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // initialise the data loader
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Explicitly set the background color to override the default color (transparent)
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(R.color.list_background));
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // Get the title of the selected item and notify the callback
        Cursor c = (Cursor) mAdapter.getItem(position);
        String title = c.getString(c.getColumnIndex(projection[1]));
        mCallback.onPoiSelected(title);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create a new cursor loader that retrieves a list of all POIs.
        return new PoiCursorLoader(getActivity(), projection);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        // set the new cursor
        mAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // remove the old cursor
        mAdapter.swapCursor(null);
    }

}
