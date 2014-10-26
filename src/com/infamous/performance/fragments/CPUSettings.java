package com.infamous.performance.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.infamous.performance.R;
import com.infamous.performance.activities.GovSetActivity;
import com.infamous.performance.activities.IOSetActivity;
import com.infamous.performance.activities.MainActivity;
import com.infamous.performance.activities.MemUsageActivity;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.io.File;
import java.util.Arrays;

public class CPUSettings extends Fragment implements SeekBar.OnSeekBarChangeListener, Constants {

    private LinearLayout lcurcpu;
    private SeekBar mMaxSlider;
    private SeekBar mMinSlider;
    private Spinner mGovernor;
    private Spinner mIo;
    private TextView mCurFreq;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private CurCPUThread mCurCPUThread;
    SharedPreferences mPreferences;
    private boolean mIsTegra3 = false;
    private boolean mIsDynFreq = false;
    private Context context;
    private final String supported[]={"ondemand","ondemandplus","lulzactive","lulzactiveW","interactive","hyper","conservative","lionheart","adaptive","intellidemand"};
    private int nCpus=Helpers.getNumOfCpus();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(savedInstanceState!=null) {
            MainActivity.curcpu=savedInstanceState.getInt("curcpu");
            MainActivity.mMaxFreqSetting.set(MainActivity.curcpu,savedInstanceState.getString("maxfreq"));
            MainActivity.mMinFreqSetting.set(MainActivity.curcpu,savedInstanceState.getString("minfreq"));
            MainActivity.mCurGovernor.set(MainActivity.curcpu,savedInstanceState.getString("governor"));
            MainActivity.mCurIO.set(MainActivity.curcpu,savedInstanceState.getString("io"));
            MainActivity.mCPUon.set(MainActivity.curcpu,savedInstanceState.getString("cpuon"));
        }
        else{
            if(MainActivity.mMinFreqSetting.isEmpty() || MainActivity.mMaxFreqSetting.isEmpty()) MainActivity.getCPUval();
        }

