/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * Jared Rummler Copyright (C) 2012 James Roberts
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

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.infamous.performance.R;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.Voltage;

import java.util.ArrayList;
import java.util.List;

public class VoltageControlSettings extends Fragment implements Constants {
    private List<Voltage> mVoltages;
    private ListAdapter mAdapter;
    SharedPreferences mPreferences;
    private Voltage mVoltage;
    private Context context;
    private int vstep=25;
    private int mstep=25;
    private int vmin=600;
    private int nvsteps=40;
    private String um=" mV";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mVoltages = getVolts(mPreferences);
        mAdapter = new ListAdapter(context);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltage_settings, root, false);

        final ListView listView = (ListView) view.findViewById(R.id.ListView);
        final Switch setOnBoot = (Switch) view.findViewById(R.id.applyAtBoot);


        if (mVoltages.isEmpty()) {
            (view.findViewById(R.id.emptyList)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.BottomBar)).setVisibility(View.GONE);
        }

        setOnBoot.setChecked(mPreferences.getBoolean(VOLTAGE_SOB, false));
        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                        mPreferences.edit().putBoolean(VOLTAGE_SOB,isChecked).apply();
                        if (isChecked){
                            String warningMessage = getString(R.string.volt_info);

                            new AlertDialog.Builder(context)
                                    .setMessage(warningMessage)
                                    .setNegativeButton(getString(R.string.cancel),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,int which) {
                                                    setOnBoot.setChecked(false);
                                                }
                                            })
                                    .setPositiveButton(getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,int which) {}
                                            }).create().show();
                        }

                    }
                });

        (view.findViewById(R.id.applyBtn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			final StringBuilder sb = new StringBuilder();
            final String vdd=Helpers.getVoltagePath();
    		if (vdd.equals(VDD_PATH)) {
				for (final Voltage volt : mVoltages) {
					if(!volt.getSavedMV().equals(volt.getCurrentMv())){
						for (int i = 0; i < Helpers.getNumOfCpus(); i++) {
                            sb.append("busybox echo \"").append(volt.getFreq()).append(" ").append(volt.getSavedMV()).append("\" > ").append(vdd.replace("cpu0", "cpu" + i)).append(";\n");
						}
					}
				}
			}
            else if(vdd.equals(VDD_TABLE)) {
                for (final Voltage volt : mVoltages) {
                    if(!volt.getSavedMV().equals(volt.getCurrentMv())){
                        sb.append("busybox echo \"").append(volt.getFreq()).append(" ").append(volt.getSavedMV()).append("\" > ").append(vdd).append(";\n");
                    }
                }
            }
			else{
				final StringBuilder b = new StringBuilder();
				for (final Voltage volt : mVoltages) {
					b.append(volt.getSavedMV()).append(" ");
				}
                if(vdd.equals(COMMON_VDD_PATH)){
                    sb.append("busybox echo \"").append(b.toString()).append("\" > ").append(vdd).append(";\n");
                }
                else{
                    for (int i = 0; i < Helpers.getNumOfCpus(); i++) {
                        sb.append("busybox echo \"").append(b.toString()).append("\" > ").append(vdd.replace("cpu0", "cpu" + i)).append(";\n");
                    }
                }
			}
			Helpers.shExec(sb,context,true);

			final List<Voltage> volts = getVolts(mPreferences);
			mVoltages.clear();
			mVoltages.addAll(volts);
			mAdapter.notifyDataSetChanged();
			}
		});

        mAdapter.setListItems(mVoltages);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                mVoltage = mVoltages.get(position);
                showDialog(vmin,vstep,nvsteps);
            }
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.voltage_control_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab),(ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, PCSettings.class);
                startActivity(intent);
                break;
            case R.id.volt_increase:
                IncreasebyStep(mstep);
                break;
            case R.id.volt_decrease:
                IncreasebyStep(-1*mstep);
                break;
            case R.id.reset:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.mt_reset))
                        .setMessage(getString(R.string.reset_msg))
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                }
                                })
                        .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ResetVolt();
                                        }
                }).create().show();
                break;
        }
        return true;
    }

    private void ResetVolt() {
        for (final Voltage volt : mVoltages) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.remove(volt.getFreq()).commit();
        }
        final List<Voltage> volts = getVolts(mPreferences);
        mVoltages.clear();
        mVoltages.addAll(volts);
        mAdapter.notifyDataSetChanged();
    }
    private void IncreasebyStep(final int pas) {
        for (final Voltage volt : mVoltages) {
            String value=Integer.toString( Integer.parseInt(volt.getSavedMV())+pas);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(volt.getFreq(), value).commit();
        }
        final List<Voltage> volts = getVolts(mPreferences);
        mVoltages.clear();
        mVoltages.addAll(volts);
        mAdapter.notifyDataSetChanged();
    }

    private List<Voltage> getVolts(final SharedPreferences preferences) {
        final List<Voltage> volts = new ArrayList<Voltage>();
        final String vddpath=Helpers.getVoltagePath();
        boolean ismicro=false;
        try {
            final String tablevdd = Helpers.readFileViaShell(vddpath, false);
            if(tablevdd!=null){
                for (final String line : tablevdd.split("\n")) {
                    if (line!=null && line.contains(":")) {
                        final String[] values = line.split(":");
                        String freq = values[0].trim();
                        if(freq.contains("mhz")){
                            freq = values[0].replace("mhz", "").trim();
                            freq=String.valueOf(Integer.parseInt(freq)*1000);
                        }

                        final String currentMv = values[1].replace("mV", "").trim();
                        if(currentMv.length()>5){
                            ismicro=true;
                        }
                        final String savedMv = preferences.getString(freq,currentMv);
                        final Voltage voltage = new Voltage();
                        voltage.setFreq(freq);
                        voltage.setCurrentMV(currentMv);
                        voltage.setSavedMV(savedMv);
                        volts.add(voltage);
                    }
                }
                if(ismicro){
                    vstep=12500;
                    mstep=25000;
                    nvsteps=80;
                    vmin=600000;
                    um=" μV";
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, vddpath + " error reading");
        }

        return volts;
   }

    public static List<Voltage> bootgetVolts(final SharedPreferences preferences) {
        final List<Voltage> volts = new ArrayList<Voltage>();
        final String vddpath=Helpers.getVoltagePath();
        try {
            final String tablevdd = Helpers.readFileViaShell(vddpath, false);
            if(tablevdd!=null){
                for (final String line : tablevdd.split("\n")) {

                    if (line!=null && line.contains(":")) {
                        final String[] values = line.split(":");
                        String freq = values[0].trim();
                        if(freq.contains("mhz")){
                            freq = values[0].replace("mhz", "").trim();
                            freq=String.valueOf(Integer.parseInt(freq)*1000);
                        }

                        final String currentMv = values[1].replace("mV", "").trim();

                        final String savedMv = preferences.getString(freq,currentMv);
                        final Voltage voltage = new Voltage();
                        voltage.setFreq(freq);
                        voltage.setCurrentMV(currentMv);
                        voltage.setSavedMV(savedMv);
                        volts.add(voltage);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, vddpath + " error reading");
        }

        return volts;
    }

    private int getNearestStepIndex(final int value,final int min,final int step,final int total) {
        int index = 0;
        for (int k=0;k<total;k++) {
            if (value > (k*step+min)) index++;
            else break;
        }
        return index;
    }

    private void showDialog(final int min,final int step,final int total) {
        final LayoutInflater factory = LayoutInflater.from(context);
        final View voltageDialog = factory.inflate(R.layout.voltage_dialog,null);

        final SeekBar voltageSeek = (SeekBar) voltageDialog.findViewById(R.id.voltageSeek);
        final TextView voltageMeter = (TextView) voltageDialog.findViewById(R.id.voltageMeter);

        final String savedMv = mVoltage.getSavedMV();
        final int savedVolt = Integer.parseInt(savedMv);

        voltageMeter.setText(savedMv + um);
        voltageSeek.setMax(total);
        voltageSeek.setProgress(getNearestStepIndex(savedVolt,vmin,vstep,nvsteps));
        voltageSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress,boolean fromUser) {
                if (fromUser) {
                    final String volt = Integer.toString(progress*step+min);
                    voltageMeter.setText(volt + um);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        new AlertDialog.Builder(context)
                .setTitle(mVoltage.getFreq()+ " kHz")
                .setView(voltageDialog)
                .setPositiveButton(getResources().getString(R.string.ps_volt_save),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                                final String value = Integer.toString(voltageSeek.getProgress()*step+min);
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString(mVoltage.getFreq(), value).commit();
                                mVoltage.setSavedMV(value);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                            }
                        }
                ).create().show();
    }

    public class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Voltage> results;

        public ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return results.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_volt, null);
                holder = new ViewHolder();
                holder.mFreq = (TextView) convertView.findViewById(R.id.Freq);
                holder.mCurrentMV = (TextView) convertView.findViewById(R.id.mVCurrent);
                holder.mSavedMV = (TextView) convertView.findViewById(R.id.mVSaved);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Voltage voltage = mVoltages.get(position);
            holder.setFreq(voltage.getFreq());
            holder.setCurrentMV(voltage.getCurrentMv());
            holder.setSavedMV(voltage.getSavedMV());
            return convertView;
        }

        public void setListItems(List<Voltage> mVoltages) {
            results = mVoltages;
        }

        public class ViewHolder {
            private TextView mFreq;
            private TextView mCurrentMV;
            private TextView mSavedMV;

            public void setFreq(final String freq) {
                mFreq.setText(freq + " kHz");
            }

            public void setCurrentMV(final String currentMv) {
                mCurrentMV.setText(getResources().getString(R.string.ps_volt_current_voltage) + currentMv + um);
            }

            public void setSavedMV(final String savedMv) {
                mSavedMV.setText(getResources().getString(R.string.ps_volt_setting_to_apply) + savedMv + um);
            }
        }
    }
}

