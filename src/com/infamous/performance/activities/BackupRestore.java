package com.infamous.performance.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;

import com.infamous.performance.util.BootClass;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.FileArrayAdapter;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.Item;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by H0RN3T on 15.07.2014.
 */
public class BackupRestore extends Activity implements Constants,AdapterView.OnItemClickListener, ActivityThemeChangeInterface {
    final Context context = this;
    private File currentDir;
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    private FileArrayAdapter adapter;
    private static ProgressDialog progressDialog;
    private static boolean isdialog=false;
    private ListView packList;
    private LinearLayout nodata;
    private String bkname;
    private String fname="pc_settings";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.backup_view);

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        packList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Item o = adapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.bkup_del_title))
                        .setMessage(getString(R.string.del_prop_msg,o.getName()))
                        .setNegativeButton(getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox rm -r " + currentDir + "/" + o.getName());
                                        if (cr.success()) {
                                            adapter.remove(o);
                                            adapter.notifyDataSetChanged();
                                            if(adapter.getCount()<=0) nodata.setVisibility(LinearLayout.VISIBLE);
                                        }
                                    }
                                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

        nodata=(LinearLayout) findViewById(R.id.nofiles);

        currentDir=new  File(mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/backup/settings");
        currentDir.mkdirs();
        fill(currentDir);

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.backup_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addbackup) {
            //------backup------
            LayoutInflater factory = LayoutInflater.from(BackupRestore.this);
            final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
            final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
            final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
            java.util.Date now = new java.util.Date();

            tn.setText(getString(R.string.bk_name));
            bkname="pc_" + formatter.format(now);
            tv.setText(bkname);
            tv.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    bkname=tv.getText().toString();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            AlertDialog.Builder builder=new AlertDialog.Builder(BackupRestore.this)
                    .setTitle(getString(R.string.create_bkup))
                    .setView(editDialog)
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.fmt_backup), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ((bkname != null) && (bkname.length() > 0)) {
                                if (bkname.endsWith("/")) { bkname = bkname.substring(0, bkname.length() - 1);}
                                if(!bkname.startsWith("/")) { bkname="/"+bkname; }
                                final File b= new File(currentDir+bkname);
                                if ( b.exists() ){
                                    Toast.makeText(BackupRestore.this,getString(R.string.exist_file,currentDir+bkname),Toast.LENGTH_LONG).show();
                                }
                                else{
                                    dialog.dismiss();
                                    if(!new File(currentDir+bkname).mkdirs()){
                                        Toast.makeText(BackupRestore.this,getString(R.string.err_file,currentDir+bkname),Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        create_backup(currentDir+bkname);
                                        fill(currentDir);
                                    }
                                }
                            }
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        return true;
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fill(currentDir);
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
        if(isdialog) progressDialog = ProgressDialog.show(BackupRestore.this, null, getString(R.string.wait));
        super.onResume();
    }

    private void fill(File f){
        File[]dirs = f.listFiles();
        List<Item> dir = new ArrayList<Item>();
        try{
            assert dirs != null;
            for(File ff: dirs){
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if(ff.isDirectory()){
                    dir.add(new Item(ff.getName(),date_modify,null,null,"dir"));
                }
            }
        }
        catch(Exception e){
        }
        Collections.sort(dir);

        adapter = new FileArrayAdapter(this,R.layout.file_item, dir);
        packList.setAdapter(adapter);
        if(adapter.getCount()<=0) nodata.setVisibility(LinearLayout.VISIBLE);
        else nodata.setVisibility(LinearLayout.GONE);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long row) {
        Item o = adapter.getItem(position);
        makedialog(o.getName());

    }

    private void makedialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.restore_bkup_title)
                .setMessage(getString(R.string.restore_bkup, s))
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
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
            theButton.setOnClickListener(new RestoreListener(alertDialog,s));
        }
    }

    class RestoreListener implements View.OnClickListener {
        private final Dialog dialog;
        private final String bn;
        public RestoreListener(Dialog dialog,String s) {
            this.dialog = dialog;
            this.bn=s;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
            new RestoreOperation().execute(bn);
        }
    }
    private class RestoreOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(currentDir+"/"+params[0]+"/"+fname));
                String line;
                mPreferences.edit().clear().commit();
                while((line = reader.readLine()) != null){
                    if(line.contains(":")){
                        String items[]=line.split("=");
                        String tip=items[0].split(":")[1];
                        String nume=items[0].split(":")[0];
                        if(tip.equals("String")){
                            mPreferences.edit().putString(nume,items[1].replace("\\n","\n")).commit();
                        }
                        else if(tip.equals("Integer")){
                            mPreferences.edit().putInt(nume,Integer.valueOf(items[1])).commit();
                        }
                        else if(tip.equals("Long")){
                            mPreferences.edit().putLong(nume,Long.valueOf(items[1])).commit();
                        }
                        else if(tip.equals("Boolean")){
                            mPreferences.edit().putBoolean(nume,Boolean.valueOf(items[1])).commit();
                        }
                    }

                }
                reader.close();
            }
            catch (Exception e) {
                return "nok";
            }
            Helpers.shExec(new BootClass(context, mPreferences).getScript(), context, true);
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            isdialog=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if(result.equals("ok")) {
                MainActivity.is_restored=true;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(BackupRestore.this, null, getString(R.string.wait));
            isdialog=true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private void create_backup(String fn){
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> entry : mPreferences.getAll().entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                sb.append(entry.getKey()+"\n");
            } else {
                sb.append(entry.getKey()+":"+val.getClass().getSimpleName()+"="+String.valueOf(val).replace("\n","\\n")+"\n");
            }
        }

        try {
            FileWriter file = new FileWriter(fn+"/"+fname);
            file.write(sb.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
