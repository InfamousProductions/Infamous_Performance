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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by h0rn3t on 05.04.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class KSMSetActivity extends Activity implements Constants, AdapterView.OnItemClickListener, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private final Context context=this;
    private List<Prop> props = new ArrayList<Prop>();
    private ListView packList;
    private LinearLayout linlaHeaderProgress,nofiles;
    private RelativeLayout tools;
    private PropAdapter adapter;
    private String path,sob,pref;
    private String[] mAvailableGovernors;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.prop_view);

        Intent intent=getIntent();
        path=intent.getStringExtra("path");
        sob=intent.getStringExtra("sob");
        pref=intent.getStringExtra("pref");

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
        setOnBoot.setChecked(mPreferences.getBoolean(sob, false));

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final StringBuilder sb = new StringBuilder();
                final String s=mPreferences.getString(pref,"");
                if(!s.equals("")){
                    String p[]=s.split(";");
                    for (String aP : p) {
                        if(aP.contains(":")){
                            final String pn[]=aP.split(":");
                            sb.append("busybox echo ").append(pn[1]).append(" > ").append(path).append("/").append(pn[0]).append(";\n");
                        }
                    }
                    final String r= Helpers.shExec(sb, context, true);
                    if((r==null)||!r.equals("nok"))
                        Toast.makeText(context, getString(R.string.applied_ok), Toast.LENGTH_SHORT).show();
                }
            }
        });

        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.edit().putBoolean(sob, isChecked).apply();
            }
        });

        new GetPropOperation().execute();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static List<Prop> load_prop(String s){
        List<Prop> props = new ArrayList<Prop>();
        props.clear();
        if(s==null) return props;
        final String p[]=s.split("\0");
        for (String aP : p) {
            try{
                if(aP!=null){
                    String pn=aP;
                    pn=pn.substring(pn.lastIndexOf("/") + 1, pn.length()).trim();
                    if(!pn.equals("run")) {
                        String v=Helpers.readOneLine(aP).trim();
                        if(pn.equals("cpu_governor")) {
                            String[] govs =v.split(" ");
                            if (govs != null) {
                                for (String gov : govs) {
                                    if (gov.charAt(0) == '[') {
                                        v = gov.substring(1, gov.length() - 1);
                                        break;
                                    }
                                }
                            }
                        }
                        props.add(new Prop(pn, v));
                    }
                }
            }
            catch (Exception e){
            }
        }
        return props;
    }

    private class GetPropOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Helpers.get_assetsScript("utils",context,"","");
            new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+getFilesDir()+"/utils" );
            if(new File(path+"/cpu_governor").exists())
                mAvailableGovernors = Helpers.getAvailableIOSchedulers(path+"/cpu_governor");
            CMDProcessor.CommandResult cr =null;
            cr = new CMDProcessor().su.runWaitFor(getFilesDir()+"/utils -getprop \""+path+"/*\" \"1\"");
            if(cr.success()){
                //PropUtil.add_exclude("run");
                props= load_prop(cr.stdout);
                return "ok";
            }
            else{
                Log.d(TAG, pref + " settings error: " + cr.stderr);
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
                    adapter = new PropAdapter(KSMSetActivity.this, R.layout.prop_item, props);
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
                                        mPreferences.edit().remove(pref).apply();
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

        final View editDialog = factory.inflate(R.layout.build_prop_dialog, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
        final EditText tn = (EditText) editDialog.findViewById(R.id.nprop);
        final TextView tt = (TextView) editDialog.findViewById(R.id.text1);
        final Spinner sp = (Spinner) editDialog.findViewById(R.id.spinner);
        final LinearLayout lpresets = (LinearLayout) editDialog.findViewById(R.id.lpresets);
        ArrayAdapter<CharSequence> vAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vAdapter.clear();

        if(pp!=null){
            final String v=pp.getVal();
            final String n=pp.getName();
            lpresets.setVisibility(LinearLayout.GONE);
            if(n.contains("cpu_governor")){
                if(mAvailableGovernors.length>0){
                    vAdapter.add(v);
                    for (String vtcp : mAvailableGovernors) {
                        if(!vtcp.trim().equals(v)) vAdapter.add(vtcp.trim());
                    }
                    lpresets.setVisibility(LinearLayout.VISIBLE);
                    sp.setAdapter(vAdapter);
                }
            }
            tv.setText(pp.getVal());
            tn.setText(pp.getName());
            tn.setVisibility(EditText.GONE);
            tt.setText(pp.getName());
        }
        else{//add
            lpresets.setVisibility(LinearLayout.GONE);
            tt.setText(getString(R.string.prop_name));
            tn.setVisibility(EditText.VISIBLE);
        }
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                tv.setText(sp.getSelectedItem().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_prop_title))
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
                            PropUtil.set_pref(tn.getText().toString().trim(), tv.getText().toString().trim(),pref,mPreferences);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).create().show();
    }


}
