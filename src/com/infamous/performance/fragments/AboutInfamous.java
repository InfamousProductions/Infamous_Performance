/*
 * Infamous Performance - An Android CPU Control application Copyright (C) 2014
 * Jamison904 and Mrimp
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.infamous.performance.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.content.*;
import android.view.View;
import android.view.*;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.net.Uri;
import android.preference.*;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.infamous.performance.R;
import com.infamous.performance.activities.KSMActivity;
import com.infamous.performance.activities.MemUsageActivity;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.activities.PackActivity;
import com.infamous.performance.activities.ZramActivity;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import android.util.*;

public class AboutInfamous extends PreferenceFragment {

	public static final String TAG = "About Infamous";

    Preference mSiteUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        addPreferencesFromResource(R.xml.about);
        mSiteUrl = findPreference("ioap_site");

        PreferenceGroup devsGroup = (PreferenceGroup) findPreference("devs");
        ArrayList<Preference> devs = new ArrayList<Preference>();
        for (int i = 0; i < devsGroup.getPreferenceCount(); i++) {
            devs.add(devsGroup.getPreference(i));
        }
        devsGroup.removeAll();
        devsGroup.setOrderingAsAdded(false);
        Collections.shuffle(devs);
        for(int i = 0; i < devs.size(); i++) {
            Preference p = devs.get(i);
            p.setOrder(i);

            devsGroup.addPreference(p);
        }
    }

	

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://infamousproductions.github.io/");
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
        Intent github = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(github);
        Intent google = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(google);
        Intent facebook = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(facebook);
    }
}
