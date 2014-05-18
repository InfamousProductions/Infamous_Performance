package com.infamous.performance.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import java.util.Comparator;

public class CPUSettings extends Fragment implements SeekBar.OnSeekBarChangeListener, Constants {

    private SeekBar mMaxSlider;
    private SeekBar mMinSlider;
    private Spinner mGovernor;
    private Spinner mIo;
    private TextView mCurFreq;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private String[] mAvailableFrequencies;
    private CurCPUThread mCurCPUThread;
    SharedPreferences mPreferences;
    private boolean mIsTegra3 = false;
    private boolean mIsDynFreq = false;
    private Context context;
    private final String supported[]={"ondemand","ondemandplus","lulzactive","lulzactiveW","interactive","hyper","conservative","lionheart","adaptive","intellidemand"};
    private TextView mCurCpu;
    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        res=getResources();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(savedInstanceState!=null) {
            MainActivity.curcpu=savedInstanceState.getInt("curcpu");
            MainActivity.mMaxFreqSetting[MainActivity.curcpu]=savedInstanceState.getString("maxfreq");
            MainActivity.mMinFreqSetting[MainActivity.curcpu]=savedInstanceState.getString("minfreq");
            MainActivity.mCurGovernor[MainActivity.curcpu]=savedInstanceState.getString("governor");
            MainActivity.mCurIO[MainActivity.curcpu]=savedInstanceState.getString("io");
            MainActivity.mCPUon[MainActivity.curcpu]=savedInstanceState.getString("cpuon");
        }
        else{
            getCPUval();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cpu_settings, root, false);

        mIsTegra3 = new File(TEGRA_MAX_FREQ_PATH).exists();
        mIsDynFreq = new File(DYN_MAX_FREQ_PATH).exists() && new File(DYN_MIN_FREQ_PATH).exists();

