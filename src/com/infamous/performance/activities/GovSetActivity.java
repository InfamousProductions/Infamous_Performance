package com.infamous.performance.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.Prop;
import com.infamous.performance.util.PropAdapter;
import com.infamous.performance.util.PropUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by h0rn3t on 21.09.2013.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class GovSetActivity extends Activity implements Constants, AdapterView.OnItemClickListener, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private final Context context=this;
    private List<Prop> props = new ArrayList<Prop>();
    private ListView packList;
    private LinearLayout linlaHeaderProgress,nofiles;
    private RelativeLayout tools;
    private PropAdapter adapter;
    private String curgov;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.prop_view);

        Intent i=getIntent();
        curgov=i.getStringExtra("curgov");

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        nofiles = (LinearLayout) findViewById(R.id.nofiles);
        tools = (RelativeLayout) findViewById(R.id.tools);

        linlaHeaderProgress.setVisibility(LinearLayout.VISIBLE);
        nofiles.setVisibility(LinearLayout.GONE);
        tools.setVisibility(RelativeLayout.GONE);

        Button applyBtn = (Button) findViewById(R.id.applyBtn);
        Switch setOnBoot = (Switch) findViewById(R.id.applyAtBoot);
        setOnBoot.setChecked(mPreferences.getBoolean(GOV_SOB, false));

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final StringBuilder sb = new StringBuilder();
                final String s=mPreferences.getString(curgov.replace(" ","_"),"");
                if(!s.equals("")){
                    String p[]=s.split(";");
                    for (String aP : p) {
                        if(aP.contains(":")){
                            final String pn[]=aP.split(":");
                            sb.append("busybox echo ").append(pn[1]).append(" > ").append(GOV_SETTINGS_PATH).append(curgov).append("/").append(pn[0]).append(";\n");
                        }
                    }
                    final String r=Helpers.shExec(sb,context,true);
                    if((r==null)||!r.equals("nok")) {
                        Toast.makeText(context, getString(R.string.applied_ok), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(INTENT_PP);
                        intent.putExtra("from",getString(R.string.app_name));
                        context.sendBroadcast(intent);
                    }
                }
            }
        });

        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.edit().putBoolean(GOV_SOB, isChecked).apply();
            }
        });

        new GetPropOperation().execute();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class GetPropOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Helpers.get_assetsScript("utils",context,"","");
            new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+getFilesDir()+"/utils" );
            CMDProcessor.CommandResult cr =null;
            cr = new CMDProcessor().su.runWaitFor(getFilesDir()+"/utils -getprop \""+GOV_SETTINGS_PATH+curgov+"/*\" \"1\"");
            if(cr.success()){
                props= PropUtil.load_prop(cr.stdout);
                return "ok";
            }
            else{
                Log.d(TAG, "GovSettings error: " + cr.stderr);
                return "nok";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("nok")) {
                linlaHeaderProgress.setVisibility(LinearLayout.GONE);
                nofiles.setVisibility(LinearLayout.VISIBLE);
            }
            else{
                linlaHeaderProgress.setVisibility(LinearLayout.GONE);
                if(props.isEmpty()){
                        nofiles.setVisibility(LinearLayout.VISIBLE);
                }
                else{
                    nofiles.setVisibility(LinearLayout.GONE);
                    tools.setVisibility(RelativeLayout.VISIBLE);
                    adapter = new PropAdapter(GovSetActivity.this, R.layout.prop_item, props);
                    packList.setAdapter(adapter);
                }
            }
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long row) {
        final Prop p = adapter.getItem(position);
        editPropDialog(p);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vm_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_vm) {
            if(!props.isEmpty()){
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.mt_reset))
                        .setMessage(getString(R.string.reset_msg))
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mPreferences.edit().remove(curgov.replace(" ","_")).apply();
                                    }
                                }).create().show();
            }
        }
        return true;
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
    @Override
    public void onResume() {
        super.onResume();
    }

    private void editPropDialog(Prop p) {
        final Prop pp=p;
        LayoutInflater factory = LayoutInflater.from(this);
        final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
        final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
        tv.setText(pp.getVal());
        tn.setText(pp.getName());

        new AlertDialog.Builder(this)
                .setTitle(curgov.toUpperCase())
                .setView(editDialog)
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((tv.getText().toString() != null) && (tv.getText().toString().length() > 0)) {
                            pp.setVal(tv.getText().toString().trim());
                            PropUtil.set_pref(tn.getText().toString().trim(), tv.getText().toString().trim(),curgov.replace(" ","_"),mPreferences);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).create().show();
    }


}
