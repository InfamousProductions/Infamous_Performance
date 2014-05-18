package com.infamous.performance.activities;

/**
 * Created by h0rn3t on 22.10.2013.
 */
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.infamous.performance.R;
import com.infamous.performance.util.ActivityThemeChangeInterface;
import com.infamous.performance.util.Constants;
import com.infamous.performance.util.Helpers;

import java.util.ArrayList;

public class HideTabs extends Activity implements Constants, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    SharedPreferences mPreferences;
    private final Context context=this;
    MyCustomAdapter dataAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.hide_tabs);
        ListView listView = (ListView) findViewById(R.id.applist);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Tab t = (Tab) parent.getItemAtPosition(position);
            }
        });
        ArrayList<Tab> TabList = new ArrayList<Tab>();
        int i=0;
        while (i<getResources().getStringArray(R.array.tabs).length) {
            Tab t = new Tab(getResources().getStringArray(R.array.tabs)[i],mPreferences.getBoolean(getResources().getStringArray(R.array.tabs)[i],true));
            if(Helpers.is_Tab_available(i)) TabList.add(t);
            i++;
        }
        dataAdapter = new MyCustomAdapter(this,R.layout.tab_item, TabList);
        listView.setAdapter(dataAdapter);

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

    @Override
    public void onPause() {
        super.onPause();
        boolean flag=false;
        for(int i=0;i<dataAdapter.getCount();i++){
            Tab t = dataAdapter.getItem(i);
            if(t.isSelected()){
                flag=true;
                break;
            }
        }
        if(!flag){
            Tab t = dataAdapter.getItem(0);
            mPreferences.edit().putBoolean(t.getName(),true).apply();
        }
    }
    private class MyCustomAdapter extends ArrayAdapter<Tab> {
        private ArrayList<Tab> TabList;
        public MyCustomAdapter(Context context, int textViewResourceId,ArrayList<Tab> TabList) {
            super(context, textViewResourceId, TabList);
            this.TabList = new ArrayList<Tab>();
            this.TabList.addAll(TabList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.tab_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Tab t = (Tab) cb.getTag();
                        if(cb.isChecked()){
                            mPreferences.edit().remove(cb.getText().toString()).apply();
                        }
                        else{
                            mPreferences.edit().putBoolean(cb.getText().toString(),cb.isChecked()).apply();
                        }
                        t.setSelected(cb.isChecked());
                        MainActivity.thide=true;
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Tab t = TabList.get(position);
            holder.name.setText(t.getName());
            holder.name.setChecked(t.isSelected());
            holder.name.setTag(t);

            return convertView;

        }

    }
    public class Tab {
        String name = null;
        boolean selected = false;

        public Tab(String name, boolean selected) {
            super();
            this.name = name;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public boolean isSelected() {
            return selected;
        }
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }
}