        mCurCpu = (TextView) view.findViewById(R.id.curcpu);
        mCurFreq = (TextView) view.findViewById(R.id.current_speed);
        mCurFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.nCpus==1) return;
                if(MainActivity.curcpu>=(MainActivity.nCpus-1)) MainActivity.curcpu=0;
                else  MainActivity.curcpu++;
                getCPUval();
                setCPUval(MainActivity.curcpu);
            }
        });

        mCurFreq.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                if(new File(CPU_ON_PATH.replace("cpu0","cpu"+MainActivity.curcpu)).exists() && MainActivity.curcpu>0){
                    final StringBuilder sb = new StringBuilder();
                    if(MainActivity.mCPUon[MainActivity.curcpu].equals("1")){
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"0\";\n");
                        MainActivity.mCPUon[MainActivity.curcpu]="0";
                    }
                    else{
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append("\" \"1\";\n");
                        MainActivity.mCPUon[MainActivity.curcpu]="1";
                    }
                    Helpers.shExec(sb,context,true);

                    setCPUval(MainActivity.curcpu);
                }

                return true;
            }
        });


        mAvailableFrequencies = new String[0];
        final String availableFrequenciesLine = Helpers.readOneLine(STEPS_PATH);
        if (availableFrequenciesLine != null) {
            mAvailableFrequencies = availableFrequenciesLine.split(" ");
            Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
                }
            });
        }


        int mFrequenciesNum = mAvailableFrequencies.length - 1;

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
        mGovernor.setSelection(Arrays.asList(mAvailableGovernors).indexOf(MainActivity.mCurGovernor[MainActivity.curcpu]));
        mGovernor.post(new Runnable() {
            public void run() {
                mGovernor.setOnItemSelectedListener(new GovListener());
            }
        });


        String[] mAvailableIo = Helpers.getAvailableIOSchedulers(IO_SCHEDULER_PATH);
        mIo = (Spinner) view.findViewById(R.id.pref_io);

        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String aMAvailableIo : mAvailableIo) {
            ioAdapter.add(aMAvailableIo);
        }
        mIo.setAdapter(ioAdapter);
        mIo.setSelection(Arrays.asList(mAvailableIo).indexOf(MainActivity.mCurIO[MainActivity.curcpu]));
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
                    final String r=Helpers.readCPU(context,MainActivity.nCpus);
                    if(r!=null){
                        for (int i = 0; i < MainActivity.nCpus; i++){
                            editor.putString(PREF_MIN_CPU+i, r.split(":")[i*5]);
                            editor.putString(PREF_MAX_CPU+i, r.split(":")[i*5+1]);
                            editor.putString(PREF_GOV, r.split(":")[i*5+2]);
                            editor.putString(PREF_IO, r.split(":")[i*5+3]);
                            editor.putString("cpuon" + i, r.split(":")[i*5+4]);
                        }
                    }
                }
                editor.commit();
            }
        });

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
                    if (aSupported.equals(MainActivity.mCurGovernor[MainActivity.curcpu])) {
                        if(new File(GOV_SETTINGS_PATH + MainActivity.mCurGovernor[MainActivity.curcpu]).exists()){
                            intent = new Intent(context, GovSetActivity.class);
                            intent.putExtra("curgov", MainActivity.mCurGovernor[MainActivity.curcpu]);
                            startActivity(intent);
                        }
                        break;
                    }
                }
                break;
            case R.id.io_settings:
                if(new File(IO_TUNABLE_PATH).exists()){
                    intent = new Intent(context, IOSetActivity.class);
                    intent.putExtra("curio", MainActivity.mCurIO[MainActivity.curcpu]);
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
        switch (seekBar.getId()){
            case R.id.max_slider:
                sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting[MainActivity.curcpu]).append(" > ").append(MAX_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append(";\n");
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting[MainActivity.curcpu]).append(" > ").append(DYN_MAX_FREQ_PATH).append(";\n");
                }
                if (mIsTegra3) {
                    sb.append("busybox echo ").append(MainActivity.mMaxFreqSetting[MainActivity.curcpu]).append(" > ").append(TEGRA_MAX_FREQ_PATH).append(";\n");
                }
                break;
            case R.id.min_slider:
                sb.append("busybox echo ").append(MainActivity.mMinFreqSetting[MainActivity.curcpu]).append(" > ").append(MIN_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.curcpu)).append(";\n");
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMinFreqSetting[MainActivity.curcpu]).append(" > ").append(DYN_MIN_FREQ_PATH).append(";\n");
                }
                break;
        }
        Helpers.shExec(sb,context,true);
        //Helpers.updateAppWidget(context);
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final StringBuilder sb = new StringBuilder();
            String selected = parent.getItemAtPosition(pos).toString();
            for (int i = 0; i < MainActivity.nCpus; i++){
                //sb.append("busybox echo ").append(selected).append(" > ").append(GOVERNOR_PATH.replace("cpu0", "cpu" + i)).append(";\n");
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
            MainActivity.mCurGovernor[MainActivity.curcpu]=selected;
            updateSharedPrefs(PREF_GOV, selected);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            String selected = parent.getItemAtPosition(pos).toString();
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
            MainActivity.mCurIO[MainActivity.curcpu]=selected;
            updateSharedPrefs(PREF_IO, selected);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }
    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putInt("curcpu",MainActivity.curcpu);
        saveState.putString("maxfreq",MainActivity.mMaxFreqSetting[MainActivity.curcpu]);
        saveState.putString("minfreq",MainActivity.mMinFreqSetting[MainActivity.curcpu]);
        saveState.putString("governor",MainActivity.mCurGovernor[MainActivity.curcpu]);
        saveState.putString("io",MainActivity.mCurIO[MainActivity.curcpu]);
        saveState.putString("cpuon",MainActivity.mCPUon[MainActivity.curcpu]);
    }
    @Override
    public void onResume() {
        if (mCurCPUThread == null) {
            mCurCPUThread = new CurCPUThread();
            mCurCPUThread.start();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Helpers.updateAppWidget(context);
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
        String current = "";
        current = mAvailableFrequencies[progress];
        int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMinFreqSetting[MainActivity.curcpu] = current;
        }
        mMaxSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMaxFreqSetting[MainActivity.curcpu] = current;
        updateSharedPrefs(PREF_MAX_CPU+MainActivity.curcpu, current);
    }

    public void setMinSpeed(int progress) {
        String current = "";
        current = mAvailableFrequencies[progress];
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMaxFreqSetting[MainActivity.curcpu] = current;
        }
        mMinSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMinFreqSetting[MainActivity.curcpu] = current;
        updateSharedPrefs(PREF_MIN_CPU+MainActivity.curcpu, current);
    }



    public void setCPUval(int i){
        mMaxSpeedText.setText(Helpers.toMHz(MainActivity.mMaxFreqSetting[i]));
        mMaxSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(MainActivity.mMaxFreqSetting[i]));

        mMinSpeedText.setText(Helpers.toMHz(MainActivity.mMinFreqSetting[i]));
        mMinSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(MainActivity.mMinFreqSetting[i]));

        if(mIsDynFreq && MainActivity.curcpu>0){
            mMaxSlider.setEnabled(false);
            mMinSlider.setEnabled(false);
        }
        else{
            mMaxSlider.setEnabled(true);
            mMinSlider.setEnabled(true);
        }
        mCurCpu.setText(Integer.toString(i+1));
        mCurCpu.setTextColor(res.getColor(R.color.pc_blue));

        if(MainActivity.mCPUon[MainActivity.curcpu].equals("0")){
             mCurCpu.setTextColor(res.getColor(R.color.pc_red));
             mMaxSlider.setEnabled(false);
             mMinSlider.setEnabled(false);
        }
        mPreferences.edit().putString("cpuon" + MainActivity.curcpu, MainActivity.mCPUon[MainActivity.curcpu]).apply();
    }

    protected class CurCPUThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(600);
                    if(new File(CUR_CPU_PATH.replace("cpu0","cpu"+MainActivity.curcpu)).exists()){
                        final String curfreq=Helpers.readOneLine(CUR_CPU_PATH.replace("cpu0","cpu"+MainActivity.curcpu));
                        if((curfreq!=null)&&(curfreq.length()>0))
                            mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0,curfreq));
                        else
                            mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0,"0"));
                    }
                    else{
                        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0,"0"));
                    }
                }
            }
            catch (InterruptedException e) {
                Log.d(TAG, "CPU thread error "+e);
            }
        }
    }

    protected Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
        mCurFreq.setText(Helpers.toMHz((String) msg.obj));
        }
    };

    private void updateSharedPrefs(String var, String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(var, value).commit();

        Intent intent = new Intent(INTENT_PP);
        intent.putExtra("from",TAG);
        context.sendBroadcast(intent);
        //Helpers.updateAppWidget(context);
    }

    private void getCPUval(){
        final String r=Helpers.readCPU(context,MainActivity.nCpus);
        if(r!=null){
            for (int i = 0; i < MainActivity.nCpus; i++){
                MainActivity.mMinFreqSetting[i]=r.split(":")[i*5];
                MainActivity.mMaxFreqSetting[i]=r.split(":")[i*5+1];
                MainActivity.mCurGovernor[i]=r.split(":")[i*5+2];
                MainActivity.mCurIO[i]=r.split(":")[i*5+3];
                MainActivity.mCPUon[i]=r.split(":")[i*5+4];
            }
        }
    }
}