        setHasOptionsMenu(true);
    }
    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putInt("curcpu",MainActivity.curcpu);
        saveState.putString("maxfreq",MainActivity.mMaxFreqSetting.get(MainActivity.curcpu));
        saveState.putString("minfreq",MainActivity.mMinFreqSetting.get(MainActivity.curcpu));
        saveState.putString("governor",MainActivity.mCurGovernor.get(MainActivity.curcpu));
        saveState.putString("io",MainActivity.mCurIO.get(MainActivity.curcpu));
        saveState.putString("cpuon",MainActivity.mCPUon.get(MainActivity.curcpu));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cpu_settings, root, false);

        mIsTegra3 = new File(TEGRA_MAX_FREQ_PATH).exists();
        mIsDynFreq = new File(DYN_MAX_FREQ_PATH).exists() && new File(DYN_MIN_FREQ_PATH).exists();

        lcurcpu=(LinearLayout) view.findViewById(R.id.lcurcpu);

        mCurFreq = (TextView) view.findViewById(R.id.current_speed);
        mCurFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nCpus==1) return;
                if(++MainActivity.curcpu>(nCpus-1)) MainActivity.curcpu=0;
                setCPUval(MainActivity.curcpu);
            }
        });

        mCurFreq.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                if(new File(CPU_ON_PATH.replace("cpu0","cpu"+MainActivity.curcpu)).exists() && MainActivity.curcpu>0){
                    final StringBuilder sb = new StringBuilder();
                    if(MainActivity.mCPUon.get(MainActivity.curcpu).equals("1")){
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"0\";\n");
                        MainActivity.mCPUon.set(MainActivity.curcpu,"0");
                    }
                    else{
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"1\";\n");
                        MainActivity.mCPUon.set(MainActivity.curcpu,"1");
                    }
                    Helpers.shExec(sb,context,true);

                    setCPUval(MainActivity.curcpu);
                }

                return true;
            }
        });

        final int mFrequenciesNum = MainActivity.mAvailableFrequencies.length - 1;

        mMaxSlider = (SeekBar) view.findViewById(R.id.max_slider);
        mMaxSlider.setMax(mFrequenciesNum);
        mMaxSlider.setOnSeekBarChangeListener(this);
        mMaxSpeedText = (TextView) view.findViewById(R.id.max_speed_text);

        mMinSlider = (SeekBar) view.findViewById(R.id.min_slider);
        mMinSlider.setMax(mFrequenciesNum);
        mMinSlider.setOnSeekBarChangeListener(this);
        mMinSpeedText = (TextView) view.findViewById(R.id.min_speed_text);


        mGovernor = (Spinner) view.findViewById(R.id.pref_governor);
        String[] mAvailableGovernors = Helpers.readOneLine(GOVERNORS_LIST_PATH).split(" ");
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String mAvailableGovernor : mAvailableGovernors) {
            governorAdapter.add(mAvailableGovernor.trim());
        }
        mGovernor.setAdapter(governorAdapter);
        mGovernor.setSelection(Arrays.asList(mAvailableGovernors).indexOf(MainActivity.mCurGovernor.get(MainActivity.curcpu)));
        mGovernor.post(new Runnable() {
            public void run() {
                mGovernor.setOnItemSelectedListener(new GovListener());
            }
        });



        mIo = (Spinner) view.findViewById(R.id.pref_io);
        String[] mAvailableIo = Helpers.getAvailableIOSchedulers(IO_SCHEDULER_PATH);

        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String aMAvailableIo : mAvailableIo) {
            ioAdapter.add(aMAvailableIo);
        }
        mIo.setAdapter(ioAdapter);
        mIo.setSelection(Arrays.asList(mAvailableIo).indexOf(MainActivity.mCurIO.get(MainActivity.curcpu)));
        mIo.post(new Runnable() {
            public void run() {
                mIo.setOnItemSelectedListener(new IOListener());
            }
        });

        Switch mSetOnBoot = (Switch) view.findViewById(R.id.cpu_sob);
        mSetOnBoot.setChecked(mPreferences.getBoolean(CPU_SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(CPU_SOB, checked);
                if (checked) {
                        for (int i = 0; i < nCpus; i++){
                            editor.putString(PREF_MIN_CPU+i, MainActivity.mMinFreqSetting.get(i));
                            editor.putString(PREF_MAX_CPU+i, MainActivity.mMaxFreqSetting.get(i));
                            editor.putString(PREF_GOV, MainActivity.mCurGovernor.get(i));
                            editor.putString(PREF_IO, MainActivity.mCurIO.get(i));
                            editor.putString("cpuon" + i, MainActivity.mCPUon.get(i));
                        }
                }
                editor.apply();
            }
        });

        //if(nCpus>1){
            LinearLayout vcpus[] = new LinearLayout[nCpus];
            for (int i = 0; i < nCpus; i++) {
                vcpus[i]= (LinearLayout)inflater.inflate(R.layout.cpu_view, root, false);
                vcpus[i].setId(i);
                TextView nc=(TextView) vcpus[i].findViewById(R.id.ncpu);
                nc.setText(Integer.toString(i+1));
                if(i!=MainActivity.curcpu) nc.setText(" ");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, (float)0.1);
                lcurcpu.addView(vcpus[i],params);
            }
        //}

        setCPUval(MainActivity.curcpu);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cpu_settings_menu, menu);
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
            case R.id.gov_settings:
                for (String aSupported : supported) {
                    if (aSupported.equals(MainActivity.mCurGovernor.get(MainActivity.curcpu))) {
                        if(new File(GOV_SETTINGS_PATH + MainActivity.mCurGovernor.get(MainActivity.curcpu)).exists()){
                            intent = new Intent(context, GovSetActivity.class);
                            intent.putExtra("curgov", MainActivity.mCurGovernor.get(MainActivity.curcpu));
                            startActivity(intent);
                        }
                        break;
                    }
                }
                break;
            case R.id.io_settings:
                if(new File(IO_TUNABLE_PATH).exists()){
                    intent = new Intent(context, IOSetActivity.class);
                    intent.putExtra("curio", MainActivity.mCurIO.get(MainActivity.curcpu));
                    startActivity(intent);
                }
                break;
            case R.id.cpu_info:
                intent = new Intent(getActivity(), MemUsageActivity.class);
                intent.putExtra("tip","cpu");
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()){
                case R.id.max_slider:
                    setMaxSpeed(progress);
                    break;
                case R.id.min_slider:
                    setMinSpeed(progress);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final StringBuilder sb = new StringBuilder();
        int oc=-1000;
        if(new File(OC_VALUE_PATH).exists()) {
            oc=Integer.parseInt(Helpers.readOneLine(OC_VALUE_PATH));
            sb.append("busybox echo 100 > ").append(OC_VALUE_PATH).append(";\n");
        }
        switch (seekBar.getId()){
            case R.id.max_slider:
                updateSharedPrefs(PREF_MAX_CPU+MainActivity.curcpu, MainActivity.mMaxFreqSetting.get(MainActivity.curcpu));
                sb.append("set_val \"").append(MAX_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"").append(MainActivity.mMaxFreqSetting.get(MainActivity.curcpu)).append("\";\n");
                if (mIsTegra3) {
                    sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting.get(MainActivity.curcpu)).append(" > ").append(TEGRA_MAX_FREQ_PATH).append(";\n");
                }
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting.get(MainActivity.curcpu)).append(" > ").append(DYN_MAX_FREQ_PATH).append(";\n");
                }
                if(new File(HARD_LIMIT_PATH).exists()){
                    sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting.get(MainActivity.curcpu)).append(" > ").append(HARD_LIMIT_PATH).append(";\n");
                }
                break;
            case R.id.min_slider:
                updateSharedPrefs(PREF_MIN_CPU+MainActivity.curcpu, MainActivity.mMinFreqSetting.get(MainActivity.curcpu));
                sb.append("set_val \"").append(MIN_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"").append(MainActivity.mMinFreqSetting.get(MainActivity.curcpu)).append("\";\n");
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMinFreqSetting.get(MainActivity.curcpu)).append(" > ").append(DYN_MIN_FREQ_PATH).append(";\n");
                }
                break;
        }
        if(new File(OC_VALUE_PATH).exists() && oc!=100 && oc>-1000) {
            sb.append("busybox echo ").append(Integer.toString(oc)).append(" > ").append(OC_VALUE_PATH).append(";\n");
        }
        Helpers.shExec(sb,context,true);
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            if(MainActivity.mCurGovernor.get(MainActivity.curcpu).equals(selected)) return;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nCpus; i++){
                sb.append("set_val \"").append(GOVERNOR_PATH.replace("cpu0", "cpu" + i)).append("\" \"").append(selected).append("\";\n");
            }
            //restore gov tunable
            final String s=mPreferences.getString(selected.replace(" ","_"),"");
            if(!s.equals("")){
                sb.append("if busybox [ -d ").append(GOV_SETTINGS_PATH).append(selected).append(" ]; then\n");
                String p[]=s.split(";");
                for (String aP : p) {
                    if(aP!=null && aP.contains(":")){
                        final String pn[]=aP.split(":");
                        sb.append("busybox echo ").append(pn[1]).append(" > ").append(GOV_SETTINGS_PATH).append(selected).append("/").append(pn[0]).append(";\n");
                    }
                }
                sb.append("fi;\n");
            }
            Helpers.shExec(sb,context,true);
            MainActivity.mCurGovernor.set(MainActivity.curcpu,selected);
            updateSharedPrefs(PREF_GOV, selected);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            if(MainActivity.mCurIO.get(MainActivity.curcpu).equals(selected)) return;
			final StringBuilder sb = new StringBuilder();
			for(byte i=0; i<2; i++){
                if (new File(IO_SCHEDULER_PATH.replace("mmcblk0","mmcblk"+i)).exists())
				    sb.append("busybox echo ").append(selected).append(" > ").append(IO_SCHEDULER_PATH.replace("mmcblk0","mmcblk"+i)).append(";\n");
			}
            //restore io tunable
            final String s=mPreferences.getString(selected.replace(" ","_"),"");
            if(!s.equals("")){
                String p[]=s.split(";");
                for(byte i=0;i<2; i++){
                    if (new File(IO_TUNABLE_PATH.replace("mmcblk0","mmcblk"+i)).exists()){
                        for (String aP : p) {
                            if(aP!=null && aP.contains(":")){
                                final String pn[]=aP.split(":");
                                sb.append("busybox echo ").append(pn[1]).append(" > ").append(IO_TUNABLE_PATH.replace("mmcblk0","mmcblk"+i)).append("/").append(pn[0]).append(";\n");
                            }
                        }
                    }
                }
            }
			Helpers.shExec(sb,context,true);
            MainActivity.mCurIO.set(MainActivity.curcpu,selected);
            updateSharedPrefs(PREF_IO, selected);
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurCPUThread == null) {
            mCurCPUThread = new CurCPUThread();
            mCurCPUThread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCurCPUThread != null) {
            if (mCurCPUThread.isAlive()) {
                mCurCPUThread.interrupt();
                try {
                    mCurCPUThread.join();
                }
                catch (InterruptedException e) {
                    Log.d(TAG, "CPU thread error " + e);
                }
            }
        }
        super.onDestroy();
    }

    public void setMaxSpeed(int progress) {
        final String current = MainActivity.mAvailableFrequencies[progress];
        int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMinFreqSetting.set(MainActivity.curcpu,current);
        }
        mMaxSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMaxFreqSetting.set(MainActivity.curcpu,current);
    }

    public void setMinSpeed(int progress) {
        final String current = MainActivity.mAvailableFrequencies[progress];
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMaxFreqSetting.set(MainActivity.curcpu,current);
        }
        mMinSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMinFreqSetting.set(MainActivity.curcpu,current);
    }

    private void setCpuView(int i){
        //if(nCpus<=1) return;
        for(int k=0; k<nCpus; k++) {
            setCpuOnOff(k);
            setCpuNo(k,i);
        }
    }
    private void setCpuNo(int i,int k){
            final LinearLayout l = (LinearLayout) lcurcpu.getChildAt(i);
            final TextView nc = (TextView) l.findViewById(R.id.ncpu);
            if (i == k) {
                nc.setText(Integer.toString(i + 1));
            } else {
                nc.setText(" ");
            }
    }

    private void setCpuOnOff(int i){
            final LinearLayout l = (LinearLayout) lcurcpu.getChildAt(i);
            final View vc = (View) l.findViewById(R.id.vcpu);
            if (MainActivity.mCPUon.get(i).equals("0")) {
                vc.setBackgroundColor(getResources().getColor(R.color.pc_light_gray));
            } else {
                vc.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
    }

    public void setCPUval(int i){
        setCpuView(i);

        mMaxSpeedText.setText(Helpers.toMHz(MainActivity.mMaxFreqSetting.get(i)));
        mMaxSlider.setProgress(Arrays.asList(MainActivity.mAvailableFrequencies).indexOf(MainActivity.mMaxFreqSetting.get(i)));

        mMinSpeedText.setText(Helpers.toMHz(MainActivity.mMinFreqSetting.get(i)));
        mMinSlider.setProgress(Arrays.asList(MainActivity.mAvailableFrequencies).indexOf(MainActivity.mMinFreqSetting.get(i)));

        if( i>0 && (mIsDynFreq || new File(HARD_LIMIT_PATH).exists()) ){
            mMaxSlider.setEnabled(false);
            mMinSlider.setEnabled(false);
        }
        else{
            mMaxSlider.setEnabled(true);
            mMinSlider.setEnabled(true);
        }

        if(MainActivity.mCPUon.get(i).equals("0")){
            mMaxSlider.setEnabled(false);
            mMinSlider.setEnabled(false);
        }
        mPreferences.edit().putString("cpuon" + i, MainActivity.mCPUon.get(i)).apply();
    }

    protected class CurCPUThread extends Thread {
        private boolean mInterrupt = false;
        private String onlist="";
        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    onlist="";
                    for(int i=0;i<nCpus;i++) {
                        if (new File(CPU_ON_PATH.replace("cpu0", "cpu" + i)).exists()) {
                            final String on=Helpers.readOneLine(CPU_ON_PATH.replace("cpu0", "cpu" + i));
                            if((on!=null) && (on.length()>0)){
                                onlist+=on.trim()+":";
                            }
                            else{
                                onlist+="0:";
                            }
                        }
                        else{
                            onlist+="1:";
                        }
                    }
                    if(new File(CUR_CPU_PATH.replace("cpu0","cpu"+MainActivity.curcpu)).exists()){
                        final String curfreq=Helpers.readOneLine(CUR_CPU_PATH.replace("cpu0","cpu"+MainActivity.curcpu));
                        if((curfreq!=null) && (curfreq.length()>0)){
                            mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0,curfreq+":"+onlist));
                        }
                        else {
                            mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, "0:"+onlist));
                        }
                    }
                    else{
                        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0,"0:"+onlist));
                    }
                    sleep(600);
                }
            }
            catch (InterruptedException e) {
                Log.d(TAG, "CPU thread error "+e);
            }
        }
    }

    protected Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            final String r=(String) msg.obj;
            //Log.d(TAG, "CPU onoff: "+r);
            mCurFreq.setText(Helpers.toMHz(r.split(":")[0]));
            for(int i=0;i<nCpus;i++) {
                MainActivity.mCPUon.set(i, r.split(":")[i + 1]);
                try {
                    setCpuOnOff(i);
                }
                catch (Exception e){}
            }
        }
    };


    private void updateSharedPrefs(String var, String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(var, value).apply();
        Intent intent = new Intent(INTENT_PP);
        intent.putExtra("from",getString(R.string.app_name));
        context.sendBroadcast(intent);

    }

}

