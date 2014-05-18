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
 *
 * Modded by h0rn3t
 */

package com.infamous.performance.fragments;

import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;

import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.*;

import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.activities.BuildPropEditor;
import com.infamous.performance.activities.FlasherActivity;
import com.infamous.performance.activities.FreezerActivity;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.activities.ResidualsActivity;
import com.infamous.performance.activities.SysctlEditor;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Tools extends PreferenceFragment implements OnSharedPreferenceChangeListener, Constants {

    private int tip;
    SharedPreferences mPreferences;
    private EditText settingText;
    private Boolean isrun=false;
    private ProgressDialog progressDialog;
    private Preference mResidualFiles,mOptimDB,mlogcat;
    private Context context;
    private String nf;
    private String dn;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
  	    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        dn= mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/logs";
        addPreferencesFromResource(R.layout.tools);

        new CMDProcessor().sh.runWaitFor("busybox mkdir -p "+dn );

        mResidualFiles= findPreference(RESIDUAL_FILES);
        mOptimDB= findPreference(PREF_OPTIM_DB);

        long mStartTime=mPreferences.getLong(RESIDUAL_FILES, 0);
        mResidualFiles.setSummary("");
        if (mStartTime>0)
            mResidualFiles.setSummary(DateUtils.getRelativeTimeSpanString(mStartTime));

        mStartTime=mPreferences.getLong(PREF_OPTIM_DB, 0);
        mOptimDB.setSummary("");
        if (mStartTime>0)
            mOptimDB.setSummary(DateUtils.getRelativeTimeSpanString(mStartTime));

        mlogcat= findPreference("pref_logcat");
        mlogcat.setSummary(getString(R.string.ps_logs,dn));
        mlogcat= findPreference("pref_dmesg");
        mlogcat.setSummary(getString(R.string.ps_logs,dn));

        if(Helpers.binExist("dd").equals(NOT_FOUND) || NO_FLASH){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("category_flash_img");
            getPreferenceScreen().removePreference(hideCat);
        }
        if(Helpers.binExist("pm").equals(NOT_FOUND)){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("category_freezer");
            getPreferenceScreen().removePreference(hideCat);
        }
        if(!new File("/system/build.prop").exists()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("category_build_prop");
            getPreferenceScreen().removePreference(hideCat);
        }
        if(Helpers.binExist("sysctl").equals(NOT_FOUND)){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("category_sysctl");
            getPreferenceScreen().removePreference(hideCat);
        }

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isrun) {
            switch (tip){
                case 0:
                    progressDialog = ProgressDialog.show(context, getString(R.string.wipe_cache_title),getString(R.string.wait));
                    break;
                case 1:
                    progressDialog = ProgressDialog.show(context, getString(R.string.fix_perms_title),getString(R.string.wait));
                    break;
                case 2:
                    progressDialog = ProgressDialog.show(context, getString(R.string.optim_db_title),getString(R.string.wait));
                    break;
                case 3:
                    progressDialog = ProgressDialog.show(context, getString(R.string.logcat_title),getString(R.string.wait));
                    break;
                case 4:
                    progressDialog = ProgressDialog.show(context, getString(R.string.dmesg_title),getString(R.string.wait));
                    break;
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDetach() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDetach();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab),(ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, PCSettings.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(RESIDUAL_FILES)) {
            mResidualFiles.setSummary("");
            final long mStartTime=sharedPreferences.getLong(key,0);
            if (mStartTime>0)
                mResidualFiles.setSummary(DateUtils.getRelativeTimeSpanString(mStartTime));

        }
        else if (key.equals(PREF_OPTIM_DB)) {
            mOptimDB.setSummary("");
            final long mStartTime=sharedPreferences.getLong(key,0);
            if (mStartTime>0)
                mOptimDB.setSummary(DateUtils.getRelativeTimeSpanString(mStartTime));
        }

    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if (key.equals(PREF_SH)) {
            Resources res = context.getResources();
            String cancel = res.getString(R.string.cancel);
            String ok = res.getString(R.string.ps_volt_save);

            LayoutInflater factory = LayoutInflater.from(context);
            final View alphaDialog = factory.inflate(R.layout.sh_dialog, null);


            settingText = (EditText) alphaDialog.findViewById(R.id.shText);
            settingText.setHint(R.string.sh_msg);
            settingText.setText(mPreferences.getString(key,""));
            settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    return true;
                }
            });

            new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.sh_title))
                    .setView(alphaDialog)
                    .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                        /* nothing */
                        }
                    })
                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final SharedPreferences.Editor editor = mPreferences.edit();
                            editor.putString(key, settingText.getText().toString()).commit();

                        }
                    })
                    .create()
                    .show();

        }
        else if(key.equals(PREF_WIPE_CACHE)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.wipe_cache_title))
                    .setMessage(getString(R.string.wipe_cache_msg))
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            //alertDialog.setCancelable(false);
            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new opListener(alertDialog,0));

        }
        else if(key.equals(FLASH_KERNEL)) {
            Intent flash = new Intent(context, FlasherActivity.class);
            flash.putExtra("mod","kernel");
            startActivity(flash);
        }
        else if(key.equals(FLASH_RECOVERY)) {
            Intent flash = new Intent(context, FlasherActivity.class);
            flash.putExtra("mod","recovery");
            startActivity(flash);
        }
        else if(key.equals(RESIDUAL_FILES)) {
            Intent intent = new Intent(context, ResidualsActivity.class);
            startActivity(intent);
        }
        else if(key.equals(PREF_FIX_PERMS)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.fix_perms_title))
                    .setMessage(getString(R.string.fix_perms_msg))
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                //alertDialog.setCancelable(false);

                Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new opListener(alertDialog,1));


        }
        else if(key.equals(PREF_OPTIM_DB)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.optim_db_title))
                    .setMessage(getString(R.string.ps_optim_db)+"\n\n"+getString(R.string.fix_perms_msg))
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            //alertDialog.setCancelable(false);
            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new opListener(alertDialog,2));
        }
        else if (key.equals(PREF_FRREZE)){
            Intent intent = new Intent(context, FreezerActivity.class);
            intent.putExtra("freeze",true);
            intent.putExtra("packs","usr");
            startActivity(intent);
        }
        else if (key.equals(PREF_UNFRREZE)){
            Intent intent = new Intent(context, FreezerActivity.class);
            intent.putExtra("freeze",false);
            startActivity(intent);
        }
        else if (key.equals("pref_build_prop")){
            Intent intent = new Intent(context, BuildPropEditor.class);
            startActivity(intent);
        }
        else if (key.equals("pref_sysctl")){
            Intent intent = new Intent(context, SysctlEditor.class);
            startActivity(intent);
        }
        else if(key.equals("pref_logcat")) {

            Date now = new Date();
            nf = "/logcat_"+formatter.format(now)+".txt";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.logcat_title))
                    .setMessage(getString(R.string.logcat_msg,dn+nf))
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            //alertDialog.setCancelable(false);
            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new opListener(alertDialog,3));
        }
        else if(key.equals("pref_dmesg")) {

            Date now = new Date();
            nf = "/dmesg_"+formatter.format(now)+".txt";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.dmesg_title))
                    .setMessage(getString(R.string.dmesg_msg,dn+nf))
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            //alertDialog.setCancelable(false);
            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new opListener(alertDialog,4));
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    class opListener implements View.OnClickListener {
        private final Dialog dialog;
        private final int mod;
        public opListener(Dialog dialog,int k) {
            this.dialog = dialog;
            this.mod = k;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
            tip=this.mod;
            switch(tip){
                case 0:
                    new WipeCacheOperation().execute();
                    break;
                case 1:
                    new FixPermissionsOperation().execute();
                    break;
                case 2:
                    new DBoptimOperation().execute();
                    break;
                case 3:
                    new LogcatOperation().execute();
                    break;
                case 4:
                    new dmesgOperation().execute();
                    break;
            }

        }
    }

    private class FixPermissionsOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            new CMDProcessor().su.runWaitFor(context.getFilesDir()+"/fix_permissions");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isrun=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            isrun=true;
            progressDialog = ProgressDialog.show(context, getString(R.string.fix_perms_title),getString(R.string.wait));
            Helpers.get_assetsScript("fix_permissions",context,"#","");
            new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+context.getFilesDir()+"/fix_permissions" );
        }
    }


    private class WipeCacheOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            final StringBuilder sb = new StringBuilder();
            sb.append("busybox rm -rf /data/dalvik-cache/*\n");
            sb.append("busybox rm -rf /cache/*\n");
            sb.append("reboot\n");
            Helpers.shExec(sb,context,true);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isrun=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            isrun=true;
            progressDialog = ProgressDialog.show(context, getString(R.string.wipe_cache_title),getString(R.string.wait));
        }
    }


    private class DBoptimOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            new CMDProcessor().su.runWaitFor(context.getFilesDir()+"/sql_optimize");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isrun=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            isrun=true;
            progressDialog = ProgressDialog.show(context, getString(R.string.optim_db_title),getString(R.string.wait));
            mPreferences.edit().putLong(PREF_OPTIM_DB,System.currentTimeMillis()).commit();
            Helpers.get_assetsBinary("sqlite3",context);
            Helpers.get_assetsScript("sql_optimize",context,"busybox chmod 750 "+context.getFilesDir()+"/sqlite3","");
            new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+context.getFilesDir()+"/sql_optimize" );
        }
    }
    private class LogcatOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            new CMDProcessor().sh.runWaitFor("logcat -v time -d > "+dn+nf);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isrun=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            isrun=true;
            progressDialog = ProgressDialog.show(context, getString(R.string.logcat_title),getString(R.string.wait));
        }
    }
    private class dmesgOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            new CMDProcessor().sh.runWaitFor("dmesg > "+dn+nf);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isrun=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            isrun=true;
            progressDialog = ProgressDialog.show(context, getString(R.string.dmesg_title),getString(R.string.wait));
        }
    }
}
