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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A database helper for tour data.
 * This class encapsulates access to an underlying SQLite database.
 */
public class TourDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TourOfLondon.db";

    // Database field types and constructs, used to create tables
    private static final String TEXT_TYPE = " TEXT";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String COMMA_SEP = ",";

    // SQL statement that creates a table for POI entries
    private static final String SQL_CREATE_ENTRIES_POI =
            "CREATE TABLE " + TourContract.PoiEntry.TABLE_NAME + " (" +
                    TourContract.PoiEntry._ID + " INTEGER PRIMARY KEY," +
                    TourContract.PoiEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_LOCATION_LAT + DOUBLE_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_LOCATION_LNG + DOUBLE_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_PICTURE_URL + TEXT_TYPE + COMMA_SEP +
                    TourContract.PoiEntry.COLUMN_NAME_PICTURE_ATTR + TEXT_TYPE +
                    " )";

    // SQL statement that creates a table of route points
    private static final String SQL_CREATE_ENTRIES_ROUTE =
            "CREATE TABLE " + TourContract.RouteEntry.TABLE_NAME + " (" +
                    TourContract.RouteEntry._ID + " INTEGER PRIMARY KEY," +
                    TourContract.RouteEntry.COLUMN_NAME_LAT + DOUBLE_TYPE + COMMA_SEP +
                    TourContract.RouteEntry.COLUMN_NAME_LNG + DOUBLE_TYPE +
                    " )";

    // SQL statement that removes the table of POIs
    private static final String SQL_DROP_POI =
            "DROP TABLE IF EXISTS " + TourContract.PoiEntry.TABLE_NAME;
    // SQL statement that removes the table of route points
    private static final String SQL_DROP_ROUTE =
            "DROP TABLE IF EXISTS " + TourContract.RouteEntry.TABLE_NAME;


    public TourDbHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the two table
        db.execSQL(SQL_CREATE_ENTRIES_POI);
        db.execSQL(SQL_CREATE_ENTRIES_ROUTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On database upgrade remove both tables and create them again.
        // TODO: An application should handle graceful database upgrades
        db.execSQL(SQL_DROP_POI);
        db.execSQL(SQL_DROP_ROUTE);
        onCreate(db);
    }

    /**
     * Returns a {@link Cursor} for all POIs with the given projection.
     *
     * @param projection
     * @return
     */
    public Cursor getAllPoi(String[] projection) {
        SQLiteDatabase db = this.getReadableDatabase();

        // select all entries from POI table
        return db.query(TourContract.PoiEntry.TABLE_NAME,
                projection, null, null, null, null, null);
    }

    /**
     * Returns a {@link Cursor} for all route point entries with the given projection.
     *
     * @param projection
     * @return
     */
    public Cursor getRoute(String[] projection) {
        SQLiteDatabase db = this.getReadableDatabase();

        // select all entries from route point table
        return db.query(TourContract.RouteEntry.TABLE_NAME,
                projection, null, null, null, null, null);
    }

    /**
     * Extract POI data from a {@link JSONArray} of points of interest and add it to the POI table.
     *
     * @param data
     */
    public void loadPois(JSONArray data) throws JSONException{

        SQLiteDatabase db = this.getWritableDatabase();

        // empty the POI table to remove all existing data
        db.delete(TourContract.PoiEntry.TABLE_NAME, null, null);

        // need to complete transaction first to clear data
        db.close();

        // begin the insert transaction
        db = this.getWritableDatabase();
        db.beginTransaction();

        // Loop over each point of interest in array
        for (int i = 0; i < data.length(); i++) {
            JSONObject poi = data.getJSONObject(i);

            //Extract POI properties
            final String title = poi.getString("title");
            final String type = poi.getString("type");
            final String description = poi.getString("description");
            final String pictureUrl = poi.getString("pictureUrl");
            final String pictureAttr = poi.getString("pictureAttr");

            // Location
            JSONObject location = poi.getJSONObject("location");
            final double lat = location.getDouble("lat");
            final double lng = location.getDouble("lng");

            // Create content values object for insert
            ContentValues cv = new ContentValues();
            cv.put(TourContract.PoiEntry.COLUMN_NAME_TITLE, title);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_TYPE, type);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_DESCRIPTION, description);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_PICTURE_URL, pictureUrl);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_LOCATION_LAT, lat);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_LOCATION_LNG, lng);
            cv.put(TourContract.PoiEntry.COLUMN_NAME_PICTURE_ATTR, pictureAttr);

            // Insert data
            db.insert(TourContract.PoiEntry.TABLE_NAME, null, cv);
        }

        // All insert statement have been submitted, mark transaction as successful
        db.setTransactionSuccessful();

        if (db != null) {
            db.endTransaction();
        }

    }

    /**
     * Extract Route data from a {@link JSONArray} of save it in the database.
     *
     * @param data
     */
    public void loadRoute(JSONArray data) throws JSONException{

        SQLiteDatabase db = this.getWritableDatabase();

        // Empty the route table to remove all existing data
        db.delete(TourContract.RouteEntry.TABLE_NAME, null, null);

        // Need to complete transaction first to clear data
        db.close();

        // Begin the insert transaction
        db = this.getWritableDatabase();
        db.beginTransaction();

        // Loop over each location in array
        for (int i = 0; i < data.length(); i++) {
            // extract data
            JSONObject poi = data.getJSONObject(i);
            final double lat = poi.getDouble("lat");
            final double lng = poi.getDouble("lng");

            // Construct insert statement
            ContentValues cv = new ContentValues();
            cv.put(TourContract.RouteEntry.COLUMN_NAME_LAT, lat);
            cv.put(TourContract.RouteEntry.COLUMN_NAME_LNG, lng);

            // Insert data
            db.insert(TourContract.RouteEntry.TABLE_NAME, null, cv);
        }

        if (db != null) {
            // All insert statement have been submitted, mark transaction as successful
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


}
