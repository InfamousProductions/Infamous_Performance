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

package com.infamous.performance.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import com.infamous.performance.widget.PCWidget;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Helpers implements Constants {

    private static String mVoltagePath;

    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists() && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, " su does not exist!!!");
            return false; // tell caller to bail...
        }
        try {
            if ((new CMDProcessor().su.runWaitFor("ls /data/app-private")).success()) {
            //if ((new CMDProcessor().su.runWaitFor("su -c id")).success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we dont have permission");
                return false;
            }
        }
        catch (final NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public static String readOneLine(String fname) {
        String line = null;
        if (new File(fname).exists()) {
        	BufferedReader br;
	        try {
	            br = new BufferedReader(new FileReader(fname), 512);
	            try {
	                line = br.readLine();
	            }
                finally {
	                br.close();
	            }
	        }
            catch (Exception e) {
	            //Log.e(TAG, "IO Exception when reading sys file", e);
	            // attempt to do magic!
	            return readFileViaShell(fname, true);
	        }
        }
        return line;
    }

    public static String readFileViaShell(String filePath, boolean useSu) {
        CMDProcessor.CommandResult cr = null;
        if (useSu) {
            cr = new CMDProcessor().su.runWaitFor("cat " + filePath);
        } else {
            cr = new CMDProcessor().sh.runWaitFor("cat " + filePath);
        }
        if (cr.success())
            return cr.stdout;
        return null;
    }

    public static String[] getAvailableIOSchedulers(String p) {
        String[] schedulers = null;
        String[] aux = readStringArray(p);
        if (aux != null) {
            schedulers = new String[aux.length];
            for (byte i = 0; i < aux.length; i++) {
                if (aux[i].charAt(0) == '[') {
                    schedulers[i] = aux[i].substring(1, aux[i].length() - 1);
                } else {
                    schedulers[i] = aux[i];
                }
            }
        }
        return schedulers;
    }

    private static String[] readStringArray(String fname) {
        String line = readOneLine(fname);
        if (line != null) {
            return line.split(" ");
        }
        return null;
    }

    public static String getIOScheduler(String p) {
        String scheduler = null;
        String[] schedulers = readStringArray(p);
        if (schedulers != null) {
            for (String s : schedulers) {
                if (s.charAt(0) == '[') {
                    scheduler = s.substring(1, s.length() - 1);
                    break;
                }
            }
        }
        return scheduler;
    }

    public static Boolean GovernorExist(String gov) {
        return readOneLine(GOVERNORS_LIST_PATH).contains(gov);
    }

    public static int getNumOfCpus() {
        int numOfCpu = 1;
        String numOfCpus = Helpers.readOneLine(NUM_OF_CPUS_PATH);
        String[] cpuCount = numOfCpus.split("-");
        if (cpuCount.length > 1) {
            try {
                int cpuStart = Integer.parseInt(cpuCount[0]);
                int cpuEnd = Integer.parseInt(cpuCount[1]);
                numOfCpu = cpuEnd - cpuStart + 1;
                if (numOfCpu < 0) numOfCpu = 1;
            }
            catch (NumberFormatException ex) {
                numOfCpu = 1;
            }
        }
        return numOfCpu;
    }

    public static boolean voltageFileExists() {
        if (new File(VDD_PATH).exists()) {
            setVoltagePath(VDD_PATH);
            return true;
        }
        else if (new File(COMMON_VDD_PATH).exists()) {
            setVoltagePath(COMMON_VDD_PATH);
            return true;
        }
        else if (new File(UV_MV_PATH).exists()) {
            setVoltagePath(UV_MV_PATH);
            return true;
        }
        else if (new File(VDD_TABLE).exists()) {
            setVoltagePath(VDD_TABLE);
            return true;
        }
        /*else if (new File(VDD_SYSFS_PATH).exists()) {
            setVoltagePath(VDD_SYSFS_PATH);
            return true;
        }
*/
        return false;
    }
    public static void setVoltagePath(String voltageFile) {
        Log.d(TAG, "Voltage table path detected: "+voltageFile);
        mVoltagePath = voltageFile;
    }
    public static String getVoltagePath() {
        return mVoltagePath;
    }

    public static String toMHz(String mhzString) {
        if((mhzString==null)||(mhzString.length()<=0)) return "";
        else return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }

    public static void restartPC(final Activity activity) {
        if (activity == null) return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    public static void updateAppWidget(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, PCWidget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        Intent update = new Intent();
        update.setAction("com.infamous.performance.ACTION_FREQS_CHANGED");
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(update);
    }

    public static Bitmap getBackground(int bgcolor) {
        try {
            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = Bitmap.createBitmap(2, 2, config);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(bgcolor);
            return bitmap;
        }
        catch (Exception e) {
            return null;
        }
    }
    public static String binExist(String b) {
        CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox which " + b);
        if (cr.success() && cr.stdout != null && cr.stdout.contains(b)) {
            if (new File(cr.stdout).isFile()) {
                Log.d(TAG, b + " detected on: " + cr.stdout);
                return cr.stdout;
            }
        }
        Log.d(TAG, b + " detected on: " + cr.stdout);
        return null;
    }

    public static Boolean moduleActive(String b) {
        CMDProcessor.CommandResult cr;
        cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox ps | busybox grep "+b+" | busybox grep -v \"busybox grep "+b+"\" | busybox awk '{print $1}'`");
        Log.d(TAG, "Module: "+cr.stdout);
        return (cr.success() && cr.stdout!=null && cr.stdout.length()>0);
    }

    public static long getMem(String tip) {
        long v=0;
        CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep "+tip+" /proc/meminfo | busybox grep -E -o '[[:digit:]]+'`");
        if(cr.success() && cr.stdout!=null && cr.stdout.length()>0){
            try{
               v = (long) Integer.parseInt(cr.stdout);//kb
            }
            catch (NumberFormatException e) {
                Log.d(TAG, tip+" conversion err: "+e);
            }
        }
        return v;
    }

    public static long getSwap() {
        long v=0;
        for (int i = 0; i < getNumOfCpus(); i++) {
            CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep zram"+i+" /proc/swaps`");
            if(cr.success() && cr.stdout!=null && cr.stdout.contains("zram"+i)){
                try{
                    v = v+ (long) Integer.parseInt(cr.stdout.split(" ")[2]);//kb
                }
                catch (NumberFormatException e) {
                    Log.d(TAG, " swap conversion err: "+e);
                }
            }
        }
        return v;
    }

    public static boolean isZRAM() {
        CMDProcessor.CommandResult cr =new CMDProcessor().sh.runWaitFor(ISZRAM);
        if((cr.success() && cr.stdout!=null && cr.stdout.length()>0)||(new File("/dev/block/zram0/").exists())||(new File("/sys/block/zram0/").exists())) return true;
        return false;
    }
    public static void get_assetsScript(String fn,Context c,String prefix,String postfix){
        byte[] buffer;
        final AssetManager assetManager = c.getAssets();
        try {
            InputStream f =assetManager.open(fn);
            buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            final String s = new String(buffer);
            final StringBuilder sb = new StringBuilder(s);
            if(!postfix.equals("")){ sb.append("\n\n").append(postfix); }
            if(!prefix.equals("")){ sb.insert(0,prefix+"\n"); }
            sb.insert(0,"#!"+Helpers.binExist("sh")+"\n\n");
            try {
                FileOutputStream fos;
                fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
                fos.write(sb.toString().getBytes());
                fos.close();

            } catch (IOException e) {
                Log.d(TAG, "error write "+fn+" file");
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            Log.d(TAG, "error read "+fn+" file");
            e.printStackTrace();
        }
    }
    public synchronized static void get_assetsBinary(String fn,Context c){
        byte[] buffer;
        final AssetManager assetManager = c.getAssets();
        try {
            InputStream f =assetManager.open(fn);
            buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            try {
                FileOutputStream fos;
                fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
                fos.write(buffer);
                fos.close();
            }
            catch (IOException e) {
                Log.d(TAG, "error write "+fn+" file");
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            Log.d(TAG, "error read "+fn+" file");
            e.printStackTrace();
        }
    }
    public static String shExec(StringBuilder s,Context c,Boolean su){
        //final String dn=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+TAG+"/logs";
        //new CMDProcessor().sh.runWaitFor("busybox mkdir -p "+dn );
        get_assetsScript("run", c, "", s.toString());
        new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+ c.getFilesDir()+"/run" );
        CMDProcessor.CommandResult cr=null;
        if(su){
            //cr=new CMDProcessor().su.runWaitFor(c.getFilesDir()+"/run > " + dn + "/run.log 2>&1");
            cr=new CMDProcessor().su.runWaitFor(c.getFilesDir()+"/run");
        }
        else{
            //cr=new CMDProcessor().sh.runWaitFor(c.getFilesDir()+"/run > " + dn + "/run.log 2>&1");
            cr=new CMDProcessor().sh.runWaitFor(c.getFilesDir()+"/run");
        }
        if(cr.success()){
            return cr.stdout;
        }
        else{
            Log.d(TAG, "execute run error: "+cr.stderr);
            return "nok";
        }
    }

    public static String readCPU(Context context,int i){
        Helpers.get_assetsScript("utils", context, "", "");
        new CMDProcessor().sh.runWaitFor("busybox chmod 750 "+context.getFilesDir()+"/utils" );
        CMDProcessor.CommandResult cr=new CMDProcessor().su.runWaitFor(context.getFilesDir()+"/utils -getcpu "+i);
        if(cr.success()) return cr.stdout;
        else return null;
    }
      
    public static String ReadableByteCount(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = String.valueOf("KMGTPE".charAt(exp-1));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static void getTabList(String strTitle, final ViewPager vp,Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(strTitle);

        List<String> listItems = new ArrayList<String>();
        for(byte i=0;i< vp.getAdapter().getCount();i++){
                listItems.add(vp.getAdapter().getPageTitle(i).toString());
        }
        alertDialogBuilder.setItems(listItems.toArray(new CharSequence[listItems.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vp.setCurrentItem(which);
                    }
                }
        ).show();
    }

    public static boolean is_Tab_available(int i){
        boolean flag=false;
        switch (i){
            case 1:flag=Helpers.getNumOfCpus()>=1;break;
            //case 2:flag=Helpers.showBattery();break;
            case 4:flag=Helpers.voltageFileExists();break;
            default: flag=true;break;
        }
        return flag;
    }

    public static String bln_path() {
        if (new File("/sys/class/misc/backlightnotification/enabled").exists()) {
            return "/sys/class/misc/backlightnotification/enabled";
        }
        else if (new File("/sys/class/leds/button-backlight/blink_buttons").exists()) {
            return "/sys/class/leds/button-backlight/blink_buttons";
        }
        else{
            return null;
        }
    }
    public static String fastcharge_path() {
        if (new File("/sys/kernel/fast_charge/force_fast_charge").exists()) {
            return "/sys/kernel/fast_charge/force_fast_charge";
        }
        else if (new File("/sys/module/msm_otg/parameters/fast_charge").exists()) {
            return "/sys/module/msm_otg/parameters/fast_charge";
        }
        else if (new File("/sys/devices/platform/htc_battery/fast_charge").exists()) {
            return "/sys/devices/platform/htc_battery/fast_charge";
        }
        else{
            return null;
        }
    }
    public static String fsync_path() {
        if (new File("/sys/class/misc/fsynccontrol/fsync_enabled").exists()) {
            return "/sys/class/misc/fsynccontrol/fsync_enabled";
        }
        else if (new File("/sys/module/sync/parameters/fsync_enabled").exists()) {
            return "/sys/module/sync/parameters/fsync_enabled";
        }
        else{
            return null;
        }
    }
    public static String touch2wake_path() {
        if (new File("/sys/module/lge_touch_core/parameters/doubletap_to_wake").exists()) {
            return "/sys/module/lge_touch_core/parameters/doubletap_to_wake";
        }
        else if (new File("/sys/module/lge_touch_core/parameters/touch_to_wake").exists()) {
            return "/sys/module/lge_touch_core/parameters/touch_to_wake";
        }
        else{
            return null;
        }
    }
    public static String wifipm_path() {
        if (new File("/sys/module/bcmdhd/parameters/wifi_pm").exists()) {
            return "/sys/module/bcmdhd/parameters/wifi_pm";
        }
        else if (new File("/sys/module/bcmdhd/parameters/wifi_fast").exists()) {
            return "/sys/module/bcmdhd/parameters/wifi_fast";
        }
        else if (new File("/sys/module/dhd/parameters/wifi_pm").exists()) {
            return "/sys/module/dhd/parameters/wifi_pm";
        }
        else{
            return null;
        }
    }
    public static String hotplug_path() {
        if (new File("/sys/devices/virtual/misc/mako_hotplug_control").exists()) {
            return "/sys/devices/virtual/misc/mako_hotplug_control";
        }
        else if (new File("/sys/class/misc/mako_hotplug_control").exists()) {
            return "/sys/class/misc/mako_hotplug_control";
        }
        else if (new File("/sys/module/auto_hotplug/parameters").exists()) {
            return "/sys/module/auto_hotplug/parameters";
        }
        else if (new File("/sys/module/dyn_hotplug/parameters").exists()) {
            return "/sys/module/dyn_hotplug/parameters";
        }
        else if (new File("/sys/class/misc/tegra_hotplug_control").exists()) {
            return "/sys/class/misc/tegra_hotplug_control";
        }
        else{
            return null;
        }
    }

    public static String extSD(){
        String externalsd="";

        if(!TextUtils.isEmpty(System.getenv("SECONDARY_STORAGE"))){
            final String externalstorage[]=System.getenv("SECONDARY_STORAGE").split(":");
            for ( final String dirs : externalstorage ) {
                final File dir= new File(dirs);
                if ( dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite() ) {
                    externalsd=dirs;
                    break;
                }
            }
        }
        else{
            final String supported[]={"/mnt/extSdCard","/storage/sdcard1","/mnt/external_sd"};
            for ( final String dirs : supported) {
                final File dir= new File(dirs);
                if ( dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite() ) {
                    externalsd=dirs;
                    break;
                }
            }
        }
        return externalsd;
    }



}

