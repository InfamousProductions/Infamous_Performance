/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
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
 */

package com.infamous.performance.activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.BootClass;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class PCSettings extends PreferenceActivity implements Constants, ActivityThemeChangeInterface, OnPreferenceChangeListener {

    SharedPreferences mPreferences;
    private CheckBoxPreference mLightThemePref,mInitd;
    private ColorPickerPreference mWidgetBgColorPref;
    private ColorPickerPreference mWidgetTextColorPref;
    private Preference mVersion,mIntSD,mExtSD;
    private Context c=this;
    private String ver="";
    private String det="";
    private Boolean isupdate=false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.pc_settings);
        setTheme();

        mLightThemePref = (CheckBoxPreference) findPreference("use_light_theme");

        mWidgetBgColorPref = (ColorPickerPreference) findPreference("widget_bg_color");
        mWidgetBgColorPref.setOnPreferenceChangeListener(this);
        mWidgetTextColorPref = (ColorPickerPreference) findPreference("widget_text_color");
        mWidgetTextColorPref.setOnPreferenceChangeListener(this);

        mVersion = findPreference("version_info");
        mVersion.setTitle(getString(R.string.pt_ver) + VERSION_NUM);

        mIntSD = findPreference("int_sd");
        mExtSD = findPreference("ext_sd");
        mExtSD.setSummary(mPreferences.getString("ext_sd_path",Helpers.extSD()));
        mIntSD.setSummary(mPreferences.getString("int_sd_path",Environment.getExternalStorageDirectory().getAbsolutePath()));

        final File initd=new File(INIT_D);
        mInitd = (CheckBoxPreference) findPreference("boot_mode");
        if(initd.exists() && initd.isDirectory()) {
            mInitd.setSummary(INIT_D + mPreferences.getString("script_name", "99PC"));
        }
        else{
            getPreferenceScreen().removePreference(mInitd);
        }


    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals("use_light_theme")) {
            mPreferences.edit().putBoolean("theme_changed",true).commit();
            finish();
            return true;
        }
        else if(key.equals("visible_tabs")){
            startActivity(new Intent(this, HideTabs.class));
            return true;
        }
        else if (key.equals("boot_mode")) {
            if(mInitd.isChecked()){
                LayoutInflater factory = LayoutInflater.from(this);
                final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
                final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
                final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
                tv.setText(mPreferences.getString("script_name","99PC"));
                tn.setText("");
                tn.setVisibility(TextView.GONE);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.pt_script_name))
                        .setView(editDialog)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s=tv.getText().toString();
                                if ((s != null) && (s.length() > 0)) {
                                    mPreferences.edit().putString("script_name",s).apply();
                                }
                                mInitd.setSummary(INIT_D+mPreferences.getString("script_name","99PC"));
                                new BootClass(c,mPreferences).writeScript();
                            }
                        }).create().show();
            }
            else{
                final StringBuilder sb=new StringBuilder();
                sb.append("mount -o rw,remount /system;\n");
                sb.append("busybox rm ").append(INIT_D).append(mPreferences.getString("script_name","99PC")).append(";\n");
                sb.append("mount -o ro,remount /system;\n");
                Helpers.shExec(sb, c, true);
            }
            return true;
        }
        else if(key.equals("int_sd")) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
            final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
            final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
            tv.setText("");
            tn.setText(getString(R.string.info_auto_sd));

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pt_int_sd))
                    .setView(editDialog)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s=tv.getText().toString();
                            if ((s != null) && (s.length() > 0)) {
                                if (s.endsWith("/")) { s = s.substring(0, s.length() - 1);}
                                if(!s.startsWith("/")) { s="/"+s; }
                                final File dir= new File(s);
                                if ( dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite() )
                                    mPreferences.edit().putString("int_sd_path",s).apply();
                            }
                            else{
                                mPreferences.edit().remove("int_sd_path").apply();
                            }
                            mIntSD.setSummary(mPreferences.getString("int_sd_path",Environment.getExternalStorageDirectory().getAbsolutePath()));
                        }
                    }).create().show();
        }
        else if(key.equals("ext_sd")) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
            final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
            final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
            tv.setText("");
            tn.setText(getString(R.string.info_auto_sd));

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pt_ext_sd))
                    .setView(editDialog)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s=tv.getText().toString();
                            if ((s != null) && (s.length() > 0)) {
                                if (s.endsWith("/")) { s = s.substring(0, s.length() - 1); }
                                if(!s.startsWith("/")) { s="/"+s; }
                                final File dir= new File(s);
                                if ( dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite() )
                                    mPreferences.edit().putString("ext_sd_path",s).apply();
                            }
                            else{
                                mPreferences.edit().remove("ext_sd_path").apply();
                            }
                            mExtSD.setSummary(mPreferences.getString("ext_sd_path",Helpers.extSD()));
                        }
                    }).create().show();
        }
        else if(key.equals("br_op")){
            startActivity(new Intent(this, BackupRestore.class));
        }
        
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWidgetBgColorPref) {
            String hex = ColorPickerPreference.convertToARGB(Integer.parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(PREF_WIDGET_BG_COLOR, intHex);
            editor.commit();
            Helpers.updateAppWidget(this);
            return true;
        }
        else if (preference == mWidgetTextColorPref) {
            String hex = ColorPickerPreference.convertToARGB(Integer.parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(PREF_WIDGET_TEXT_COLOR, intHex);
            editor.commit();
            Helpers.updateAppWidget(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mLightThemePref.isChecked();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
        getListView().setBackgroundDrawable(getResources().getDrawable(is_light_theme ? R.drawable.background_holo_light : R.drawable.background_holo_dark));
    }

    private class GetUpdates extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost method = new HttpPost(URL+"ver.php");
                HttpResponse response = httpclient.execute(method);
                HttpEntity entity = response.getEntity();
                if(entity != null){
                    return EntityUtils.toString(entity);
                }
                else{
                    return "";
                }
            }
            catch(Exception e){
                return "";
            }
        }
        @Override
        protected void onPostExecute(String result) {

            if(result.equals("")){
                mVersion.setSummary(getString(R.string.no_update));
            }
            else{
                try{
                    JSONObject json = new JSONObject(result);
                    ver=json.getString("ver");
                    det=json.getString("log").replace("<br>","\n");
                    if(testver(ver)){
                        mVersion.setSummary(getString(R.string.is_update));
                        isupdate=true;
                    }
                    else{
                        mVersion.setSummary(getString(R.string.no_update));
                    }
                }
                catch (Exception e){
                    mVersion.setSummary(getString(R.string.no_update));
                    e.printStackTrace();
                }
            }
        }
        @Override
        protected void onPreExecute() { }
        @Override
        protected void onProgressUpdate(Void... values) { }
    }
    public boolean testver(String v){
        int i=0;
        final String[] sv1=VERSION_NUM.replace(" ",".").split("\\.");
        final String[] sv2=v.split("\\.");
        //if(sv1.length>sv2.length) return true;
        while(i<sv2.length){
            if(sv1[i].equals(sv2[i])) i++;
            else return (Integer.parseInt(sv1[i]) <= Integer.parseInt(sv2[i]));
        }
        return false;
    }
    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
        catch (Exception e) {
            return false;
        }
    }
}
