package com.infamous.performance.activities;

/**
 * Created by h0rn3t on 21.07.2013.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class FlasherActivity extends Activity implements Constants, ActivityThemeChangeInterface {
    TextView flasherInfo;
    TextView deviceName;
    TextView deviceModel;
    TextView deviceBoard;
    Button chooseBtn,bkBtn;
    ImageView attn;
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    private String part,tip,model,bkname;
    private static ProgressDialog progressDialog;
    private static boolean isdialog=false;
    private String dn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.flasher);

        Intent intent1=getIntent();
        tip=intent1.getStringExtra("mod");

        dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/backup/";

        flasherInfo=(TextView)findViewById(R.id.flashinfo);
        deviceName=(TextView)findViewById(R.id.name);
        deviceModel=(TextView)findViewById(R.id.model);
        deviceBoard=(TextView)findViewById(R.id.board);
        chooseBtn=(Button) findViewById(R.id.chooseBtn);
        bkBtn=(Button) findViewById(R.id.bkBtn);
        //bkBtn.setVisibility(View.GONE);

        model=Build.MODEL;
        deviceModel.setText(model);
        deviceBoard.setText(Build.MANUFACTURER);
        deviceName.setText(Build.DEVICE);//Build.PRODUCT

        if(getPart(model)){
            attn=(ImageView) findViewById(R.id.attn);
            attn.setVisibility(View.GONE);
            if(tip.equalsIgnoreCase("kernel")){
                flasherInfo.setText("boot.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                chooseBtn.setText(getString(R.string.btn_choose,"boot.img"));
            }
            else{
                flasherInfo.setText("recovery.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                chooseBtn.setText(getString(R.string.btn_choose,"recovery.img"));
            }
            chooseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    try{
                        Intent intent2 = new Intent(FlasherActivity.this, FileChooser.class);
                        intent2.putExtra("mod",tip);
                        intent2.putExtra("part",part);
                        startActivity(intent2);
                        //finish();
                    }
                    catch(Exception e){
                        Log.e(TAG,"Error launching filechooser activity");
                    }
                }
            });
            bkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //------backup------
                    LayoutInflater factory = LayoutInflater.from(FlasherActivity.this);
                    final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
                    final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
                    final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);

                    tn.setText(getString(R.string.bk_name));
                    bkname=makeBkName(tip);
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
                    AlertDialog.Builder builder=new AlertDialog.Builder(FlasherActivity.this)
                            .setTitle(getString(R.string.fmt_backup)+" "+tip.toUpperCase())
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
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (theButton != null) {
                        theButton.setOnClickListener(new CustomListener(alertDialog));
                    }
                }
            });
        }
        else{
            chooseBtn.setVisibility(View.GONE);
            bkBtn.setVisibility(View.GONE);
        }
    }
    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            if ((bkname != null) && (bkname.length() > 0)) {
                if (bkname.endsWith("/")) { bkname = bkname.substring(0, bkname.length() - 1);}
                if(!bkname.startsWith("/")) { bkname="/"+bkname; }
                if ( new File(dn+tip+bkname).exists() ){
                    Toast.makeText(FlasherActivity.this,getString(R.string.exist_file,dn+tip+bkname),Toast.LENGTH_LONG).show();
                }
                else{
                    dialog.dismiss();
                    if(!new File(dn+tip+bkname).mkdirs()){
                        Toast.makeText(FlasherActivity.this,getString(R.string.err_file,dn+tip+bkname),Toast.LENGTH_LONG).show();
                    }
                    else {
                        new backupOperation().execute();
                    }
                }
            }

        }
    }

    private static String getValue(String tag, org.w3c.dom.Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodes.item(0);
        return node.getNodeValue();
    }

    private Boolean getPart(String m){
        Boolean gasit=false;
        InputStream f;

        final String fn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/devices.xml";
        try {
            if (new File(fn).exists()){
                f = new BufferedInputStream(new FileInputStream(fn));
                Log.i(TAG,"external /"+TAG+"/devices.xml in use");
            }
            else{
                f = getResources().openRawResource(R.raw.devices);
            }
            DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc=builder.parse(f, null);
            doc.getDocumentElement().normalize();
            NodeList nList=doc.getElementsByTagName("device");
            for (int k = 0; k < nList.getLength(); k++) {
                Node node = nList.item(k);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    final String models[]=getValue("model", element).split(",");
                    for (String mi : models) {
                        if(mi.equalsIgnoreCase(m)){
                            part=getValue(tip, element);
                            gasit=true;
                        }
                    }
                    if(gasit) {
                        Log.d(TAG,tip+" partition detected: "+part);
                        break;
                    }
                }
            }
            f.close();
        }
        catch (Exception e) {
            Log.e(TAG,"Error reading devices.xml");
            gasit=false;
            e.printStackTrace();
        }
        return gasit;
    }

    @Override
    public void onResume() {
        if(isdialog) progressDialog = ProgressDialog.show(FlasherActivity.this, null, getString(R.string.wait));
        super.onResume();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.flasher_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.flash_kernel) {
            tip="kernel";
            if(getPart(model)){
                if(tip.equalsIgnoreCase("kernel")){
                    flasherInfo.setText("boot.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                    chooseBtn.setText(getString(R.string.btn_choose,"boot.img"));
                }
                else{
                    flasherInfo.setText("recovery.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                    chooseBtn.setText(getString(R.string.btn_choose,"recovery.img"));
                }
                chooseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //------------
                        try{
                            Intent intent2 = new Intent(FlasherActivity.this, FileChooser.class);
                            intent2.putExtra("mod",tip);
                            intent2.putExtra("part",part);
                            startActivity(intent2);
                        }
                        catch(Exception e){
                            Log.e(TAG,"Error launching filechooser activity");
                        }
                    }
                });
            }
            else{
                chooseBtn.setVisibility(View.GONE);
                bkBtn.setVisibility(View.GONE);
            }

        }
        if (item.getItemId() == R.id.flash_recovery) {
            tip="recovery";
            if(getPart(model)){
                if(tip.equalsIgnoreCase("kernel")){
                    flasherInfo.setText("boot.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                    chooseBtn.setText(getString(R.string.btn_choose,"boot.img"));
                }
                else{
                    flasherInfo.setText("recovery.img "+getString(R.string.flash_info,part)+" "+tip.toUpperCase());
                    chooseBtn.setText(getString(R.string.btn_choose,"recovery.img"));
                }
                chooseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //------------
                        try{
                            Intent intent2 = new Intent(FlasherActivity.this, FileChooser.class);
                            intent2.putExtra("mod",tip);
                            intent2.putExtra("part",part);
                            startActivity(intent2);
                        }
                        catch(Exception e){
                            Log.e(TAG,"Error launching filechooser activity");
                        }
                    }
                });
            }
            else{
                chooseBtn.setVisibility(View.GONE);
                bkBtn.setVisibility(View.GONE);
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

    private class backupOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            final StringBuilder sb = new StringBuilder();
            if(tip.equalsIgnoreCase("kernel")){
                final File destDir = new File("/system/lib/modules");
                final File[]dirs = destDir.listFiles();
                if((dirs!=null)&&(dirs.length>0)){
                    sb.append("busybox cp /system/lib/modules/*.ko").append(" ").append(dn).append(tip).append(bkname).append(";\n");
                }
                sb.append("dd if=").append(part).append(" of=\"").append(dn).append(tip).append(bkname).append("/boot.img\";\n");
            }
            else{
                sb.append("dd if=").append(part).append(" of=\"").append(dn).append(tip).append(bkname).append("/recovery.img\";\n");
            }

            return Helpers.shExec(sb, FlasherActivity.this, true);
        }
        @Override
        protected void onPostExecute(String result) {
            isdialog=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if((result==null)||!result.equals("nok")){
                Toast.makeText(FlasherActivity.this,getString(R.string.bk_ok),Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(FlasherActivity.this,getString(R.string.bk_nok),Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(FlasherActivity.this, null, getString(R.string.wait));
            isdialog=true;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private String makeBkName(String tip){
        if(tip.equalsIgnoreCase("kernel")) {
            CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox uname -r");
            if (cr.success() && cr.stdout != null && cr.stdout.length() > 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd.HH.mm.ss");
                Date now = new Date();
                return cr.stdout + "_" + formatter.format(now);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
                Date now = new Date();
                return tip + "_" + formatter.format(now);
            }
        }
        else{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
            Date now = new Date();
            return tip + "_" + formatter.format(now);
        }
    }
}
