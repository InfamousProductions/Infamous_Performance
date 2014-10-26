package com.infamous.performance.util;

/**
 * Created by h0rn3t on 17.07.2013.
 */
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infamous.performance.R;

import java.util.ArrayList;

public class PackAdapter extends ArrayAdapter<PackItem> {

    private final ArrayList<PackItem> list;
    private final Activity context;

    public PackAdapter(Activity context, ArrayList<PackItem> list) {
        super(context, R.layout.pack_item, list);
        this.context = context;
        this.list = list;
    }
    public void delItem(int p){
        list.remove(p);
        notifyDataSetChanged();
    }
    public ArrayList<PackItem> getList(){
        return list;
    }
    static class ViewHolder {
        public TextView app,pack;
        public ImageView image;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.pack_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.app = (TextView) rowView.findViewById(R.id.packname);
            viewHolder.pack = (TextView) rowView.findViewById(R.id.packraw);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        final String npack=list.get(position).getPackName();
        holder.pack.setText(npack);
        holder.app.setText(list.get(position).getAppName());
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(npack, 0);
            holder.image.setImageDrawable(context.getPackageManager().getApplicationIcon(packageInfo.applicationInfo));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return rowView;
    }


}
