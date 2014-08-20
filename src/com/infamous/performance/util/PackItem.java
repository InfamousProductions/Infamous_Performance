package com.infamous.performance.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.infamous.performance.activities.MainActivity;

/**
 * Created by h0rn3t on 29.05.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class PackItem {
    private String packname,appname;

    public PackItem(String p) {
        try {
            PackageInfo packageInfo = MainActivity.c.getPackageManager().getPackageInfo(p, 0);
            this.appname = MainActivity.c.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString();
        }
        catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            this.appname = "";
        }
        this.packname = p;
    }

    public String getPackName() {
        return packname;
    }
    public String getAppName() {
        return appname;
    }

}
