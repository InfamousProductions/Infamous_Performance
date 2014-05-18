/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.infamous.performance.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.*;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.activities.PFKActivity;
import com.infamous.performance.activities.TouchScreenSettings;
import com.infamous.performance.activities.VMSettings;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.VibratorClass;

import java.io.File;

public class Advanced extends PreferenceFragment implements OnSharedPreferenceChangeListener, Constants {
    SharedPreferences mPreferences;
	private Preference mBltimeout,mViber,mPFK,mDynamicWriteBackActive,mDynamicWriteBackSuspend,mVM,mTouchScr;
	private CheckBoxPreference mBltouch;

    private CheckBoxPreference mBln,mDynamicWriteBackOn,mDsync,mWifiPM;
	private ListPreference mReadAhead;
	private int mSeekbarProgress;
	private EditText settingText;
	private String sreadahead;
    private String BLN_PATH,VIBE_PATH,WIFIPM_PATH;
    private Context context;
    VibratorClass vib=new VibratorClass();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.advanced);
        
	    sreadahead=getResources().getString(R.string.ps_read_ahead,"");

        mReadAhead = (ListPreference) findPreference(PREF_READ_AHEAD);
        mBltimeout= findPreference(PREF_BLTIMEOUT);
        mBltouch=(CheckBoxPreference) findPreference(PREF_BLTOUCH);
        mBln=(CheckBoxPreference) findPreference(PREF_BLN);
        mWifiPM=(CheckBoxPreference) findPreference("pref_wifi_pm");
        mTouchScr=findPreference("touchscr_settings");
        mViber= findPreference("pref_viber");
        mVM= findPreference("vm_settings");

        mDsync=(CheckBoxPreference) findPreference(PREF_DSYNC);

        mPFK = findPreference("pfk_settings");

        mDynamicWriteBackOn = (CheckBoxPreference) findPreference(PREF_DYNAMIC_DIRTY_WRITEBACK);
        mDynamicWriteBackActive = findPreference(PREF_DIRTY_WRITEBACK_ACTIVE);
        mDynamicWriteBackSuspend = findPreference(PREF_DIRTY_WRITEBACK_SUSPEND);
		

        if (!new File(DSYNC_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("dsync");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mDsync.setChecked(Helpers.readOneLine(DSYNC_PATH).equals("1"));
        }
        if (!new File(PFK_HOME_ENABLED).exists() || !new File(PFK_MENUBACK_ENABLED).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("pfk");
            getPreferenceScreen().removePreference(hideCat);
        }

        if (!new File(BL_TIMEOUT_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("bltimeout");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mBltimeout.setSummary(Helpers.readOneLine(BL_TIMEOUT_PATH));
        }

        if (!new File(BL_TOUCH_ON_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("bltouch");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mBltouch.setChecked(Helpers.readOneLine(BL_TOUCH_ON_PATH).equals("1"));
        }

        BLN_PATH=Helpers.bln_path();
        if (BLN_PATH==null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("bln");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mBln.setChecked(Helpers.readOneLine(BLN_PATH).equals("1"));
        }

        if (no_touchscreen()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("touch_scr");
            getPreferenceScreen().removePreference(hideCat);
        }


        VIBE_PATH=vib.get_path();

        if (VIBE_PATH==null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("viber");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mViber.setSummary(vib.get_val(VIBE_PATH));
        }

        if (!new File(DYNAMIC_DIRTY_WRITEBACK_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("cat_dynamic_write_back");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            boolean ison=Helpers.readOneLine(DYNAMIC_DIRTY_WRITEBACK_PATH).equals("1");
            mDynamicWriteBackOn.setChecked(ison);
            mDynamicWriteBackActive.setSummary(Helpers.readOneLine(DIRTY_WRITEBACK_ACTIVE_PATH));
            mDynamicWriteBackSuspend.setSummary(Helpers.readOneLine(DIRTY_WRITEBACK_SUSPEND_PATH));
        }
        WIFIPM_PATH=Helpers.wifipm_path();
        if (WIFIPM_PATH==null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("wifi_pm");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mWifiPM.setChecked(Helpers.readOneLine(WIFIPM_PATH).equals("1"));
        }
		final String readahead=Helpers.readOneLine(READ_AHEAD_PATH);
	    mReadAhead.setValue(readahead);
        mReadAhead.setSummary(getString(R.string.ps_read_ahead, readahead + "  kb"));

            
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab),(ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, PCSettings.class);
                startActivity(intent);
            break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mDsync){
            if (Helpers.readOneLine(DSYNC_PATH).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + DSYNC_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + DSYNC_PATH);
            }
                return true;
        }
        else if (preference == mBltimeout){
                String title = getString(R.string.bltimeout_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(BL_TIMEOUT_PATH));
                openDialog(currentProgress, title, 0,5000, preference,BL_TIMEOUT_PATH, PREF_BLTIMEOUT);
                return true;
        }
        else if (preference == mBltouch){
            if (Helpers.readOneLine(BL_TOUCH_ON_PATH).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + BL_TOUCH_ON_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + BL_TOUCH_ON_PATH);
            }
            return true;
        }
        else if (preference == mBln){
            if (Helpers.readOneLine(BLN_PATH).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + BLN_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + BLN_PATH);
            }
            return true;
        }
        else if (preference == mTouchScr) {
            Intent intent = new Intent(context, TouchScreenSettings.class);
            startActivity(intent);
            return true;
        }
        else if (preference == mViber){
            String title = getString(R.string.viber_title);
            int currentProgress = Integer.parseInt(vib.get_val(VIBE_PATH));
            openDialog(currentProgress, title, vib.get_min(),vib.get_max(), preference,VIBE_PATH, "pref_viber");
            return true;
        }
        else if (preference == mPFK){
            Intent intent = new Intent(context, PFKActivity.class);
            startActivity(intent);
            return true;
        }
        else if (preference == mDynamicWriteBackOn){
            if (Helpers.readOneLine(DYNAMIC_DIRTY_WRITEBACK_PATH).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + DYNAMIC_DIRTY_WRITEBACK_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + DYNAMIC_DIRTY_WRITEBACK_PATH);
            }
                return true;
        }
        else if (preference == mDynamicWriteBackActive) {
                String title = getString(R.string.dynamic_writeback_active_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_ACTIVE_PATH));
                openDialog(currentProgress, title, 0,5000, preference,DIRTY_WRITEBACK_ACTIVE_PATH, PREF_DIRTY_WRITEBACK_ACTIVE);
                return true;
        }
        else if (preference == mDynamicWriteBackSuspend) {
                String title = getString(R.string.dynamic_writeback_suspend_title);
                int currentProgress = Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_SUSPEND_PATH));
                openDialog(currentProgress, title, 0,5000, preference,DIRTY_WRITEBACK_SUSPEND_PATH, PREF_DIRTY_WRITEBACK_SUSPEND);
                return true;
        }
        else if (preference == mVM) {
            Intent intent = new Intent(context, VMSettings.class);
            startActivity(intent);
            return true;
        }
        else if (preference == mWifiPM){
            if (Helpers.readOneLine(WIFIPM_PATH).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + WIFIPM_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + WIFIPM_PATH);
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		if (key.equals(PREF_READ_AHEAD)) {
			final String values = mReadAhead.getValue();
			if (!values.equals(Helpers.readOneLine(READ_AHEAD_PATH))){
                for(byte i=0;i<2;i++){
                    if(new File(READ_AHEAD_PATH.replace("mmcblk0","mmcblk"+i)).exists())
                        new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + READ_AHEAD_PATH.replace("mmcblk0","mmcblk"+i));
                }
			}
			mReadAhead.setSummary(sreadahead+values + " kb");
		}	

		else if (key.equals(PREF_BLTIMEOUT)) {
			mBltimeout.setSummary(Helpers.readOneLine(BL_TIMEOUT_PATH));
		}

    	else if (key.equals(BLX_SOB)) {
    			if(sharedPreferences.getBoolean(key,false)){
				editor.putInt(PREF_BLX, Integer.parseInt(Helpers.readOneLine(BLX_PATH))).apply();
    			}
    			else{
    				editor.remove(PREF_BLX).apply();
    			}
		}
    	else if (key.equals(BLTIMEOUT_SOB)) {
    			if(sharedPreferences.getBoolean(key,false)){
				editor.putInt(PREF_BLTIMEOUT, Integer.parseInt(Helpers.readOneLine(BL_TIMEOUT_PATH))).apply();
    			}
    			else{
    				editor.remove(PREF_BLTIMEOUT).apply();
    			}
		}
    	else if (key.equals(PFK_SOB)) {
    			if(sharedPreferences.getBoolean(key,false)){
				if(Helpers.readOneLine(PFK_HOME_ENABLED).equals("1")){
					editor.putBoolean(PFK_HOME_ON, true);
				}
				else{
					editor.putBoolean(PFK_HOME_ON, false);
				}
				editor.putInt(PREF_HOME_ALLOWED_IRQ, Integer.parseInt(Helpers.readOneLine(PFK_HOME_ALLOWED_IRQ)))
				.putInt(PREF_HOME_REPORT_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_HOME_REPORT_WAIT)));
				if(Helpers.readOneLine(PFK_MENUBACK_ENABLED).equals("1")){
					editor.putBoolean(PFK_MENUBACK_ON,true);
				}
				else{
					editor.putBoolean(PFK_MENUBACK_ON,false);
				}
				editor.putInt(PREF_MENUBACK_INTERRUPT_CHECKS, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_INTERRUPT_CHECKS)))
				.putInt(PREF_MENUBACK_FIRST_ERR_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_FIRST_ERR_WAIT)))
				.putInt(PREF_MENUBACK_LAST_ERR_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_LAST_ERR_WAIT)))
				.apply();
    			}
    			else{
				editor.remove(PFK_HOME_ON)
				.remove(PREF_HOME_ALLOWED_IRQ)
				.remove(PREF_HOME_REPORT_WAIT)
				.remove(PFK_MENUBACK_ON)
				.remove(PREF_MENUBACK_INTERRUPT_CHECKS)
				.remove(PREF_MENUBACK_FIRST_ERR_WAIT)
				.remove(PREF_MENUBACK_LAST_ERR_WAIT)
				.apply();
    			}
		}
    	else if (key.equals(DYNAMIC_DIRTY_WRITEBACK_SOB)) {
    			if(sharedPreferences.getBoolean(key,false)){
				if(Helpers.readOneLine(DYNAMIC_DIRTY_WRITEBACK_PATH).equals("1")){
					editor.putBoolean(PREF_DYNAMIC_DIRTY_WRITEBACK,true);
				}
				else{
					editor.putBoolean(PREF_DYNAMIC_DIRTY_WRITEBACK,false);
				}    				
				editor.putInt(PREF_DIRTY_WRITEBACK_ACTIVE, Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_ACTIVE_PATH)))
				.putInt(PREF_DIRTY_WRITEBACK_SUSPEND, Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_SUSPEND_PATH)))
				.apply();
    			}
    			else{
				editor.remove(PREF_DYNAMIC_DIRTY_WRITEBACK)
				.remove(PREF_DIRTY_WRITEBACK_ACTIVE)
				.remove(PREF_DIRTY_WRITEBACK_SUSPEND)
				.apply();
    			}
		}
    }

    public void openDialog(int currentProgress, String title, final int min, final int max, final Preference pref, final String path, final String key) {
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
        
        settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
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

        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				mSeekbarProgress = seekbar.getProgress();
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
                    String v;
                    if (key.equals("pref_viber")) {
                        v=vib.get_val(path);
                        Vibrator vb = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(1000);
                    }
                    else{
                        v=Helpers.readOneLine(path);
                    }
                    final SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putInt(key, Integer.parseInt(v));
                    editor.commit();
                    pref.setSummary(v);

                }
            }).create().show();
    }
    private boolean no_touchscreen(){
        return (!new File(SLIDE2WAKE).exists() && !new File(SWIPE2WAKE).exists() && !new File(HOME2WAKE).exists() && !new File(LOGO2WAKE).exists() && !new File(LOGO2MENU).exists() && !new File(POCKET_DETECT).exists() && !new File(PICK2WAKE).exists() && !new File(FLICK2SLEEP).exists() && Helpers.touch2wake_path() == null);
    }
}

