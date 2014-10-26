package com.infamous.performance.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

/**
 * Created by h0rn3t on 09.02.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class checkSU extends Activity implements Constants, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    private ProgressBar wait;
    private TextView info;
    private ImageView attn;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.check_su);
        wait=(ProgressBar) findViewById(R.id.wait);
        info=(TextView) findViewById(R.id.info);
        attn=(ImageView) findViewById(R.id.attn);

        if(mPreferences.getBoolean("booting",false)) {
            info.setText(getString(R.string.boot_wait));
            wait.setVisibility(View.GONE);
            attn.setVisibility(View.VISIBLE);
        }
        else {

            new TestSU().execute();
        }
    }

    private class TestSU extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SystemClock.sleep(1000);
            final Boolean canSu = Helpers.checkSu();
            final Boolean canBb = Helpers.binExist("busybox")!=null;
            if (canSu && canBb) return "ok";
            else return "nok";
        }
        @Override
        protected void onPostExecute(String result) {

            if(result.equals("nok")){
                //mPreferences.edit().putBoolean("firstrun", true).commit();
                info.setText(getString(R.string.su_failed_su_or_busybox));
                wait.setVisibility(View.GONE);
                attn.setVisibility(View.VISIBLE);
            }
            else{
                //mPreferences.edit().putBoolean("firstrun", false).commit();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("r",result);
                setResult(RESULT_OK,returnIntent);
                finish();
            }

        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = is_light_theme;
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

}
