package com.infamous.performance.util;

import android.util.Log;
import java.io.File;

/**
 * Created by h0rn3t on 05.01.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class VibratorClass implements Constants {
    private int max=0;
    private int min=0;
    private String path=null;

    public int get_min(){
        return min;
    }
    public int get_max(){
        return max;
    }
    public String get_val(String p){
        return getOnlyNumerics(Helpers.readOneLine(p));
    }
    public String get_path(){
        if (new File("/sys/class/vibetonz/immDuty/pwmvalue_intensity").exists()) {
            this.min=0;
            this.max=127;
            this.path= "/sys/class/vibetonz/immDuty/pwmvalue_intensity";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/vibrator/pwmvalue").exists()) {
            this.min=0;
            this.max=127;
            this.path= "/sys/vibrator/pwmvalue";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/class/misc/vibratorcontrol/vibrator_strength").exists()) {
            this.min=1000;
            this.max=1600;
            this.path= "/sys/class/misc/vibratorcontrol/vibrator_strength";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/vibe/pwmduty").exists()) {
            this.min=1000;
            this.max=1450;
            this.path= "/sys/vibe/pwmduty";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/class/timed_output/vibrator/amp").exists()) {
            this.min=0;
            this.max=100;
            this.path= "/sys/class/timed_output/vibrator/amp";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/class/misc/pwm_duty/pwm_duty").exists()) {
            this.min=0;
            this.max=100;
            this.path="/sys/class/misc/pwm_duty/pwm_duty";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else if (new File("/sys/devices/virtual/timed_output/vibrator/voltage_level").exists()) {
            this.min=1200;
            this.max=3100;
            this.path="/sys/devices/virtual/timed_output/vibrator/voltage_level";
            Log.d(TAG, "vibe path detected: "+this.path);
        }
        else{
            this.path=null;
            Log.d(TAG, "vibe path not detected");
        }
        return this.path;
    }
    private String getOnlyNumerics(String str) {
        if (str == null) {
            Log.e(TAG, "vibe value read error");
            return "0";
        }
        StringBuffer strBuff = new StringBuffer();
        char c;
        for (int i = 0; i < str.length() ; i++) {
            c = str.charAt(i);
            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }


}
