package com.infamous.performance.activities;
/**
 * Created by h0rn3t on 11.09.2013.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;

public class KSMActivity extends Activity implements Constants, ActivityThemeChangeInterface {
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    final Context context = this;
    private TextView t1,t2,t3,t4,t5,t6,t7;
    private CurThread mCurThread;
    private Boolean ist1=false;
    private Boolean ist2=false;
    private Boolean ist3=false;
    private Boolean ist4=false;
    private Boolean ist5=false;
    private Boolean ist6=false;
    private Boolean ist7=false;
    private int ksm=0;
    private String ksmpath=KSM_RUN_PATH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.ksm_settings);
        t1=(TextView)findViewById(R.id.t2);
        t2=(TextView)findViewById(R.id.t4);
        t3=(TextView)findViewById(R.id.t6);
        t4=(TextView)findViewById(R.id.t8);
        t5=(TextView)findViewById(R.id.t10);
        t6=(TextView)findViewById(R.id.t12);
        t7=(TextView)findViewById(R.id.t14);
        if(new File(UKSM_RUN_PATH+"/run").exists()){
            ksm=1;
            ksmpath=UKSM_RUN_PATH;
        }
        if (new File(KSM_PAGESSHARED_PATH[ksm]).exists()) {
            t1.setText(Helpers.readOneLine(KSM_PAGESSHARED_PATH[ksm]));
            ist1=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout1);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }

        if (new File(KSM_PAGESSHARED_PATH[ksm]).exists()) {
            t2.setText(Helpers.readOneLine(KSM_PAGESUNSHERED_PATH[ksm]));
            ist2=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout2);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        if (new File(KSM_PAGESSHARING_PATH[ksm]).exists()) {
            t3.setText(Helpers.readOneLine(KSM_PAGESSHARING_PATH[ksm]));
            ist3=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout3);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        if (new File(KSM_PAGESVOLATILE_PATH[ksm]).exists()) {
            t4.setText(Helpers.readOneLine(KSM_PAGESVOLATILE_PATH[ksm]));
            ist4=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout4);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        if (new File(KSM_FULLSCANS_PATH[ksm]).exists()) {
            t5.setText(Helpers.readOneLine(KSM_FULLSCANS_PATH[ksm]));
            ist5=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout5);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        if (new File(KSM_SLEEP_TIMES_PATH[ksm]).exists()) {
            t6.setText(Helpers.readOneLine(KSM_SLEEP_TIMES_PATH[ksm]));
            ist6=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout6);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        if (new File(KSM_PAGESSCANNED_PATH[ksm]).exists()) {
            t7.setText(Helpers.readOneLine(KSM_PAGESSCANNED_PATH[ksm]));
            ist7=true;
        }
        else{
            LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout7);
            relativeLayout.setVisibility(LinearLayout.GONE);
        }
        ((Button) findViewById(R.id.rst)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        final String vlast=Helpers.readOneLine(ksmpath+"/run");
                        final StringBuilder sb = new StringBuilder();
                        sb.append("busybox echo 0 > ").append(ksmpath).append("/run;\n").append("sleep 0.5;\n");
                        sb.append("busybox echo 2 > ").append(ksmpath).append("/run;\n").append("sleep 0.5;\n");
                        sb.append("busybox echo ").append(vlast).append(" > ").append(ksmpath).append("/run;\n");
                        Helpers.shExec(sb,context,true);

                    }
                };
                new Thread(runnable).start();
            }
        });
        ((Button) findViewById(R.id.ksm_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, KSMSetActivity.class);
                i.putExtra("path",ksmpath);
                i.putExtra("sob",KSM_SOB);
                i.putExtra("pref","pref_ksm");
                startActivity(i);
            }
        });
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

    protected class CurThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(800);
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
            if (ist1) t1.setText(Helpers.readOneLine(KSM_PAGESSHARED_PATH[ksm]));
            if (ist2) t2.setText(Helpers.readOneLine(KSM_PAGESUNSHERED_PATH[ksm]));
            if (ist3) t3.setText(Helpers.readOneLine(KSM_PAGESSHARING_PATH[ksm]));
            if (ist4) t4.setText(Helpers.readOneLine(KSM_PAGESVOLATILE_PATH[ksm]));
            if (ist5) t5.setText(Helpers.readOneLine(KSM_FULLSCANS_PATH[ksm]));
            if (ist6) t6.setText(Helpers.readOneLine(KSM_SLEEP_TIMES_PATH[ksm]));
            if (ist7) t7.setText(Helpers.readOneLine(KSM_PAGESSCANNED_PATH[ksm]));
        }
    };


}
