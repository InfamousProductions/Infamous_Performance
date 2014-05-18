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

package com.infamous.performance.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.infamous.performance.R;
import com.infamous.performance.fragments.VoltageControlSettings;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.GPUClass;
import com.infamous.performance.util.Helpers;
import com.infamous.performance.util.VibratorClass;
import com.infamous.performance.util.Voltage;


import java.io.File;
import java.util.List;


public class BootService extends Service implements Constants {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) stopSelf();
        new BootWorker(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BootWorker extends AsyncTask<Void, Void, String> {
        Context c;
        final int ncpus=Helpers.getNumOfCpus();
        final String FASTCHARGE_PATH=Helpers.fastcharge_path();
        SharedPreferences preferences;

        public BootWorker(Context c) {
            this.c = c;
            this.preferences = PreferenceManager.getDefaultSharedPreferences(c);
        }
        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(Void... args) {
            final StringBuilder sb = new StringBuilder();
            VibratorClass vib=new VibratorClass();
            final String VIBE_PATH=vib.get_path();
            final String BLN_PATH=Helpers.bln_path();
            final String WIFIPM_PATH=Helpers.wifipm_path();
            final String gov = preferences.getString(PREF_GOV, Helpers.readOneLine(GOVERNOR_PATH));
            final String io = preferences.getString(PREF_IO, Helpers.getIOScheduler(IO_SCHEDULER_PATH));
            final float maxdisk = Helpers.getMem("MemTotal") / 1024;
            final String hotpath=Helpers.hotplug_path();
            final GPUClass gpu=new GPUClass();
            String s;

            int ksm=0;
            String ksmpath=KSM_RUN_PATH;
            if (new File(UKSM_RUN_PATH+"/run").exists()) {
                ksm=1;
                ksmpath=UKSM_RUN_PATH;
            }
            if(!Helpers.binExist("mpdecision").equals(NOT_FOUND) && preferences.getBoolean("mpdecision_boot",false)){
                if(preferences.getBoolean("pref_mpdecision",false)){
                    if(!Helpers.moduleActive("mpdecision")){
                        sb.append("mpdecisionstart;\n");
                    }
                }
                else{
                    if(Helpers.moduleActive("mpdecision")){
                        sb.append("stop mpdecision;\n");
                    }
                }
            }
            if (preferences.getBoolean(CPU_SOB, false)) {
                for (int i = 0; i < ncpus; i++) {
                    if(new File(CPU_ON_PATH.replace("cpu0","cpu"+i)).exists() && i>0){
                        if(preferences.getString("cpuon"+i, "0").equals("1")){
                            sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + i)).append("\" \"1\";\n");
                        }
                    }
                    if (new File(MAX_FREQ_PATH.replace("cpu0","cpu"+i)).exists()) {
                        final String max = preferences.getString(PREF_MAX_CPU+i, Helpers.readOneLine(MAX_FREQ_PATH).replace("cpu0","cpu"+i));
                        sb.append("busybox echo ").append(max).append(" > ").append(MAX_FREQ_PATH.replace("cpu0", "cpu" + i)).append(";\n");
                    }
                    if (new File(MIN_FREQ_PATH.replace("cpu0","cpu"+i)).exists()) {
                        final String min = preferences.getString(PREF_MIN_CPU+i, Helpers.readOneLine(MIN_FREQ_PATH).replace("cpu0","cpu"+i));
                        sb.append("busybox echo ").append(min).append(" > ").append(MIN_FREQ_PATH.replace("cpu0", "cpu" + i)).append(";\n");
                    }

                    //sb.append("busybox echo ").append(gov).append(" > ").append(GOVERNOR_PATH.replace("cpu0", "cpu" + i)).append(";\n");
                    sb.append("set_val \"").append(GOVERNOR_PATH.replace("cpu0", "cpu" + i)).append("\" \"").append(gov).append("\";\n");
                }


                if (new File(TEGRA_MAX_FREQ_PATH).exists()) {
                    final String tegramax=preferences.getString(PREF_MAX_CPU+"0", Helpers.readOneLine(TEGRA_MAX_FREQ_PATH));
                    sb.append("busybox echo ").append(tegramax).append(" > ").append(TEGRA_MAX_FREQ_PATH).append(";\n");
                }
                if(new File(DYN_MAX_FREQ_PATH).exists()){
                    final String max0=preferences.getString(PREF_MAX_CPU+"0", Helpers.readOneLine(MAX_FREQ_PATH));
                    sb.append("busybox echo ").append(max0).append(" > ").append(DYN_MAX_FREQ_PATH).append(";\n");
                }
                if(new File(DYN_MIN_FREQ_PATH).exists()){
                    final String min0=preferences.getString(PREF_MIN_CPU+"0", Helpers.readOneLine(MIN_FREQ_PATH));
                    sb.append("busybox echo ").append(min0).append(" > ").append(DYN_MIN_FREQ_PATH).append(";\n");
                }
                for(byte i=0;i<2; i++){
                    if (new File(IO_SCHEDULER_PATH.replace("mmcblk0","mmcblk"+i)).exists()){
                        sb.append("busybox echo ").append(io).append(" > ").append(IO_SCHEDULER_PATH.replace("mmcblk0","mmcblk"+i)).append(";\n");
                    }
                }
            }
            if (preferences.getBoolean("so_minmax_boot", false)) {
                if (new File(SO_MAX_FREQ).exists()) {
                    s=preferences.getString("pref_so_max", Helpers.readOneLine(SO_MAX_FREQ));
                    sb.append("busybox echo ").append(s).append(" > ").append(SO_MAX_FREQ).append(";\n");
                }
                if (new File(SO_MIN_FREQ).exists()) {
                    s=preferences.getString("pref_so_min", Helpers.readOneLine(SO_MIN_FREQ));
                    sb.append("busybox echo ").append(s).append(" > ").append(SO_MIN_FREQ).append(";\n");
                }
            }
            if(hotpath!=null){
                if (preferences.getBoolean(HOTPLUG_SOB, false)) {
                    s = preferences.getString("hotplug", "");
                    if (!s.equals("")) {
                        String p[]=s.split(";");
                        for (String aP : p) {
                            if(aP!=null && aP.contains(":")){
                                final String pn[]=aP.split(":");
                                sb.append("busybox echo ").append(pn[1]).append(" > ").append(hotpath).append("/").append(pn[0]).append(";\n");
                            }
                        }
                    }
                }
            }
            if (new File(GEN_HP).exists()) {
                if(preferences.getBoolean("pref_hp", false)){
                    sb.append("busybox echo 1 > ").append(GEN_HP).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(GEN_HP).append(";\n");
                }
            }
            if(new File(CPU_QUIET_GOV).exists()){
                if(preferences.getBoolean("cpuq_boot", false)){
                    s=preferences.getString("pref_cpuquiet", Helpers.readOneLine(CPU_QUIET_CUR));
                    sb.append("busybox echo ").append(s).append(" > ").append(CPU_QUIET_CUR).append(";\n");
                }
            }
            if (new File(INTELLI_PLUG).exists()) {
                if(preferences.getBoolean("pref_intelliplug", false)){
                    sb.append("busybox echo 1 > ").append(INTELLI_PLUG).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(INTELLI_PLUG).append(";\n");
                }
            }
            if (new File(ECO_MODE).exists()) {
                if(preferences.getBoolean("pref_ecomode", false)){
                    sb.append("busybox echo 1 > ").append(ECO_MODE).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(ECO_MODE).append(";\n");
                }
            }
            if (new File(MC_PS).exists()) {
                if(preferences.getBoolean("pref_mc_ps", false)){
                    sb.append("busybox echo "+preferences.getString("pref_mcps",Helpers.readOneLine(MC_PS))+" > ").append(MC_PS).append(";\n");
                }
            }
            if(gpu.gpuclk_path()!=null){
                if(preferences.getBoolean("gpu_fmax_boot",false)){
                    sb.append("busybox echo "+preferences.getString("pref_gpu_fmax",Helpers.readOneLine(gpu.gpuclk_path()))+" > ").append(gpu.gpuclk_path()).append(";\n");
                }
            }
            if(gpu.gpugovset_path()!=null){
                if (preferences.getBoolean(GPU_PARAM_SOB, false)) {
                    s = preferences.getString("gpuparam", "");
                    if (!s.equals("")) {
                        String p[]=s.split(";");
                        for (String aP : p) {
                            if(aP!=null && aP.contains(":")){
                                final String pn[]=aP.split(":");
                                sb.append("busybox echo ").append(pn[1]).append(" > ").append(gpu.gpugovset_path()).append("/").append(pn[0]).append(";\n");
                            }
                        }
                    }
                }
            }
            if (preferences.getBoolean(VOLTAGE_SOB, false)) {

                if(Helpers.voltageFileExists()){
                    final List<Voltage> volts = VoltageControlSettings.getVolts(preferences);
                    final String vddpath=Helpers.getVoltagePath();
                    if (vddpath.equals(VDD_PATH)) {
                        for (final Voltage volt : volts) {
                            if(!volt.getSavedMV().equals(volt.getCurrentMv())){
                                for (byte i = 0; i < ncpus; i++) {
                                    sb.append("busybox echo \"").append(volt.getFreq()).append(" ").append(volt.getSavedMV()).append("\" > ").append(vddpath.replace("cpu0", "cpu" + i)).append(";\n");
                                }
                            }
                        }
                    }
                    else if(vddpath.equals(VDD_TABLE)) {
                        for (final Voltage volt : volts) {
                            if(!volt.getSavedMV().equals(volt.getCurrentMv())){
                                sb.append("busybox echo \"").append(volt.getFreq()).append(" ").append(volt.getSavedMV()).append("\" > ").append(vddpath).append(";\n");
                            }
                        }
                    }
                    else{
                        //other formats
                        final StringBuilder b = new StringBuilder();
                        for (final Voltage volt : volts) {
                            b.append(volt.getSavedMV()).append(" ");
                        }
                        if(vddpath.equals(COMMON_VDD_PATH)){
                            sb.append("busybox echo \"").append(b.toString()).append("\" > ").append(vddpath).append(";\n");
                        }
                        else{
                            for (byte i = 0; i < ncpus; i++) {
                                sb.append("busybox echo \"").append(b.toString()).append("\" > ").append(vddpath.replace("cpu0", "cpu" + i)).append(";\n");
                            }
                        }
                    }
                }
            }
            if (preferences.getBoolean(PREF_READ_AHEAD_BOOT, false)) {
                s = preferences.getString(PREF_READ_AHEAD,Helpers.readOneLine(READ_AHEAD_PATH));
                for(byte i=0;i<2;i++){
                    if(new File(READ_AHEAD_PATH.replace("mmcblk0","mmcblk"+i)).exists())
                        sb.append("busybox echo ").append(s).append(" > ").append(READ_AHEAD_PATH.replace("mmcblk0","mmcblk"+i)).append(";\n");
                }
            }
            if (FASTCHARGE_PATH!=null) {
                if(preferences.getBoolean(PREF_FASTCHARGE, false)){
                    sb.append("busybox echo 1 > ").append(FASTCHARGE_PATH).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(FASTCHARGE_PATH).append(";\n");
                }
            }
            if (new File(BLX_PATH).exists()) {
                if (preferences.getBoolean(BLX_SOB, false)) {
                    sb.append("busybox echo ").append(preferences.getInt(PREF_BLX, Integer.parseInt(Helpers.readOneLine(BLX_PATH)))).append(" > ").append(BLX_PATH).append(";\n");
                }
            }
            if (new File(DSYNC_PATH).exists()) {
                if (preferences.getBoolean(PREF_DSYNC, false)) {
                    sb.append("busybox echo 1 > " + DSYNC_PATH + ";\n");
                }
                else{
                    sb.append("busybox echo 0 > " + DSYNC_PATH + ";\n");
                }
            }
            if (new File(BL_TIMEOUT_PATH).exists()) {
                if (preferences.getBoolean(BLTIMEOUT_SOB, false)) {
                    sb.append("busybox echo ").append(preferences.getInt(PREF_BLTIMEOUT, Integer.parseInt(Helpers.readOneLine(BL_TIMEOUT_PATH)))).append(" > ").append(BL_TIMEOUT_PATH).append(";\n");
                }
            }
            if (new File(BL_TOUCH_ON_PATH).exists()) {
                if (preferences.getBoolean(PREF_BLTOUCH, false)) {
                    sb.append("busybox echo 1 > " + BL_TOUCH_ON_PATH + ";\n");
                }
                else{
                    sb.append("busybox echo 0 > " + BL_TOUCH_ON_PATH + ";\n");
                }
            }
            if (BLN_PATH!=null) {
                if (preferences.getBoolean(PREF_BLN, false)) {
                    sb.append("busybox echo 1 > ").append(BLN_PATH).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(BLN_PATH).append(";\n");
                }
            }
            if (WIFIPM_PATH!=null) {
                if (preferences.getBoolean("pref_wifi_pm", false)) {
                    sb.append("busybox echo 1 > ").append(WIFIPM_PATH).append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(WIFIPM_PATH).append(";\n");
                }
            }
            if (VIBE_PATH!=null) {
                if (preferences.getBoolean("viber_sob", false)) {
                    sb.append("busybox echo ").append(preferences.getInt("pref_viber", Integer.parseInt(vib.get_val(VIBE_PATH)))).append(" > ").append(VIBE_PATH).append(";\n");
                }
            }
            if (new File(PFK_HOME_ENABLED).exists() && new File(PFK_MENUBACK_ENABLED).exists()) {
                if (preferences.getBoolean(PFK_SOB, false)) {
                    sb.append("busybox echo ").append(preferences.getInt(PREF_HOME_ALLOWED_IRQ, Integer.parseInt(Helpers.readOneLine(PFK_HOME_ALLOWED_IRQ)))).append(" > ").append(PFK_HOME_ALLOWED_IRQ).append(";\n");
                    sb.append("busybox echo ").append(preferences.getInt(PREF_HOME_REPORT_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_HOME_REPORT_WAIT)))).append(" > ").append(PFK_HOME_REPORT_WAIT).append(";\n");
                    sb.append("busybox echo ").append(preferences.getInt(PREF_MENUBACK_INTERRUPT_CHECKS, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_INTERRUPT_CHECKS)))).append(" > ").append(PFK_MENUBACK_INTERRUPT_CHECKS).append(";\n");
                    sb.append("busybox echo ").append(preferences.getInt(PREF_MENUBACK_FIRST_ERR_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_FIRST_ERR_WAIT)))).append(" > ").append(PFK_MENUBACK_FIRST_ERR_WAIT).append(";\n");
                    sb.append("busybox echo ").append(preferences.getInt(PREF_MENUBACK_LAST_ERR_WAIT, Integer.parseInt(Helpers.readOneLine(PFK_MENUBACK_LAST_ERR_WAIT)))).append(" > ").append(PFK_MENUBACK_LAST_ERR_WAIT).append(";\n");
                    if (preferences.getBoolean(PFK_HOME_ON, false)) {
                        sb.append("busybox echo 1 > " + PFK_HOME_ENABLED + ";\n");
                    }
                    else{
                        sb.append("busybox echo 0 > " + PFK_HOME_ENABLED + ";\n");
                    }
                    if (preferences.getBoolean(PFK_MENUBACK_ON, false)) {
                        sb.append("busybox echo 1 > " + PFK_MENUBACK_ENABLED + ";\n");
                    }
                    else{
                        sb.append("busybox echo 0 > " + PFK_MENUBACK_ENABLED + ";\n");
                    }
                }
            }
            if (new File("/system/etc/sysctl.conf").exists()) {
                if (preferences.getBoolean(SYSCTL_SOB, false)) {
                    sb.append("busybox sysctl -p;\n");
                }
            }
            if (preferences.getBoolean(VM_SOB, false)) {
                s = preferences.getString(PREF_VM, null);
                if(s != null){
                    String p[]=s.split(";");
                    for (String aP : p) {
                        if(!aP.equals("") && aP!=null){
                            final String pn[]=aP.split(":");
                            sb.append("busybox echo ").append(pn[1]).append(" > ").append(VM_PATH).append(pn[0]).append(";\n");
                        }
                    }
                }
            }
            if (new File(DYNAMIC_DIRTY_WRITEBACK_PATH).exists()) {
                if (preferences.getBoolean(DYNAMIC_DIRTY_WRITEBACK_SOB, false)) {
                    if (preferences.getBoolean(PREF_DYNAMIC_DIRTY_WRITEBACK, false)) {
                        sb.append("busybox echo 1 > " + DYNAMIC_DIRTY_WRITEBACK_PATH + ";\n");
                    }
                    else{
                        sb.append("busybox echo 0 > " + DYNAMIC_DIRTY_WRITEBACK_PATH + ";\n");
                    }
                    sb.append("busybox echo ").append(preferences.getInt(PREF_DIRTY_WRITEBACK_ACTIVE, Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_ACTIVE_PATH)))).append(" > ").append(DIRTY_WRITEBACK_ACTIVE_PATH).append(";\n");
                    sb.append("busybox echo ").append(preferences.getInt(PREF_DIRTY_WRITEBACK_SUSPEND, Integer.parseInt(Helpers.readOneLine(DIRTY_WRITEBACK_SUSPEND_PATH)))).append(" > ").append(DIRTY_WRITEBACK_SUSPEND_PATH).append(";\n");
                }
            }
            //read & save default values
            s=Helpers.readOneLine(MINFREE_PATH);
            preferences.edit().putString(MINFREE_DEFAULT,s).apply();

            if (preferences.getBoolean(PREF_MINFREE_BOOT, false)) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_MINFREE, s)).append(" > ").append(MINFREE_PATH).append(";\n");
            }
            if (new File(USER_PROC_PATH).exists()) {
                    if (preferences.getBoolean(USER_PROC_SOB, false)) {
                        if (preferences.getBoolean(PREF_USER_PROC, false)) {
                            sb.append("busybox echo ").append(preferences.getString(PREF_USER_NAMES, Helpers.readOneLine(USER_PROC_NAMES_PATH))).append(" > ").append(USER_PROC_NAMES_PATH).append(";\n");
                            sb.append("busybox echo 1 > " + USER_PROC_PATH + ";\n");
                        }
                        else{
                            sb.append("busybox echo 0 > " + USER_PROC_PATH + ";\n");
                        }
                    }
            }
            if (new File(SYS_PROC_PATH).exists()) {
                    if (preferences.getBoolean(SYS_PROC_SOB, false)) {
                        if (preferences.getBoolean(PREF_SYS_PROC, false)) {
                            sb.append("busybox echo ").append(preferences.getString(PREF_SYS_NAMES, Helpers.readOneLine(USER_SYS_NAMES_PATH))).append(" > ").append(USER_SYS_NAMES_PATH).append(";\n");
                            sb.append("busybox echo 1 > " + SYS_PROC_PATH + ";\n");
                        }
                        else{
                            sb.append("busybox echo 0 > " + SYS_PROC_PATH + ";\n");
                        }
                    }
            }
            if (new File(ksmpath+"/run").exists()) {
                //---reset------
                sb.append("busybox echo 0 > ").append(ksmpath).append("/run;\n").append("sleep 0.5;\n");
                sb.append("busybox echo 2 > ").append(ksmpath).append("/run;\n").append("sleep 0.5;\n");
                if (preferences.getBoolean(KSM_SOB, false)) {
                    s = preferences.getString("pref_ksm", null);
                    if (s != null) {
                        String p[] = s.split(";");
                        for (String aP : p) {
                            if (!aP.equals("") && aP != null) {
                                final String pn[] = aP.split(":");
                                if(new File(ksmpath+"/"+pn[0]).exists())
                                    sb.append("busybox echo ").append(pn[1]).append(" > ").append(ksmpath).append("/").append(pn[0]).append(";\n");
                            }
                        }
                    }
                }
                if (preferences.getBoolean(PREF_RUN_KSM, false)) {
                    sb.append("busybox echo 1 > ").append(ksmpath).append("/run").append(";\n");
                }
                else{
                    sb.append("busybox echo 0 > ").append(ksmpath).append("/run").append(";\n");
                }
            }
            if (preferences.getBoolean(GOV_SOB, false)) {
                s = preferences.getString(gov.replace(" ","_"), "");
                if (!s.equals("")) {
                    sb.append("if busybox [ -d ").append(GOV_SETTINGS_PATH).append(gov).append(" ]; then\n");
                    String p[]=s.split(";");
                    for (String aP : p) {
                        if(aP!=null && aP.contains(":")){
                            final String pn[]=aP.split(":");
                            sb.append("busybox echo ").append(pn[1]).append(" > ").append(GOV_SETTINGS_PATH).append(gov).append("/").append(pn[0]).append(";\n");
                        }
                    }
                    sb.append("fi;\n");
                }
            }
            if (preferences.getBoolean(IO_SOB, false)) {
                s=preferences.getString(io.replace(" ","_"),"");
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
            }
            if (preferences.getBoolean(TOUCHSCREEN_SOB, false)) {
                if (new File(SLIDE2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_SLIDE2WAKE, Helpers.readOneLine(SLIDE2WAKE))).append(" > ").append(SLIDE2WAKE).append(";\n");
                }
                if (new File(SWIPE2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_SWIPE2WAKE, Helpers.readOneLine(SWIPE2WAKE))).append(" > ").append(SWIPE2WAKE).append(";\n");
                }
                if (new File(HOME2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_HOME2WAKE, Helpers.readOneLine(HOME2WAKE))).append(" > ").append(HOME2WAKE).append(";\n");
                }
                if (new File(LOGO2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_LOGO2WAKE, Helpers.readOneLine(LOGO2WAKE))).append(" > ").append(LOGO2WAKE).append(";\n");
                }
                if (new File(LOGO2MENU).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_LOGO2MENU, Helpers.readOneLine(LOGO2MENU))).append(" > ").append(LOGO2MENU).append(";\n");
                }
                if (new File(DOUBLETAP2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_DOUBLETAP2WAKE, Helpers.readOneLine(DOUBLETAP2WAKE))).append(" > ").append(DOUBLETAP2WAKE).append(";\n");
                }
                if (new File(POCKET_DETECT).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_POCKET_DETECT, Helpers.readOneLine(POCKET_DETECT))).append(" > ").append(POCKET_DETECT).append(";\n");
                }
                if (new File(PICK2WAKE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_PICK2WAKE, Helpers.readOneLine(PICK2WAKE))).append(" > ").append(PICK2WAKE).append(";\n");
                }
                if (new File(FLICK2SLEEP).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_FLICK2SLEEP, Helpers.readOneLine(FLICK2SLEEP))).append(" > ").append(FLICK2SLEEP).append(";\n");
                }
                if (new File(FLICK2SLEEP_SENSITIVE).exists()) {
                    sb.append("busybox echo ").append(preferences.getString(PREF_FLICK2SLEEP_SENSITIVE, "0")).append(" > ").append(FLICK2SLEEP_SENSITIVE).append(";\n");
                }
                if (Helpers.touch2wake_path()!=null) {
                    s=Helpers.touch2wake_path();
                    sb.append("busybox echo ").append(preferences.getString(PREF_TOUCH2WAKE, Helpers.readOneLine(s))).append(" > ").append(s).append(";\n");
                }
            }
            if (preferences.getBoolean(ZRAM_SOB, false)){
                sb.append("zramstop ").append(ncpus).append(";\n");
                if (preferences.getBoolean(ZRAM_ON, false)) {
                    int curdisk = preferences.getInt(PREF_ZRAM,Math.round(maxdisk*18/100));
                    long v = (long)(curdisk*1024*1024);
                    sb.append("zramstart \"").append(ncpus).append("\" \"").append(v).append("\";\n");
                }
            }
            sb.append(preferences.getString(PREF_SH, "# no custom shell command")).append(";\n");
            Helpers.shExec(sb,c,true);
            return "";
        }
        @Override
        protected void onPreExecute() {
            Toast.makeText(c, TAG+ " start boot sequence", Toast.LENGTH_SHORT).show();
        }
    	@Override
    	protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(c, TAG+ " boot complete", Toast.LENGTH_SHORT).show();
            if (FASTCHARGE_PATH!=null) {
                if(Helpers.readOneLine(FASTCHARGE_PATH).equals("1")){
                    // add notification to warn user they can only charge
                    CharSequence contentTitle = c.getText(R.string.fast_charge_notification_title);
                    CharSequence contentText = c.getText(R.string.fast_charge_notification_message);
                    Notification n = new Notification.Builder(c)
                            .setAutoCancel(true)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setSmallIcon(R.drawable.ic_notify)
                            .setWhen(System.currentTimeMillis()).getNotification();
                    NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(1337, n);//1337
                }
            }
            Intent intent = new Intent(INTENT_PP);
            intent.putExtra("from",TAG);
            c.sendBroadcast(intent);

            stopSelf();
        }
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
