package com.espthermostat.base.application;

import com.espthermostat.base.net.wifi.WifiAdmin;
import com.espthermostat.base.threadpool.CachedThreadPool;
import com.espthermostat.base.time.EspTimeManager;
import com.espthermostat.util.EspStrings;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

public class EspApplication extends Application
{
    private static EspApplication instance;
    
    public static EspApplication sharedInstance()
    {
        if (instance == null)
        {
            throw new NullPointerException(
                "EspApplication instance is null, please register in AndroidManifest.xml first");
        }
        return instance;
    }
    
    public static boolean GOOGLE_PALY_VERSION = true;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        initAsyn();
        initSyn();
    }
    
    public Context getContext()
    {
        return getBaseContext();
    }
    
    public Resources getResources()
    {
        return getBaseContext().getResources();
    }
    
    public String getVersionName()
    {
        String version = "";
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
            version = "Not found version";
        }
        return version;
    }
    
    public int getVersionCode()
    {
        int code = 0;
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            code = pi.versionCode;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return code;
    }
    
    public String getEspRootSDPath()
    {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            path = Environment.getExternalStorageDirectory().toString() + "/Espressif/";
        }
        return path;
    }
    
    private String __formatString(int value)
    {
        String strValue = "";
        byte[] ary = __intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--)
        {
            strValue += (ary[i] & 0xFF);
            if (i > 0)
            {
                strValue += ".";
            }
        }
        return strValue;
    }
    
    private byte[] __intToByteArray(int value)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte)((value >>> offset) & 0xFF);
        }
        return b;
    }
    
    public String getGateway()
    {
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wm.getDhcpInfo();
        return __formatString(d.gateway);
    }
    
    private void initSyn()
    {
        // init db
        // data and data directory using seperate session for they maybe take long time
    }
    
    private void initAsyn()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                CachedThreadPool.getInstance();
                WifiAdmin.getInstance();
                EspTimeManager.getInstance().getUTCTimeLong();
            }
        }.start();
    }
}
