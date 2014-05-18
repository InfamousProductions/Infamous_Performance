package com.infamous.performance.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;
import java.util.Arrays;

/**
 * Created by h0rn3t on 04.01.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class TouchScreenSettings extends Activity implements Constants, ActivityThemeChangeInterface {

    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    final Context context = this;
    private Switch mt21;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.touch_screen);

        Switch mSetOnBoot = (Switch) findViewById(R.id.swboot);
        mSetOnBoot.setChecked(mPreferences.getBoolean(TOUCHSCREEN_SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                mPreferences.edit().putBoolean(TOUCHSCREEN_SOB, checked).commit();
            }
        });


        Switch mt1 = (Switch) findViewById(R.id.switch1);
        if(!new File(SLIDE2WAKE).exists()){
            mt1.setEnabled(false);
            mPreferences.edit().remove(PREF_SLIDE2WAKE).commit();
        }
        else{
            final String b=Helpers.readOneLine(SLIDE2WAKE);
            mPreferences.edit().putString(PREF_SLIDE2WAKE, b).commit();
            mt1.setChecked(mPreferences.getString(PREF_SWIPE2WAKE, b).equals("1"));
            mt1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + SLIDE2WAKE);
                        mPreferences.edit().putString(PREF_SLIDE2WAKE,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + SLIDE2WAKE);
                        mPreferences.edit().putString(PREF_SLIDE2WAKE, "0").commit();
                    }
                }
            });
        }

        Switch mt2 = (Switch) findViewById(R.id.switch2);
        mt21 = (Switch) findViewById(R.id.switch21);

        if(!new File(SWIPE2WAKE).exists()){
            mt2.setEnabled(false);
            mt21.setEnabled(false);
            mPreferences.edit().remove(PREF_SWIPE2WAKE).commit();
        }
        else{
            final String b=Helpers.readOneLine(SWIPE2WAKE);
            mPreferences.edit().putString(PREF_SWIPE2WAKE, b).commit();

            if(b.equals("2")){
                    mt2.setChecked(false);
                    mt21.setChecked(true);
            }
            else if(b.equals("1")){
                    mt2.setChecked(true);
                    mt21.setChecked(true);
            }
            else{
                    mt2.setChecked(false);
                    mt21.setChecked(false);
            }
            mt2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if (checked) {
                        mt21.setChecked(true);
                        mt21.setEnabled(false);
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + SWIPE2WAKE);
                        mPreferences.edit().putString(PREF_SWIPE2WAKE, "1").commit();
                    } else {
                        mt21.setEnabled(true);
                        if (mt21.isChecked()) {
                            new CMDProcessor().su.runWaitFor("busybox echo 2 > " + SWIPE2WAKE);
                            mPreferences.edit().putString(PREF_SWIPE2WAKE, "2").commit();
                        } else {
                            new CMDProcessor().su.runWaitFor("busybox echo 0 > " + SWIPE2WAKE);
                            mPreferences.edit().putString(PREF_SWIPE2WAKE, "0").commit();
                        }
                    }
                }
            });
            mt21.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        //Toast.makeText(context, "2", Toast.LENGTH_SHORT).show();
                        new CMDProcessor().su.runWaitFor("busybox echo 2 > " + SWIPE2WAKE);
                        mPreferences.edit().putString(PREF_SWIPE2WAKE,"2").commit();
                    }
                    else{
                        //Toast.makeText(context, "0", Toast.LENGTH_SHORT).show();
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + SWIPE2WAKE);
                        mPreferences.edit().putString(PREF_SWIPE2WAKE, "0").commit();
                    }
                }
            });
        }

        Switch mt3 = (Switch) findViewById(R.id.switch3);
        if(!new File(HOME2WAKE).exists()){
            mt3.setEnabled(false);
            mPreferences.edit().remove(PREF_HOME2WAKE).commit();
        }
        else{
            final String b= Helpers.readOneLine(HOME2WAKE);
            mPreferences.edit().putString(PREF_HOME2WAKE,b).commit();
            mt3.setChecked(mPreferences.getString(PREF_HOME2WAKE, b).equals("1"));
            mt3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + HOME2WAKE);
                        mPreferences.edit().putString(PREF_HOME2WAKE,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + HOME2WAKE);
                        mPreferences.edit().putString(PREF_HOME2WAKE, "0").commit();
                    }
                }
            });
        }
        Switch mt4 = (Switch) findViewById(R.id.switch4);
        if(!new File(LOGO2WAKE).exists()){
            mt4.setEnabled(false);
            mPreferences.edit().remove(PREF_LOGO2WAKE).commit();
        }
        else{
            final String b=Helpers.readOneLine(LOGO2WAKE);
            mPreferences.edit().putString(PREF_LOGO2WAKE, b).commit();
            mt4.setChecked(mPreferences.getString(PREF_LOGO2WAKE, b).equals("1"));
            mt4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + LOGO2WAKE);
                        mPreferences.edit().putString(PREF_LOGO2WAKE,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + LOGO2WAKE);
                        mPreferences.edit().putString(PREF_LOGO2WAKE, "0").commit();
                    }
                }
            });
        }
        Switch mt5 = (Switch) findViewById(R.id.switch5);
        if(!new File(LOGO2MENU).exists()){
            mt5.setEnabled(false);
            mPreferences.edit().remove(PREF_LOGO2MENU).commit();
        }
        else{
            final String b=Helpers.readOneLine(LOGO2MENU);
            mPreferences.edit().putString(PREF_LOGO2MENU, b).commit();
            mt5.setChecked(mPreferences.getString(PREF_LOGO2MENU, b).equals("1"));
            mt5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + LOGO2MENU);
                        mPreferences.edit().putString(PREF_LOGO2MENU,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + LOGO2MENU);
                        mPreferences.edit().putString(PREF_LOGO2MENU, "0").commit();
                    }
                }
            });
        }
        Switch mt6 = (Switch) findViewById(R.id.switch6);
        if(!new File(DOUBLETAP2WAKE).exists()){
            mt6.setEnabled(false);
            mPreferences.edit().remove(PREF_DOUBLETAP2WAKE).commit();
        }
        else{
            final String b=Helpers.readOneLine(DOUBLETAP2WAKE);
            mPreferences.edit().putString(PREF_DOUBLETAP2WAKE, b).commit();
            mt6.setChecked(mPreferences.getString(PREF_DOUBLETAP2WAKE,b).equals("1")||mPreferences.getString(PREF_DOUBLETAP2WAKE,b).equals("Y"));
            mt6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        if(b.equals("0")||b.equals("1")){
                            new CMDProcessor().su.runWaitFor("busybox echo 1 > " + DOUBLETAP2WAKE);
                            mPreferences.edit().putString(PREF_DOUBLETAP2WAKE,"1").commit();
                        }
                        else{
                            new CMDProcessor().su.runWaitFor("busybox echo \"Y\" > " + DOUBLETAP2WAKE);
                            mPreferences.edit().putString(PREF_DOUBLETAP2WAKE,"Y").commit();
                        }
                    }
                    else{
                        if(b.equals("0")||b.equals("1")){
                            new CMDProcessor().su.runWaitFor("busybox echo 0 > " +DOUBLETAP2WAKE);
                            mPreferences.edit().putString(PREF_DOUBLETAP2WAKE, "0").commit();
                        }
                        else{
                            new CMDProcessor().su.runWaitFor("busybox echo \"N\" > " +DOUBLETAP2WAKE);
                            mPreferences.edit().putString(PREF_DOUBLETAP2WAKE, "N").commit();
                        }
                    }
                }
            });
        }
        Switch mt7 = (Switch) findViewById(R.id.switch7);
        if(!new File(POCKET_DETECT).exists()){
            mt7.setEnabled(false);
            mPreferences.edit().remove(PREF_POCKET_DETECT).commit();
        }
        else{
            mPreferences.edit().putString(PREF_POCKET_DETECT, Helpers.readOneLine(POCKET_DETECT)).commit();
            mt7.setChecked(mPreferences.getString(PREF_POCKET_DETECT, Helpers.readOneLine(POCKET_DETECT)).equals("1"));
            mt7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + POCKET_DETECT);
                        mPreferences.edit().putString(PREF_POCKET_DETECT,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " +POCKET_DETECT);
                        mPreferences.edit().putString(PREF_POCKET_DETECT, "0").commit();
                    }
                }
            });
        }
        Switch mt8 = (Switch) findViewById(R.id.switch8);
        if(!new File(PICK2WAKE).exists()){
            mt8.setEnabled(false);
            mPreferences.edit().remove(PREF_PICK2WAKE).commit();
        }
        else{
            mPreferences.edit().putString(PREF_PICK2WAKE, Helpers.readOneLine(PICK2WAKE)).commit();
            mt8.setChecked(mPreferences.getString(PREF_PICK2WAKE, Helpers.readOneLine(PICK2WAKE)).equals("1"));
            mt8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + PICK2WAKE);
                        mPreferences.edit().putString(PREF_PICK2WAKE,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " +PICK2WAKE);
                        mPreferences.edit().putString(PREF_PICK2WAKE, "0").commit();
                    }
                }
            });
        }
        Switch mt9 = (Switch) findViewById(R.id.switch9);
        RelativeLayout f2s=(RelativeLayout) findViewById(R.id.f2s);
        Spinner mSens = (Spinner) findViewById(R.id.spinsens);
        mSens.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                new CMDProcessor().su.runWaitFor("busybox echo "+Integer.toString(position)+" > " + FLICK2SLEEP_SENSITIVE);
                mPreferences.edit().putString(PREF_FLICK2SLEEP_SENSITIVE, Integer.toString(position)).commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });

        if(!new File(FLICK2SLEEP).exists()){
            mt9.setEnabled(false);
            f2s.setVisibility(RelativeLayout.GONE);
            mPreferences.edit().remove(PREF_FLICK2SLEEP).commit();
        }
        else{
            if(new File(FLICK2SLEEP_SENSITIVE).exists()){
                CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox echo `awk -F'[][]' '{print $2}' /sys/devices/virtual/htc_g_sensor/g_sensor/f2w_sensitivity_values`");
                if(cr.success()&&(cr.stdout.equals("0")||(cr.stdout.equals("1")))){
                    f2s.setVisibility(RelativeLayout.VISIBLE);
                    mSens.setSelection(Integer.valueOf(cr.stdout));
                    mPreferences.edit().putString(PREF_FLICK2SLEEP_SENSITIVE, cr.stdout).commit();
                }
            }
            mPreferences.edit().putString(PREF_FLICK2SLEEP, Helpers.readOneLine(FLICK2SLEEP)).commit();
            mt9.setChecked(mPreferences.getString(PREF_FLICK2SLEEP, Helpers.readOneLine(FLICK2SLEEP)).equals("1"));
            mt9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + FLICK2SLEEP);
                        mPreferences.edit().putString(PREF_FLICK2SLEEP,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " +FLICK2SLEEP);
                        mPreferences.edit().putString(PREF_FLICK2SLEEP, "0").commit();
                    }
                }
            });
        }
        Switch mt10 = (Switch) findViewById(R.id.switch10);
        if(Helpers.touch2wake_path()==null){
            mt10.setEnabled(false);
            mPreferences.edit().remove(PREF_TOUCH2WAKE).commit();
        }
        else{
            final String touch2wakepath=Helpers.touch2wake_path();
            mPreferences.edit().putString(PREF_TOUCH2WAKE, Helpers.readOneLine(touch2wakepath)).commit();
            mt10.setChecked(mPreferences.getString(PREF_TOUCH2WAKE, Helpers.readOneLine(touch2wakepath)).equals("1"));
            mt10.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    if(checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + touch2wakepath);
                        mPreferences.edit().putString(PREF_TOUCH2WAKE,"1").commit();
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " +touch2wakepath);
                        mPreferences.edit().putString(PREF_TOUCH2WAKE, "0").commit();
                    }
                }
            });
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
}

