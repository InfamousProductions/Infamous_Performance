package com.infamous.performance.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

/**
 * Created by h0rn3t on 01.02.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class PFKActivity extends Activity implements ActivityThemeChangeInterface, Constants {
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    final Context context = this;

    private TextView mh1;
    private TextView mh2;
    private TextView mh3;
    private TextView mh4;
    private TextView mh5;
    private TextView mh6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.pfk_settings);

        TextView mt = (TextView) findViewById(R.id.ihome);
        mt.setText(getString(R.string.ps_home_enabled, ""));
        mh1 = (TextView) findViewById(R.id.hview);
        mh1.setText(Helpers.readOneLine(PFK_HOME_IGNORED_KP));

        mt =(TextView)findViewById(R.id.hval1);
        mt.setText(getString(R.string.home_allowed_irq_title));
        mh1 = (TextView) findViewById(R.id.hview1);
        mh1.setText(Helpers.readOneLine(PFK_HOME_ALLOWED_IRQ));

        mt =(TextView)findViewById(R.id.hval2);
        mt.setText(getString(R.string.home_report_wait_title));
        mh2 = (TextView) findViewById(R.id.hview2);
        mh2.setText(Helpers.readOneLine(PFK_HOME_REPORT_WAIT));

        final RelativeLayout r1 = (RelativeLayout) findViewById(R.id.lhome2);
        r1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.home_allowed_irq_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(PFK_HOME_ALLOWED_IRQ));
                openDialog(currentProgress, title, 1, 32, PFK_HOME_ALLOWED_IRQ, PREF_HOME_ALLOWED_IRQ, mh1);
            }
        });

        final RelativeLayout r2 = (RelativeLayout) findViewById(R.id.lhome3);
        r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.home_report_wait_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(PFK_HOME_REPORT_WAIT));
                openDialog(currentProgress, title, 5, 25, PFK_HOME_REPORT_WAIT, PREF_HOME_REPORT_WAIT, mh2);
            }
        });

        mt =(TextView)findViewById(R.id.imenu);
        mt.setText(getString(R.string.ps_menuback_enabled, ""));
        mh3 = (TextView) findViewById(R.id.mview);
        mh3.setText(Helpers.readOneLine(PFK_MENUBACK_IGNORED_KP));

        mt =(TextView)findViewById(R.id.mval1);
        mt.setText(getString(R.string.menuback_interrupt_checks_title));
        mh4 = (TextView) findViewById(R.id.mview1);
        mh4.setText(Helpers.readOneLine(PFK_MENUBACK_INTERRUPT_CHECKS));

        mt =(TextView)findViewById(R.id.mval2);
        mt.setText(getString(R.string.menuback_first_err_wait_title));
        mh5 = (TextView) findViewById(R.id.mview2);
        mh5.setText(Helpers.readOneLine(PFK_MENUBACK_FIRST_ERR_WAIT));

        mt =(TextView)findViewById(R.id.mval3);
        mt.setText(getString(R.string.menuback_last_err_wait_title));
        mh6 = (TextView) findViewById(R.id.mview3);
        mh6.setText(Helpers.readOneLine(PFK_MENUBACK_LAST_ERR_WAIT));

        final RelativeLayout r3 = (RelativeLayout) findViewById(R.id.lmenu2);
        r3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.menuback_interrupt_checks_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_INTERRUPT_CHECKS));
                openDialog(currentProgress, title, 1, 10, PFK_MENUBACK_INTERRUPT_CHECKS, PREF_MENUBACK_INTERRUPT_CHECKS, mh4);
            }
        });
        final RelativeLayout r4 = (RelativeLayout) findViewById(R.id.lmenu3);
        r4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.menuback_first_err_wait_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_FIRST_ERR_WAIT));
                openDialog(currentProgress, title, 50, 1000, PFK_MENUBACK_FIRST_ERR_WAIT, PREF_MENUBACK_FIRST_ERR_WAIT, mh5);
            }
        });
        final RelativeLayout r5 = (RelativeLayout) findViewById(R.id.lmenu4);
        r5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.menuback_last_err_wait_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_LAST_ERR_WAIT));
                openDialog(currentProgress, title, 50, 100, PFK_MENUBACK_LAST_ERR_WAIT, PREF_MENUBACK_LAST_ERR_WAIT, mh6);
            }
        });
        final Switch m1 = (Switch) findViewById(R.id.switchhome);
        m1.setChecked(mPreferences.getBoolean(PFK_HOME_ON, Helpers.readOneLine(PFK_HOME_ENABLED).equals("1")));
        m1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                mPreferences.edit().putBoolean(PFK_HOME_ON, checked).apply();
                if (checked) {
                    new CMDProcessor().su.runWaitFor("busybox echo 1 > " + PFK_HOME_ENABLED);
                }
                else{
                    new CMDProcessor().su.runWaitFor("busybox echo 0 > " + PFK_HOME_ENABLED);
                }
            }
        });
        final Switch m2 = (Switch) findViewById(R.id.switchmenu);
        m2.setChecked(mPreferences.getBoolean(PFK_MENUBACK_ON, Helpers.readOneLine(PFK_MENUBACK_ENABLED).equals("1")));
        m2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                mPreferences.edit().putBoolean(PFK_MENUBACK_ON, checked).apply();
                if (checked) {
                    new CMDProcessor().su.runWaitFor("busybox echo 1 > " + PFK_MENUBACK_ENABLED);
                }
                else{
                    new CMDProcessor().su.runWaitFor("busybox echo 0 > " + PFK_MENUBACK_ENABLED);
                }
            }
        });
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


    public void openDialog(int currentProgress, String title, final int min, final int max,final String path, final String key,final TextView e) {
        Resources res = context.getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        LayoutInflater factory = LayoutInflater.from(context); 
        
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);

        seekbar.setMax(max-min);
        if(currentProgress>max) currentProgress=max-min;
        else if(currentProgress<min) currentProgress=0;
        else currentProgress=currentProgress-min;

        seekbar.setProgress(currentProgress);

        final EditText settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setText(Integer.toString(currentProgress+min));

        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int val = Integer.parseInt(settingText.getText().toString())-min;
                    seekbar.setProgress(val);
                    return true;
                }
                return false;
            }
        });

        settingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val > max) {
                        s.replace(0, s.length(), Integer.toString(max));
                        val=max;
                    }
                    seekbar.setProgress(val-min);
                }
                catch (NumberFormatException ex) {
                }
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                final int mSeekbarProgress = seekbar.getProgress();
                if(fromUser){
                    settingText.setText(Integer.toString(mSeekbarProgress+min));
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }
        };
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing
                            }
                        })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int val = min;
                        if (!settingText.getText().toString().equals(""))
                            val = Integer.parseInt(settingText.getText().toString());
                        if (val < min) val = min;
                        seekbar.setProgress(val - min);
                        int newProgress = seekbar.getProgress() + min;
                        new CMDProcessor().su.runWaitFor("busybox echo " + Integer.toString(newProgress) + " > " + path);
                        final String v=Helpers.readOneLine(path);
                        mPreferences.edit().putInt(key, Integer.parseInt(v)).commit();
                        e.setText(v);

                    }
                }).create().show();
    }
}
