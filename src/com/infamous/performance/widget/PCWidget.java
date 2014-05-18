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

package com.infamous.performance.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import com.infamous.performance.R;
import com.infamous.performance.activities.MainActivity;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;


public class PCWidget extends AppWidgetProvider implements Constants {
    SharedPreferences mPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        final ComponentName nm = new ComponentName(context, PCWidget.class);
        final String action = intent.getAction();
        if (action!=null && action.equals("com.infamous.performance.ACTION_FREQS_CHANGED")) {
            onUpdate(context, awm, awm.getAppWidgetIds(nm));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        int i=0;
        int nCpus=Helpers.getNumOfCpus();

        for (int awi : appWidgetIds) {
            if((MainActivity.mMinFreqSetting[i]==null) || (MainActivity.mMaxFreqSetting[i]==null) || (MainActivity.mCurGovernor[i]==null) || (MainActivity.mCurIO[i]==null)){
                final String r=Helpers.readCPU(context,nCpus);
                if(r!=null)
                    onUpdateWidget(context, appWidgetManager, awi, Helpers.toMHz(r.split(":")[i*5+1]), Helpers.toMHz(r.split(":")[i*5]), r.split(":")[i*5+2], r.split(":")[i*5+3],(i+1));
                else
                    onUpdateWidget(context, appWidgetManager, awi, Helpers.toMHz("0"), Helpers.toMHz("0"), "-", "-",(i+1));
            }
            else{
                onUpdateWidget(context, appWidgetManager, awi, Helpers.toMHz(MainActivity.mMaxFreqSetting[i]), Helpers.toMHz(MainActivity.mMinFreqSetting[i]), MainActivity.mCurGovernor[i], MainActivity.mCurIO[i], (i+1));
            }
            if(++i==nCpus) i=0;
        }
    }

    public void onUpdateWidget(Context context,AppWidgetManager appWidgetManager, int appWidgetId, String max,String min, String gov, String io,int curcpu) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget);

        int bgColor = mPreferences.getInt(PREF_WIDGET_BG_COLOR, 0xff000000);
        int textColor = mPreferences.getInt(PREF_WIDGET_TEXT_COLOR, 0xff808080);
        views.setImageViewBitmap(R.id.widget_bg, Helpers.getBackground(bgColor));
        views.setTextViewText(R.id.curcpu, "CPU "+curcpu);
        views.setTextViewText(R.id.max, max);
        views.setTextViewText(R.id.min, min);
        views.setTextViewText(R.id.gov, gov);
        views.setTextViewText(R.id.io, io);
        views.setTextColor(R.id.curcpu, textColor);
        views.setTextColor(R.id.max, textColor);
        views.setTextColor(R.id.min, textColor);
        views.setTextColor(R.id.io, textColor);
        views.setTextColor(R.id.gov, textColor);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("cpu",curcpu-1);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_bg, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
