package com.brewcrewfoo.performance.activities;

/**
 * Created by h0rn3t on 01.10.2013.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.brewcrewfoo.performance.R;
import com.brewcrewfoo.performance.util.ActivityThemeChangeInterface;
import com.brewcrewfoo.performance.util.CMDProcessor;
import com.brewcrewfoo.performance.util.Constants;
import com.brewcrewfoo.performance.util.Helpers;
import com.brewcrewfoo.performance.util.Prop;
import com.brewcrewfoo.performance.util.PropAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildPropEditor extends Activity implements Constants, AdapterView.OnItemClickListener, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private final Context context=this;
    Resources res;
    private ListView packList;
    private LinearLayout linlaHeaderProgress;
    private LinearLayout nofiles;
    private RelativeLayout tools,search;
    private PropAdapter adapter=null;
    private EditText filterText = null;
    private List<Prop> props = new ArrayList<Prop>();
    private String[] oggs={};
    private final String dn= Environment.getExternalStorageDirectory().getAbsolutePath()+"/PerformanceControl";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        res = getResources();
        setTheme();
        setContentView(R.layout.prop_view);

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        nofiles = (LinearLayout) findViewById(R.id.nofiles);
        tools = (RelativeLayout) findViewById(R.id.tools);
        search = (RelativeLayout) findViewById(R.id.search);
        filterText = (EditText) findViewById(R.id.filtru);
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adapter!=null)
                adapter.getFilter().filter(filterText.getText().toString());
            }
        });

        tools.setVisibility(View.GONE);
        search.setVisibility(View.GONE);

        new GetPropOperation().execute();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.build_prop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.new_prop:
                editPropDialog(null);
                break;
            case R.id.search_prop:
                search.setVisibility(RelativeLayout.VISIBLE);
                break;
            case R.id.restore_prop:
                makedialog(getString(R.string.prefcat_build_prop),getString(R.string.prop_restore_msg),(byte)0);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        if(search.isShown()){
            search.setVisibility(RelativeLayout.GONE);
            filterText.setText("");
        }
        else{
            finish();
        }
    }

    private class GetPropOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            File f= new File(dn);
            if (!f.exists()) {f.mkdir();}
            f=new File(dn+"/buildprop");
            if (!f.exists()) {f.mkdir();}
            CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox find /system -type f -name \"*.ogg\"");
            oggs=cr.stdout.split("\n");
            final String s = Helpers.readFileViaShell("/system/build.prop", false);
            return s;
        }
        @Override
        protected void onPostExecute(String result) {
            if((result==null)||(result.length()<=0)) {
                finish();
            }
            else{
                load_builprop(result);
                Collections.sort(props);
                linlaHeaderProgress.setVisibility(View.GONE);
                if(props.isEmpty()){
                    nofiles.setVisibility(View.VISIBLE);
                }
                else{
                    nofiles.setVisibility(View.GONE);
                    adapter = new PropAdapter(BuildPropEditor.this, R.layout.prop_item, props);
                    packList.setAdapter(adapter);
                }
            }
        }
        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
            nofiles.setVisibility(View.GONE);
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long row) {
        final Prop p = adapter.getItem(position);
        if(!p.getName().contains("fingerprint"))
            editPropDialog(p);
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
        final Prop pp = p;
        String titlu="";

        LayoutInflater factory = LayoutInflater.from(this);
        final View editDialog = factory.inflate(R.layout.build_prop_dialog, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
        final EditText tn = (EditText) editDialog.findViewById(R.id.nprop);
        final Spinner sp = (Spinner) editDialog.findViewById(R.id.spinner);
        final LinearLayout lpresets = (LinearLayout) editDialog.findViewById(R.id.lpresets);
        ArrayAdapter<CharSequence> vAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vAdapter.clear();

        if(pp!=null){
            final String v=pp.getVal();
            lpresets.setVisibility(LinearLayout.GONE);
            if(v.equals("0")){
                vAdapter.add("0");
                vAdapter.add("1");
                lpresets.setVisibility(LinearLayout.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            else if(v.equals("1")){
                vAdapter.add("1");
                vAdapter.add("0");
                lpresets.setVisibility(LinearLayout.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            else if(v.equalsIgnoreCase("true")){
                vAdapter.add("true");
                vAdapter.add("false");
                lpresets.setVisibility(LinearLayout.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            else if(v.equalsIgnoreCase("false")){
                vAdapter.add("false");
                vAdapter.add("true");
                lpresets.setVisibility(LinearLayout.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            else if(v.contains(".ogg")){
                if(oggs.length>0){
                    vAdapter.add(v);
                    for(int i=0;i<oggs.length;i++){
                        File f = new File(oggs[i]);
                        if(!f.getName().equalsIgnoreCase(v))
                            vAdapter.add(f.getName());
                    }
                    lpresets.setVisibility(LinearLayout.VISIBLE);
                    sp.setAdapter(vAdapter);
                }
            }

            tv.setText(pp.getVal());
            tn.setText(pp.getName());
            titlu=getString(R.string.edit_prop_title);

        }
        else{//add
            titlu=getString(R.string.add_prop_title);
            vAdapter.add("");
            vAdapter.add("0");
            vAdapter.add("1");
            vAdapter.add("true");
            vAdapter.add("false");
            sp.setAdapter(vAdapter);
            lpresets.setVisibility(LinearLayout.VISIBLE);
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
                .setTitle(titlu)
                .setView(editDialog)
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setPositiveButton(getString(R.string.ps_volt_save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helpers.get_assetsScript("utils",context,"","");
                        new CMDProcessor().su.runWaitFor("busybox chmod 750 "+getFilesDir()+"/utils" );

                        if(!new File(dn+"/buildprop/build.prop.bak").exists()){
                            new CMDProcessor().sh.runWaitFor("busybox cp /system/build.prop "+dn+"/buildprop/build.prop.bak" );
                            Toast.makeText(context, getString(R.string.prop_backup, dn), Toast.LENGTH_LONG).show();
                        }
                        if (pp!=null) {
                            if (tv.getText().toString() != null){
                                pp.setVal(tv.getText().toString().trim());
                                new CMDProcessor().su.runWaitFor(getFilesDir()+"/utils -setprop "+pp.getName()+" "+pp.getVal());
                            }
                        }
                        else {
                            if (tv.getText().toString() != null && tn.getText().toString() != null && tn.getText().toString().trim().length() > 0){
                                props.add(new Prop(tn.getText().toString().trim(),tv.getText().toString().trim()));
                                new CMDProcessor().su.runWaitFor(getFilesDir()+"/utils -setprop "+tn.getText().toString().trim()+" "+tv.getText().toString().trim());
                            }
                        }
                        Collections.sort(props);
                        adapter.notifyDataSetChanged();
                    }
                }).create().show();
    }


    private void makedialog(String t,String m,byte op){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t)
                .setMessage(m)
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                //finish();
                            }
                        })
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //alertDialog.setCancelable(false);
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (theButton != null) {
            theButton.setOnClickListener(new CustomListener(alertDialog,op));
        }
    }
    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        private final byte op;
        public CustomListener(Dialog dialog,byte op) {
            this.dialog = dialog;
            this.op=op;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
            switch (op){
                case 0:
                    if(new File(dn+"/buildprop/build.prop.bak").exists()){
                        final StringBuilder sb = new StringBuilder();
                        sb.append("busybox mount -o remount,rw /system").append(";\n");
                        sb.append("busybox cp ").append(dn).append("/buildprop/build.prop.bak /system/build.prop;\n");
                        sb.append("busybox chmod 644 ").append("/system/build.prop;\n");
                        sb.append("busybox mount -o remount,ro /system").append(";\n");
                        Helpers.shExec(sb,context,true);
                        new GetPropOperation().execute();
                    }
                    else{
                        Toast.makeText(context, getString(R.string.prop_no_backup), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }
    public void load_builprop(String s){
        props.clear();
        String p[]=s.split("\n");
        for (String aP : p) {
            if(!aP.contains("#") && aP.trim().length()>0 && aP!=null){
                aP=aP.replace("[","").replace("]","");
                String pp[]=aP.split("=");
                if(pp.length>=2){
                    String r="";
                    for(int i=2;i<pp.length;i++){
                        r=r+"="+pp[i];
                    }
                    props.add(new Prop(pp[0],pp[1].trim()+r));
                }
                else{
                    props.add(new Prop(pp[0],""));
                }
            }
        }
    }
}
