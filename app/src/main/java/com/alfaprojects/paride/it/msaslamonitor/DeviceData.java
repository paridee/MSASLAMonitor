package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by paride on 26/08/15.
 * describes all the performance data of a device and holds an application context
 */
public class DeviceData {
    public double RTT=-1;
    public double bandwidthDL=-1;
    public double bandwidthUL=-1;
    public long   mobilespeedparameter=0;
    public long   cloudspeedparameter=8;
    public boolean wifi=false;
    public double wifipower     =   0;
    public Object radiopower    =   null;
    public Object cpupower      =   null;
    public Object cpuspeeds     =   null;
    public double maxCpuPower   =   0;
    public double wifiidle      =   0;
    public Object cellularidle  =   0;
    public double cellularmaxidle  =   0;
    public double cellularmaxpower  =   0;
    public double scalingfactor =   8;
    public double wifilevel;
    public double batterylevel  =   0;
    public double highbatterythreshold  =   30;
    public double highwifithreshold     =   -120;
    public Context appContext;

    public DeviceData clone(){
        DeviceData returnvalue          =   new DeviceData();
        returnvalue.RTT                 =   RTT;
        returnvalue.bandwidthDL         =   bandwidthDL;
        returnvalue.bandwidthUL         =   bandwidthUL;
        returnvalue.mobilespeedparameter=   mobilespeedparameter;
        returnvalue.cloudspeedparameter =   cloudspeedparameter;
        returnvalue.wifi                =   wifi;
        returnvalue.wifipower           =   wifipower;
        returnvalue.radiopower          =   radiopower;
        returnvalue.cpupower            =   cpupower;
        returnvalue.cpuspeeds           =   cpuspeeds;
        returnvalue.maxCpuPower         =   maxCpuPower;
        returnvalue.wifiidle            =   wifiidle;
        returnvalue.cellularidle        =   cellularidle;
        returnvalue.cellularmaxidle     =   cellularmaxidle;
        returnvalue.cellularmaxpower    =   cellularmaxpower;
        returnvalue.scalingfactor       =   scalingfactor;
        returnvalue.wifilevel           =   wifilevel;
        returnvalue.batterylevel        =   batterylevel;
        returnvalue.highbatterythreshold=   highbatterythreshold;
        returnvalue.highwifithreshold   =   highwifithreshold;
        returnvalue.appContext          =   appContext;
        return returnvalue;
    }
    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    public void updateBatteryAndSignalState(){
        WifiManager myManager   =   (WifiManager)this.appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myInfo =   myManager.getConnectionInfo();
        int RSSI    =   -127; //no signal
        if(myInfo!=null){
            try{
                RSSI        =   myInfo.getRssi();
                System.out.println("RSSI: "+RSSI);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            System.out.println("INFO NOT AVAILABLE");
        }
        IntentFilter myBatteryFilter   =   new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent bStatus  =   this.appContext.registerReceiver(null, myBatteryFilter);
        int level       =   bStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status      =   bStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        double percentage   =   (((double)level)/((double)status));
        String date =   new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        System.out.println(date + ":LIVELLO RSSI " + RSSI + " battery " + percentage);
        //NON FA ANALISI SULLA DISPONIBILITA' DI RETE CELLULARE, IN QUESTO CONTESTO NON CI INTERESSA
        //NON E' NECESSARIA PER CALCOLARE IL RISPARMIO SULLA COMPUTAZIONE IN LOCALE
    }
}
