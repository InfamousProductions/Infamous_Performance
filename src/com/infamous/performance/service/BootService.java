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

import com.infamous.performance.R;
import com.infamous.performance.util.BootClass;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;


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
        SharedPreferences preferences;

        public BootWorker(Context c) {
            this.c = c;
            this.preferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        @Override
        protected String doInBackground(Void... args) {
            if(!preferences.getBoolean("boot_mode",false)){
                //bootservice
                preferences.edit().putString(MINFREE_DEFAULT,Helpers.readOneLine(MINFREE_PATH)).commit();
                Helpers.shExec(new BootClass(c,preferences).getScript(),c,true);
            }
            else{
                //initd script
                if(preferences.getBoolean(PREF_MINFREE_BOOT,false)) {
                    preferences.edit().putString(MINFREE_DEFAULT,Helpers.readOneLine(MINFREE_PATH)).commit();
                }
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            if(preferences.getBoolean("boot_mode",false)) return;
            final NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification n = new Notification.Builder(c)
                    .setContentTitle(c.getText(R.string.app_name))
                    .setContentText("apply boot sequence")
                    .setTicker("apply boot sequence")
                    .setSmallIcon(R.drawable.ic_notify)
                    .setWhen(System.currentTimeMillis()).getNotification();
            nm.notify(1338, n);
            //Toast.makeText(c, TAG+ " start boot sequence", Toast.LENGTH_SHORT).show();
        }
    	@Override
    	protected void onPostExecute(String result) {
            super.onPostExecute(result);
            final String FASTCHARGE_PATH=Helpers.fastcharge_path();
            final NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancelAll();
            //Toast.makeText(c, TAG+ " boot complete", Toast.LENGTH_SHORT).show();
            if (FASTCHARGE_PATH!=null) {
                if(Helpers.readOneLine(FASTCHARGE_PATH).equals("1")){
                    Notification n = new Notification.Builder(c)
                            .setContentTitle(c.getText(R.string.app_name))
                            .setContentText(c.getText(R.string.fast_charge_notification_title))
                            .setTicker(c.getText(R.string.fast_charge_notification_title))
                            .setSmallIcon(R.drawable.ic_fastcharge)
                            .setWhen(System.currentTimeMillis()).getNotification();
                    //n.flags = Notification.FLAG_NO_CLEAR;
                    nm.notify(1337, n);
                }
            }
            preferences.edit().putBoolean("booting",false).commit();
            stopSelf();
        }
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
