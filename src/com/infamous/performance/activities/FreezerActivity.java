package com.infamous.performance.activities;
/**
 * Created by h0rn3t on 15.09.2013.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.PackAdapter;
import com.infamous.performance.util.PackItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class FreezerActivity extends Activity implements Constants, AdapterView.OnItemClickListener,ActivityThemeChangeInterface {

    final Context context = this;
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private LinearLayout linlaHeaderProgress;
    private LinearLayout linNopack,llist;
    private TextView itxt;
    private ArrayList<PackItem> list = new ArrayList<PackItem>();
    private ListView packList;
    private PackAdapter adapter;
    private int curpos;
    private Boolean freeze;
    private String pn;
    private String titlu;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.freezer_list);

        Intent i=getIntent();
        freeze=i.getBooleanExtra("freeze",false);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linNopack = (LinearLayout) findViewById(R.id.noproc);

        llist = (LinearLayout) findViewById(R.id.llist);
        itxt = (TextView) findViewById(R.id.infotxt);

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        if(freeze) {
            titlu=getString(R.string.pt_freeze);
        }
        else{
            titlu=getString(R.string.pt_unfreeze);
        }
        new GetPacksOperation().execute();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long row) {
        pn=list.get(position).getPackName();
        curpos=position;
        if(freeze) {
            makedialog(titlu,getString(R.string.freeze_msg, pn));
        }
        else{
            makedialog(titlu,getString(R.string.unfreeze_msg,pn));
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    private class GetPacksOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            CMDProcessor.CommandResult cr;
            if(!freeze){
                cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -d | cut -d':' -f2`");
            }
            else{
                cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -e | cut -d':' -f2`");
                /*if(packs.equals("sys")){
                    cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -s -e | cut -d':' -f2`");
                }
                else{
                    cr=new CMDProcessor().sh.runWaitFor("busybox echo `pm list packages -3 -e | cut -d':' -f2`");
                }*/
            }
            list.clear();
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
                adapter = new PackAdapter(FreezerActivity.this, list);
                packList.setAdapter(adapter);
                linNopack.setVisibility(View.GONE);
                llist.setVisibility(LinearLayout.VISIBLE);
                if(!freeze){
                    itxt.setText(getString(R.string.pt_unfreeze));
                }
                else{
                    itxt.setText(getString(R.string.pt_freeze));
                }
            }
            else{
                linNopack.setVisibility(View.VISIBLE);
            }
            linlaHeaderProgress.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
            linNopack.setVisibility(View.GONE);
            llist.setVisibility(LinearLayout.GONE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void makedialog(String titlu,String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titlu)
                .setMessage(msg)
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
            theButton.setOnClickListener(new FreezeListener(alertDialog));
        }
    }
    class FreezeListener implements View.OnClickListener {
        private final Dialog dialog;
        public FreezeListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
            new FreezeOperation().execute();
        }
    }
    private class FreezeOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            CMDProcessor.CommandResult cr;
            if(freeze){
                cr = new CMDProcessor().su.runWaitFor("pm disable " + pn+ " 2> /dev/null");
            }
            else{
                cr = new CMDProcessor().su.runWaitFor("pm enable " + pn+ " 2> /dev/null");
            }
            if(cr.success()){ return "ok";}
            else{return "nok";}
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if(result.equals("ok")){
                adapter.delItem(curpos);
                if(adapter.isEmpty()){
                    llist.setVisibility(LinearLayout.GONE);
                    linNopack.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, getString(R.string.wait));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


}
