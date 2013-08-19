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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A selection dialog for the map type.
 * It provides four choices: Normal, Hybrid, Satellite, Terrain.
 * The attaching Activity must implement the MapTypeDialogListener interface to receive a callback on selection.
 */
public class SelectMapTypeDialog extends DialogFragment {

    private MapTypeDialogListener mListener;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_HYBRID = 1;
    public static final int TYPE_SATELLITE = 2;
    public static final int TYPE_TERRAIN = 3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.map_select_type_dialog);
        builder.setItems(R.array.maptypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Notify the callback that the user has made a selection
                mListener.onSelectMapTypeDialogSelected(which);
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (MapTypeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MapTypeDialogListener");
        }
    }

    /**
     * Interface used by callbacks from the SelectMapTypeDialog when the user makes a selection.
     */
    public interface MapTypeDialogListener {
        public void onSelectMapTypeDialogSelected(int type);
    }
}
