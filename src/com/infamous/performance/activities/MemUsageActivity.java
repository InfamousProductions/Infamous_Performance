package com.infamous.performance.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Prop;
import com.infamous.performance.util.PropAdapter;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by h0rn3t on 10.02.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class MemUsageActivity extends Activity implements  ActivityThemeChangeInterface,Constants {
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private ListView packList;
    private List<Prop> props = new ArrayList<Prop>();
    private PropAdapter adapter;
    private CurThread mCurThread;
    private String path;
    private String titlu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        Intent intent=getIntent();
        final String tip=intent.getStringExtra("tip");
        if(tip.equals("mem")){
            path=MEM_INFO_PATH;
            titlu=getString(R.string.mt_mem_usage);
        }
        else{
            path=CPU_INFO_PATH;
            titlu=getString(R.string.cpu_info);
        }

        setContentView(R.layout.mem_usage);
        TextView t=(TextView) findViewById(R.id.infotxt);
        t.setText(titlu);
        packList = (ListView) findViewById(R.id.memlist);
        readFile(path);
        adapter = new PropAdapter(MemUsageActivity.this, R.layout.mem_item, props);
        packList.setAdapter(adapter);
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
        if (mCurThread == null) {
            mCurThread = new CurThread();
            mCurThread.start();
        }
        super.onResume();
    }
    @Override
    public void onDestroy() {
        if (mCurThread != null) {
            if (mCurThread.isAlive()) {
                mCurThread.interrupt();
                try {
                    mCurThread.join();
                }
                catch (InterruptedException e) {
                }
            }
        }
        super.onDestroy();
    }

    protected class CurThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(1200);
                    mCurHandler.sendMessage(mCurHandler.obtainMessage(0,null));
                }
            }
            catch (InterruptedException e) {
                //return;
            }
        }
    }
    protected Handler mCurHandler = new Handler() {
        public void handleMessage(Message msg) {
            readFile(path);
            adapter.notifyDataSetChanged();
        }
    };
    public void readFile(String fName) {
        props.clear();
        FileReader fr = null;
        try {
            fr = new FileReader(fName);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (null != line) {
                if(line!=null && line.contains(":"))
                    props.add(new Prop(line.split(":")[0].trim(),line.split(":")[1].trim()));
                line = br.readLine();
            }
        }
        catch (IOException ex) {
        }
        finally {
            if (null != fr) {
                try {
                    fr.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}
