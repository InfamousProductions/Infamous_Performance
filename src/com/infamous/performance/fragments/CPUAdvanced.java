package com.infamous.performance.fragments;

/**
 * Created by h0rn3t on 02.01.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.infamous.performance.R;
import com.infamous.performance.activities.ParamActivity;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.GPUClass;
import com.infamous.performance.util.Helpers;

import java.io.File;

public class CPUAdvanced extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,Constants {

    SharedPreferences mPreferences;
    private CheckBoxPreference mMpdecision,mIntelliplug,mEcomode,mGenHP;
    private Preference mHotplugset,mGpuGovset;
    private ListPreference mSOmax,mSOmin,lmcps,lcpuq,lgpufmax;
    private String pso="";
    private Context context;
    private String hotpath=Helpers.hotplug_path();
    final private CharSequence[] vmcps={"0","1","2"};
    private GPUClass gpu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        setHasOptionsMenu(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.cpu_advanced);

        pso=getString(R.string.ps_so_minmax);

        mSOmax = (ListPreference) findPreference("pref_so_max");
        mSOmin = (ListPreference) findPreference("pref_so_min");

        mMpdecision = (CheckBoxPreference) findPreference("pref_mpdecision");
        mIntelliplug = (CheckBoxPreference) findPreference("pref_intelliplug");
        mEcomode = (CheckBoxPreference) findPreference("pref_ecomode");
        //mMcPS = (CheckBoxPreference) findPreference("pref_mc_ps");
        lmcps = (ListPreference) findPreference("pref_mcps");
        lcpuq = (ListPreference) findPreference("pref_cpuquiet");
        mHotplugset = findPreference("pref_hotplug");
        mGenHP = (CheckBoxPreference) findPreference("pref_hp");
        lgpufmax = (ListPreference) findPreference("pref_gpu_fmax");
        mGpuGovset = findPreference("pref_gpugov_set");

        if (!new File(SO_MAX_FREQ).exists() || !new File(SO_MIN_FREQ).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("so_min_max");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            final String availableFrequencies = Helpers.readOneLine(STEPS_PATH);
            if (availableFrequencies != null) {
                CharSequence[] entries = availableFrequencies.split(" ");
                mSOmax.setEntries(entries);
                mSOmax.setEntryValues(entries);
                mSOmin.setEntries(entries);
                mSOmin.setEntryValues(entries);
                final String readsomax=Helpers.readOneLine(SO_MAX_FREQ);
                final String readsomin=Helpers.readOneLine(SO_MIN_FREQ);
                mSOmax.setValue(readsomax);
                mSOmin.setValue(readsomin);
                mSOmax.setSummary(pso+ readsomax+" kHz");
                mSOmin.setSummary(pso+ readsomin+" kHz");
            }
        }

        if (Helpers.binExist("mpdecision").equals(NOT_FOUND)){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mpdecision");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            Boolean mpdon = Helpers.moduleActive("mpdecision");
            mMpdecision.setChecked(mpdon);
            mPreferences.edit().putBoolean("mpdecision",mpdon).apply();
        }
        if (!new File(INTELLI_PLUG).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("intelliplug");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mIntelliplug.setChecked(Helpers.readOneLine(INTELLI_PLUG).equals("1"));
        }
        if(hotpath==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hotplugset");
            getPreferenceScreen().removePreference(hideCat);
        }
        if (!new File(ECO_MODE).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("ecomode");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mEcomode.setChecked(Helpers.readOneLine(ECO_MODE).equals("1"));
        }
        if (!new File(MC_PS).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mc_ps");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            lmcps.setEntries(getResources().getStringArray(R.array.mc_ps_array));
            lmcps.setEntryValues(vmcps);
            lmcps.setValue(Helpers.readOneLine(MC_PS));
            lmcps.setSummary(getString(R.string.ps_mc_ps,lmcps.getEntry().toString()));
        }
        if(!new File(GEN_HP).exists()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hp");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mGenHP.setChecked(Helpers.readOneLine(GEN_HP).equals("1"));
            mGenHP.setSummary(getString(R.string.ps_hp)+" | "+getString(R.string.ps_dsync));
        }
        if(!new File(CPU_QUIET_GOV).exists()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("cpuq");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            final String cur_cpuq_gov=Helpers.readOneLine(CPU_QUIET_GOV);
            final String s=Helpers.readOneLine(CPU_QUIET_CUR);
            final CharSequence[] govs=cur_cpuq_gov.split(" ");
            lcpuq.setEntries(govs);
            lcpuq.setEntryValues(govs);
            lcpuq.setValue(s);
            lcpuq.setSummary(getString(R.string.ps_cpuquiet,s));
        }
        gpu = new GPUClass();
        if(gpu.gpuclk_path()==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpu_max_clk");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            lgpufmax.setEntries(gpu.gpuclk_names());
            lgpufmax.setEntryValues(gpu.gpuclk_values());
            final String s=Helpers.readOneLine(gpu.gpuclk_path());
            lgpufmax.setValue(s);
            lgpufmax.setSummary(getString(R.string.ps_gpu_fmax, Helpers.toMHz(String.valueOf(Integer.parseInt(s.toString()) / 1000))));
        }
        if(gpu.gpugovset_path()==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpugovset");
            getPreferenceScreen().removePreference(hideCat);
        }
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
                Intent intent = new Intent(getActivity(), PCSettings.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference==mMpdecision) {
            if(mMpdecision.isChecked()){
                final StringBuilder sb = new StringBuilder();
                sb.append("mpdecisionstart;\n");
                Helpers.shExec(sb,context,true);
            }
            else{
                new CMDProcessor().su.runWaitFor("stop mpdecision");
            }
            return true;
        }
        else if(preference==mHotplugset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path",hotpath);
            i.putExtra("sob",HOTPLUG_SOB);
            i.putExtra("pref","hotplug");
            startActivity(i);
        }
        else if(preference==mIntelliplug) {
            if (Helpers.readOneLine(INTELLI_PLUG).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + INTELLI_PLUG);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + INTELLI_PLUG);
            }
            return true;
        }
        else if(preference==mEcomode) {
            if (Helpers.readOneLine(ECO_MODE).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        }
        else if(preference==mGenHP) {
            if (Helpers.readOneLine(GEN_HP).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        }
        else if(preference==mGpuGovset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path",gpu.gpugovset_path());
            i.putExtra("sob",GPU_PARAM_SOB);
            i.putExtra("pref","gpuparam");
            startActivity(i);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_so_max")) {
            final String values = mSOmax.getValue();
            if (!values.equals(Helpers.readOneLine(SO_MAX_FREQ))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + SO_MAX_FREQ);
            }
            mSOmax.setSummary(pso + values + " kHz");
        }
        else if (key.equals("pref_so_min")) {
            final String values = mSOmin.getValue();
            if (!values.equals(Helpers.readOneLine(SO_MIN_FREQ))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + SO_MIN_FREQ);
            }
            mSOmin.setSummary(pso + values + " kHz");
        }
        else if(key.equals("pref_mcps")){
            final String values = lmcps.getValue();
            if (!values.equals(Helpers.readOneLine(MC_PS))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + MC_PS);
            }
            lmcps.setSummary(getString(R.string.ps_mc_ps,lmcps.getEntry()));
        }
        else if(key.equals("pref_cpuquiet")){
            final String values = lcpuq.getValue();
            if (!values.equals(Helpers.readOneLine(CPU_QUIET_CUR))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + CPU_QUIET_CUR);
            }
            lcpuq.setSummary(getString(R.string.ps_cpuquiet,values));
        }
        else if(key.equals("pref_gpu_fmax")){
            final String values = lgpufmax.getValue();
            if (!values.equals(Helpers.readOneLine(gpu.gpuclk_path()))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + gpu.gpuclk_path());
            }
            lgpufmax.setSummary(getString(R.string.ps_gpu_fmax,Helpers.toMHz(String.valueOf(Integer.parseInt(values) / 1000))));
        }
    }


}

