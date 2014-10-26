package com.infamous.performance.activities;

/**
 * Created by h0rn3t on 17.07.2013.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.PackAdapter;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.PackItem;


public class PackActivity extends Activity implements Constants, OnItemClickListener,ActivityThemeChangeInterface {

    Context c=this;
    ListView packList;
    TextView packNames;
    Button applyBtn;
    SharedPreferences mPreferences;
    LinearLayout linlaHeaderProgress;
    LinearLayout linTools;
    LinearLayout linNopack;
    private boolean mIsLightTheme;
    private String pack_path;
    private String pack_pref;
    private Boolean tip;
    private ArrayList<PackItem> list = new ArrayList<PackItem>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.pack_list);
        Intent i=getIntent();
        tip=i.getBooleanExtra("mod",false);

        packNames=(TextView)  findViewById(R.id.procNames);
        if(tip){
            pack_path=USER_SYS_NAMES_PATH;
            pack_pref=PREF_SYS_NAMES;
            packNames.setHint(R.string.ps_sys_proc);
        }
        else{
            pack_path=USER_PROC_NAMES_PATH;
            pack_pref=PREF_USER_NAMES;
            packNames.setHint(R.string.ps_user_proc);
        }
        packNames.setText(mPreferences.getString(pack_pref,""));
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linTools = (LinearLayout) findViewById(R.id.tools);
        linNopack = (LinearLayout) findViewById(R.id.noproc);

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        new LongOperation().execute();

        applyBtn=(Button) findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mPreferences.edit().putString(pack_pref, packNames.getText().toString()).commit();
                new CMDProcessor().su.runWaitFor("busybox echo "+mPreferences.getString(pack_pref, Helpers.readOneLine(pack_path))+" > " + pack_path);
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            CMDProcessor.CommandResult cr = null;
            if(tip){
                cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -s | cut -d':' -f2`");
            }
            else{
                cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -3 | cut -d':' -f2`");
            }
            if(cr.success()&& !cr.stdout.equals("")){
                for(String p:cr.stdout.split(" ")){
                    list.add(new PackItem(p));
                }
                Collections.sort(list, new Comparator<PackItem>() {
                    public int compare(PackItem s1, PackItem s2) {
                        return s1.getAppName().compareTo(s2.getAppName());
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(list.size()>0){
                PackAdapter adapter = new PackAdapter(PackActivity.this, list);
                packList.setAdapter(adapter);
                linTools.setVisibility(View.VISIBLE);
                linNopack.setVisibility(View.GONE);
            }
            else{
                linTools.setVisibility(View.GONE);
                linNopack.setVisibility(View.VISIBLE);
            }
            linlaHeaderProgress.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
            linTools.setVisibility(View.GONE);
            linNopack.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long row) {
        final String told=packNames.getText().toString();
        final String pn=list.get(position).getPackName();
        if(told.equals("")){
            packNames.setText(pn);
        }
        else{
            String[] packlist=told.split(",");
            if(! Arrays.asList(packlist).contains(pn)){
                packNames.setText(told+","+pn);
            }
        }
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = is_light_theme;
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }

}
