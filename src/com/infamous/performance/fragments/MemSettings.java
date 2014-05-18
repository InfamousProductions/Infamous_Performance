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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.*;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.activities.KSMActivity;
import com.infamous.performance.activities.MemUsageActivity;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.activities.PackActivity;
import com.infamous.performance.activities.ZramActivity;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemSettings extends PreferenceFragment implements OnSharedPreferenceChangeListener,Constants {
    SharedPreferences mPreferences;
	
	private int mSeekbarProgress;
	private EditText settingText;

	private Preference mForegroundApp;
	private Preference mVisibleApp;
	private Preference mSecondaryServer;
	private Preference mHiddenApp;
	private Preference mContentProviders;
	private Preference mEmptyApp;

    private ListPreference mPresets;
    final private CharSequence[] ventries ={"512,1024,1280,2048,3072,4096","1024,2048,2560,4096,6144,8192","1024,2048,4096,8192,12288,16384","2048,4096,8192,16384,24576,32768","4096,8192,16384,32768,49152,65536"};
	private String values[];

    private CheckBoxPreference mUserON;
    private CheckBoxPreference mSysON;
    private Preference mUserNames;
    private Preference mSysNames;
    private CheckBoxPreference mKSM;
    private Preference mKSMsettings;
    private Preference mZRAMsettings;

    private Boolean ispm;
    private int ksm=0;
    private String ksmpath=KSM_RUN_PATH;
    private float maxdisk = Helpers.getMem("MemTotal") / 1024;
    private int swap = Math.round(Helpers.getSwap() / 1024);
    private int curdisk=0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.mem_settings);
	
        values = Helpers.readOneLine(MINFREE_PATH).split(",");

        mForegroundApp= findPreference(OOM_FOREGROUND_APP);
        mVisibleApp= findPreference(OOM_VISIBLE_APP);
        mSecondaryServer= findPreference(OOM_SECONDARY_SERVER);
        mHiddenApp= findPreference(OOM_HIDDEN_APP);
        mContentProviders= findPreference(OOM_CONTENT_PROVIDERS);
        mEmptyApp= findPreference(OOM_EMPTY_APP);

        mPresets= (ListPreference) findPreference("oom_presets");

        Map<String,String> oom=new LinkedHashMap<String, String>();
        final String s=mPreferences.getString(MINFREE_DEFAULT,"");
        if(!s.equals("")){
            oom.put(getResources().getString(R.string.oom_default),s);
        }

        for(int i=0;i<getResources().getStringArray(R.array.oom_values).length;i++){
            oom.put(getResources().getStringArray(R.array.oom_values)[i],ventries[i].toString());
        }
        mPresets.setEntryValues(oom.values().toArray(new CharSequence[oom.size()]));
        mPresets.setEntries(oom.keySet().toArray(new CharSequence[oom.size()]));
        mPresets.setValue(Helpers.readOneLine(MINFREE_PATH));

        updateOOM(values);

        mUserON=(CheckBoxPreference) findPreference(PREF_USER_PROC);
        mSysON=(CheckBoxPreference) findPreference(PREF_SYS_PROC);
        mUserNames= findPreference(PREF_USER_NAMES);
        mSysNames= findPreference(PREF_SYS_NAMES);

        mKSM=(CheckBoxPreference) findPreference(PREF_RUN_KSM);
        mKSMsettings= findPreference("ksm_settings");

        mZRAMsettings= findPreference("zram_settings");
        CheckBoxPreference mZRAMboot=(CheckBoxPreference) findPreference("zram_boot");

        String names="";
        if (!new File(USER_PROC_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("notkill_user_proc");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mUserON.setChecked(Helpers.readOneLine(USER_PROC_PATH).equals("1"));
            names=Helpers.readOneLine(USER_PROC_NAMES_PATH);
            if(names==null) names="";
            mPreferences.edit().putString(PREF_USER_NAMES, names).apply();
        }
        if (!new File(SYS_PROC_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("notkill_sys_proc");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mSysON.setChecked(Helpers.readOneLine(SYS_PROC_PATH).equals("1"));
            names=Helpers.readOneLine(USER_SYS_NAMES_PATH);
            if(names==null) names="";
            mPreferences.edit().putString(PREF_SYS_NAMES, names).apply();
        }
        if (!new File(UKSM_RUN_PATH+"/run").exists() && !new File(KSM_RUN_PATH+"/run").exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("ksm");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            if(new File(UKSM_RUN_PATH+"/run").exists()){
                ksm=1;
                ksmpath=UKSM_RUN_PATH;
                mKSMsettings.setSummary(R.string.uksm);
            }
            else{
                ksm=0;
                ksmpath=KSM_RUN_PATH;
                mKSMsettings.setSummary(null);
            }
            mKSM.setChecked(Helpers.readOneLine(ksmpath+"/run").equals("1"));
            //mKSMsettings.setSummary(getString(R.string.ksm_pagtoscan)+" "+Helpers.readOneLine(KSM_PAGESTOSCAN_PATH[ksm])+" | "+getString(R.string.ksm_sleep)+" "+Helpers.readOneLine(KSM_SLEEP_PATH[ksm]));

        }
        ispm=(!Helpers.binExist("pm").equals(NOT_FOUND));

        if(!Helpers.isZRAM()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("zram");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            int percent=0;
            if(swap>0){
                percent=Math.round(swap * 100 / maxdisk);
                curdisk=Math.round(maxdisk*percent/100);
                if(mZRAMboot.isChecked()) mPreferences.edit().putInt(PREF_ZRAM,curdisk).apply();
            }
            else{
                curdisk=mPreferences.getInt(PREF_ZRAM, Math.round(maxdisk*18/100));
                percent=Math.round(curdisk * 100 / maxdisk);
            }
            mZRAMsettings.setSummary(getString(R.string.ps_zram)+" | "+getString(R.string.zram_disk_size,Helpers.ReadableByteCount(curdisk*1024*1024))+" ("+String.valueOf(percent)+"%)");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mem_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab),(ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(getActivity(), PCSettings.class);
                startActivity(intent);
                break;
            case R.id.mem_usage:
                intent = new Intent(getActivity(), MemUsageActivity.class);
                intent.putExtra("tip","mem");
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
		if (preference.equals(mForegroundApp)) {
			String title = getString(R.string.title_foreground_app)+" (mb)";
			int currentProgress = oomConv(values[0]);
			openDialog(0,currentProgress, title, 0,oomConv(values[1]), preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
		else if (preference.equals(mVisibleApp)) {
			String title = getString(R.string.title_visible_app)+" (mb)";
			int currentProgress = oomConv(values[1]);
			openDialog(1,currentProgress, title, oomConv(values[0]),oomConv(values[2]), preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
		else if (preference.equals(mSecondaryServer)) {
			String title = getString(R.string.title_secondary_server)+" (mb)";
			int currentProgress = oomConv(values[2]);
			openDialog(2,currentProgress, title, oomConv(values[1]),oomConv(values[3]), preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
		else if (preference.equals(mHiddenApp)) {
			String title = getString(R.string.title_hidden_app)+" (mb)";
			int currentProgress = oomConv(values[3]);
			openDialog(3,currentProgress, title, oomConv(values[2]),oomConv(values[4]), preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
		else if (preference.equals(mContentProviders)) {
			String title = getString(R.string.title_content_providers)+" (mb)";
			int currentProgress = oomConv(values[4]);
			openDialog(4,currentProgress, title, oomConv(values[3]),oomConv(values[5]), preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
		else if (preference.equals(mEmptyApp)) {
			String title = getString(R.string.title_empty_app)+" (mb)";
			int currentProgress = oomConv(values[5]);
			openDialog(5,currentProgress, title, oomConv(values[4]),256, preference, MINFREE_PATH, PREF_MINFREE);
			return true;
		}
        else if (preference.equals(mUserON)){
            if (Integer.parseInt(Helpers.readOneLine(USER_PROC_PATH))==0){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + USER_PROC_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + USER_PROC_PATH);
            }
            return true;
        }
        else if (preference.equals(mSysON)){
            if (Integer.parseInt(Helpers.readOneLine(SYS_PROC_PATH))==0){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + SYS_PROC_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + SYS_PROC_PATH);
            }
            return true;
        }
        else if (preference.equals(mUserNames)){
            if(ispm){
                Intent getpacks = new Intent(getActivity(), PackActivity.class);
                getpacks.putExtra("mod",false);
                startActivity(getpacks);
            }
            else{
                ProcEditDialog(key, getString(R.string.pt_user_names_proc), "", USER_PROC_NAMES_PATH, false);
            }
        }
        else if (preference.equals(mSysNames)){
            if(ispm){
                Intent getpacks = new Intent(getActivity(), PackActivity.class);
                getpacks.putExtra("mod",true);
                startActivity(getpacks);
            }
            else{
                ProcEditDialog(key,getString(R.string.pt_sys_names_proc),"",USER_SYS_NAMES_PATH,true);
            }
        }
        else if (preference.equals(mKSM)){
            if ((Integer.parseInt(Helpers.readOneLine(ksmpath+"/run"))==0)||(Integer.parseInt(Helpers.readOneLine(ksmpath+"/run"))==2)){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ksmpath+"/run");
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ksmpath+"/run");
            }
            return true;
        }
        else if (preference.equals(mKSMsettings)){
            startActivityForResult(new Intent(getActivity(), KSMActivity.class), 1);
        }
        else if (preference.equals(mZRAMsettings)){
            Intent intent = new Intent(getActivity(), ZramActivity.class);
            intent.putExtra("curdisk", curdisk);
            startActivityForResult(intent,1);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
    	if (key.equals(PREF_MINFREE_BOOT)) {
    		if(sharedPreferences.getBoolean(key,false)){
			    sharedPreferences.edit().putString(PREF_MINFREE, Helpers.readOneLine(MINFREE_PATH)).apply();
    		}
    		else{
    			sharedPreferences.edit().remove(PREF_MINFREE).apply();
    		}
	    }
        else if(key.equals(ZRAM_SOB)){
            if(sharedPreferences.getBoolean(key,false)){
                sharedPreferences.edit().putInt(PREF_ZRAM,curdisk).apply();
            }
            else{
                sharedPreferences.edit().remove(PREF_ZRAM).apply();
            }
        }
        else if(key.equals("oom_presets")){
            new CMDProcessor().su.runWaitFor("busybox echo " + mPresets.getValue() + " > " + MINFREE_PATH);
            mPreferences.edit().putString(PREF_MINFREE, mPresets.getValue()).apply();
            values = Helpers.readOneLine(MINFREE_PATH).split(",");
            updateOOM(values);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getActivity();
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                final int r=data.getIntExtra("result",0);
                Log.d(TAG, "input = "+r);
                switch(r){
                    case 1:
                    //    mKSMsettings.setSummary(getString(R.string.ksm_pagtoscan)+" "+Helpers.readOneLine(KSM_PAGESTOSCAN_PATH[ksm])+" | "+getString(R.string.ksm_sleep)+" "+Helpers.readOneLine(KSM_SLEEP_PATH[ksm]));
                        break;
                    case 2:
                        curdisk=mPreferences.getInt(PREF_ZRAM, Math.round(maxdisk*18/100));
                        final int percent=Math.round(curdisk * 100 / maxdisk);
                        mZRAMsettings.setSummary(getString(R.string.ps_zram)+" | "+getString(R.string.zram_disk_size,Helpers.ReadableByteCount(curdisk*1024*1024))+" ("+String.valueOf(percent)+"%)");
                        break;
                }
            }
            //if (resultCode == Activity.RESULT_CANCELED) {}
        }
    }
	private void updateOOM(String[] v) {
		mForegroundApp.setSummary(oomConv(values[0])+"mb "+"[ "+v[0]+" ]");
		mVisibleApp.setSummary(oomConv(values[1])+"mb "+"[ "+v[1]+" ]");
		mSecondaryServer.setSummary(oomConv(values[2])+"mb "+"[ "+v[2]+" ]");
		mHiddenApp.setSummary(oomConv(values[3])+"mb "+"[ "+v[3]+" ]");
		mContentProviders.setSummary(oomConv(values[4])+"mb "+"[ "+v[4]+" ]");
		mEmptyApp.setSummary(oomConv(values[5])+"mb "+"[ "+v[5]+" ]");	
	}
	
    private int oomConv(String s) {
        final int mb = Integer.parseInt(s.trim()) * 4 / 1024;
        return (int) Math.ceil(mb);
    }
	
	private static String implodeArray(String[] inputArray, String glueString) {
		String output = "";
		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);
			for (int i=1; i<inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}
			output = sb.toString();
		}
		return output;
	}

    public void ProcEditDialog(final String key,String title,String msg,String path,Boolean type) {
        Resources res = getActivity().getResources();
        final String cancel = res.getString(R.string.cancel);
        final String ok = res.getString(R.string.ps_volt_save);

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.sh_dialog, null);
        final String namespath = path;


        settingText = (EditText) alphaDialog.findViewById(R.id.shText);
        settingText.setText(mPreferences.getString(key,""));
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
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
            }
        });
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setView(alphaDialog)
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        /* nothing */
                    }
                })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(key, settingText.getText().toString()).commit();
                        new CMDProcessor().su.runWaitFor("busybox echo "+mPreferences.getString(key, Helpers.readOneLine(namespath))+" > " + namespath);

                    }
                })
                .create()
                .show();
    }


    public void openDialog(final int idx,int currentProgress, String title, final int min, final int max,
                           final Preference pref, final String path, final String key) {
        Resources res = getActivity().getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);

		seekbar.setMax(max);
		seekbar.setProgress(currentProgress);
        
        settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				int val = Integer.parseInt(settingText.getText().toString());
				seekbar.setProgress(val);
				return true;
			}
			return false;
		}
		});
		settingText.setText(Integer.toString(currentProgress));
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
						seekbar.setProgress(val);
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
					settingText.setText(Integer.toString(mSeekbarProgress));
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

        new AlertDialog.Builder(getActivity())
			.setTitle(title)
			.setView(alphaDialog)
			.setNegativeButton(cancel,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int which) {
				// nothing
				}
			})
			.setPositiveButton(ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int val = Integer.parseInt(settingText.getText().toString());
					if(val<min){val=min;}
					seekbar.setProgress(val);
					int newProgress = seekbar.getProgress();
					values[idx]=Integer.toString(newProgress*256);
					pref.setSummary(newProgress+" MB "+"("+values[idx]+")");
					new CMDProcessor().su.runWaitFor("busybox echo " + implodeArray(values,",") + " > " + path);
					final SharedPreferences.Editor editor = mPreferences.edit();
					editor.putString(key, implodeArray(values,","));
					editor.commit();
				}
			}).create().show();
			
    }
}
