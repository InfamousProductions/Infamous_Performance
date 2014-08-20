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
import android.app.Fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;
import com.infamous.performance.R;
import com.infamous.performance.activities.PCSettings;
import com.infamous.performance.util.CMDProcessor;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;


import java.io.File;

public class BatteryInfo extends Fragment implements SeekBar.OnSeekBarChangeListener, Constants {
    TextView mbattery_percent;
    TextView mbattery_volt;
    TextView mbattery_status;
    TextView mBlxVal;
    ImageView mBattIcon;
    Switch mFastchargeOnBoot;
    SharedPreferences mPreferences;
    private String mFastChargePath;
    private Context context;
    private BroadcastReceiver batteryInfoReceiver;
    private int level,plugged;
    private boolean no_settings=true;
    private final String bstatfile="/data/system/batterystats.bin";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
  	    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        batteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //int  health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
                //String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
                //boolean  present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
                //int  rawvoltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);

                plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
                int  scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
                int  lev= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                int  status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
                int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);

                level=lev*scale/100;
                mbattery_percent.setText(level+"%");
                if (new File(BAT_VOLT_PATH).exists()) {
                    int volt = Integer.parseInt(Helpers.readOneLine(BAT_VOLT_PATH));
                    if (volt > 5000) volt = (int) Math.round(volt / 1000.0);// in microvolts
                    mbattery_volt.setText(volt + " mV");
                }
                switch ((int) Math.ceil(level / 20.0)){
                    case 0:
                        mBattIcon.setImageResource(R.drawable.battery_0);
                        break;
                    case 1:
                        mBattIcon.setImageResource(R.drawable.battery_1);
                        break;
                    case 2:
                        mBattIcon.setImageResource(R.drawable.battery_2);
                        break;
                    case 3:
                        mBattIcon.setImageResource(R.drawable.battery_3);
                        break;
                    case 4:
                        mBattIcon.setImageResource(R.drawable.battery_4);
                        break;
                    case 5:
                        mBattIcon.setImageResource(R.drawable.battery_5);
                        break;
                }
                mbattery_status.setText((temperature/10)+"°C  "+getResources().getStringArray(R.array.batt_status)[status]);
            }
        };
        //getActivity().registerReceiver(batteryInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );
        setRetainInstance(true);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.batt_menu, menu);
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
            case R.id.calib:
                if(!new File(bstatfile).exists()){
                    Toast.makeText(context,getString(R.string.no_file,bstatfile),Toast.LENGTH_SHORT);
                    break;
                }
                if((level==100)&&(plugged!=0)) {
                    //calibrate
                    final LayoutInflater factory = LayoutInflater.from(context);
                    final View cDialog = factory.inflate(R.layout.calib_dialog,null);
                    final CheckBox sw = (CheckBox) cDialog.findViewById(R.id.sw);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.mt_calib))
                            .setView(cDialog)
                            .setMessage(getString(R.string.mt_calib_ok))
                            .setNegativeButton(getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            )
                            .setPositiveButton(getString(R.string.mt_calib),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            StringBuilder sb=new StringBuilder();
                                            sb.append("busybox rm -f "+bstatfile+";\n");
                                            if (sw.isChecked()) {
                                                mPreferences.edit().putBoolean("booting",true).commit();
                                                sb.append("reboot;\n");
                                            }
                                            Helpers.shExec(sb,context,true);
                                            dialog.cancel();
                                        }
                                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    new AlertDialog.Builder(context).setTitle(getString(R.string.mt_calib)).setMessage(getString(R.string.mt_calib_nok))
                            .setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    }).create().show();
                }
                break;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_info, root, false);

        mbattery_percent = (TextView) view.findViewById(R.id.batt_percent);
        mbattery_volt = (TextView) view.findViewById(R.id.batt_volt);
        mbattery_status = (TextView) view.findViewById(R.id.batt_status);
        mBattIcon=(ImageView) view.findViewById(R.id.batt_icon);

        if (new File(BAT_VOLT_PATH).exists()){
                int volt=Integer.parseInt(Helpers.readOneLine(BAT_VOLT_PATH));
                if(volt>5000) volt = (int) Math.round(volt / 1000.0);// in microvolts
                mbattery_volt.setText(volt+" mV");
                mBattIcon.setVisibility(ImageView.GONE);
                mbattery_volt.setVisibility(TextView.VISIBLE);
                mbattery_volt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                        if(powerUsageIntent.resolveActivity(context.getPackageManager())!=null) startActivity(powerUsageIntent);
                    }
                });
                mbattery_volt.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View view) {
                        mBattIcon.setVisibility(ImageView.VISIBLE);
                        mbattery_volt.setVisibility(TextView.GONE);
                        return true;
                    }
                });
        }
        else{
            mBattIcon.setVisibility(ImageView.VISIBLE);
            mbattery_volt.setVisibility(TextView.GONE);
            mBattIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                        startActivity(powerUsageIntent);
                    }
                    catch(Exception e){
                    }
                }
            });
        }

        SeekBar mBlxSlider = (SeekBar) view.findViewById(R.id.blx_slider);
        if (new File(BLX_PATH).exists()) {
            no_settings=false;
            mBlxSlider.setMax(100);

            mBlxVal = (TextView) view.findViewById(R.id.blx_val);
            mBlxVal.setText(getString(R.string.blx_title)+" " + Helpers.readOneLine(BLX_PATH)+"%");

            mBlxSlider.setProgress(Integer.parseInt(Helpers.readOneLine(BLX_PATH)));
            mBlxSlider.setOnSeekBarChangeListener(this);
            Switch mSetOnBoot = (Switch) view.findViewById(R.id.blx_sob);
            mSetOnBoot.setChecked(mPreferences.getBoolean(BLX_SOB, false));
            mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    final SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putBoolean(BLX_SOB, checked);
                    if (checked) {
                        editor.putInt(PREF_BLX, Integer.parseInt(Helpers.readOneLine(BLX_PATH)));
                    }
                    editor.commit();
                }
            });
        }
        else{
            LinearLayout mpart = (LinearLayout) view.findViewById(R.id.blx_layout);
            mpart.setVisibility(LinearLayout.GONE);
        }
        mFastChargePath=Helpers.fastcharge_path();
        if (mFastChargePath!=null) {
            no_settings=false;
            mFastchargeOnBoot = (Switch) view.findViewById(R.id.fastcharge_sob);
            mFastchargeOnBoot.setChecked(mPreferences.getBoolean(PREF_FASTCHARGE, Helpers.readOneLine(mFastChargePath).equals("1")));
            mFastchargeOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v,boolean checked) {
                    mPreferences.edit().putBoolean(PREF_FASTCHARGE,checked).apply();
                    final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (checked){
                        new CMDProcessor().su.runWaitFor("busybox echo 1 > " + mFastChargePath);
                        Notification n = new Notification.Builder(context)
                                .setContentTitle(context.getText(R.string.app_name))
                                .setContentText(context.getText(R.string.fast_charge_notification_title))
                                .setTicker(context.getText(R.string.fast_charge_notification_title))
                                .setSmallIcon(R.drawable.ic_fastcharge)
                                .setWhen(System.currentTimeMillis()).getNotification();
                        //n.flags = Notification.FLAG_NO_CLEAR;
                        nm.notify(1337, n);
                    }
                    else{
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + mFastChargePath);
                        nm.cancel(1337);
                    }
                }
            });
        }
         else{
            LinearLayout mpart = (LinearLayout) view.findViewById(R.id.fastcharge_layout);
            mpart.setVisibility(LinearLayout.GONE);
         }
        if(no_settings) {
            LinearLayout ns = (LinearLayout) view.findViewById(R.id.no_settings);
            ns.setVisibility(LinearLayout.VISIBLE);
        }

        return view;
    }

   @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mBlxVal.setText(getString(R.string.blx_title)+" " + progress + "%");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        new CMDProcessor().su.runWaitFor("busybox echo " + seekBar.getProgress() + " > " + BLX_PATH);
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PREF_BLX, seekBar.getProgress()).commit();
    }
    @Override
    public void onStop() {
        try{
            getActivity().unregisterReceiver(batteryInfoReceiver);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        super.onStop();
    }
    @Override
    public void onResume() {
        super.onResume();
        try{
            getActivity().registerReceiver(batteryInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
