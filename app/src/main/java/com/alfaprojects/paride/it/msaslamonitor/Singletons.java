package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by paride on 01/07/15.
 */
public class Singletons {
    //public static double RTT=-1;
    //public static double bandwidthDL=-1;
    //public static double bandwidthUL=-1;
    //public static long   mobilespeedparameter=0;
    //public static long   cloudspeedparameter=8;
    //public static boolean wifi=false;
    public static MainDashboard dashboard=null;
    //public static double wifipower     =   0;
    //public static Object radiopower    =   null;
    //public static Object cpupower      =   null;
    //public static Object cpuspeeds     =   null;
    //public static double maxCpuPower   =   0;
    //public static double wifiidle      =   0;
    //public static Object cellularidle  =   0;
    //public static double cellularmaxidle  =   0;
    //public static double cellularmaxpower  =   0;
    public static Context anApplicationContext;
    public static String serverip   =   "10.200.93.235";

    public static void updateGraph(DeviceData data) {
        if(data.RTT!=-1&&data.bandwidthUL!=-1)
            dashboard.updateGraph(data.wifi,data.bandwidthUL,data.RTT,data);
        else{
            System.out.println("missing some values");
        }
    }

    public static double calculatespeedparameter(DeviceData device){   //parameter used as reference for scaling factor, run on both sides
        //TODO needed to be extended with a task parameter and a reference test
        long returnvalue=0;
        for(int j=1;j<50;j++){
            int k=100000*j;
            double [] pippo = new double[k];
            for(int i =0;i<k;i++)
            {
                pippo[i]=3;

            }
            //System.out.println("qui");
            HashMap<String,double[]> testhash	=	new HashMap<String,double[]>();
            testhash.put("A", pippo);
            //System.out.println("qui2");
            String media="(sumA/cardA)";
            long val=System.currentTimeMillis();
            double res	=	ExpressionSolver.getResult(media,testhash);
            long end=System.currentTimeMillis();
            System.out.println(j+" "+(end-val));
            returnvalue =   end-val;
            //System.out.println("vediamo se funzioni"+res);
        }
        device.mobilespeedparameter =   returnvalue;
        System.out.println("Singletons.java SET MOBILE SPEED PARAMETER "+device.mobilespeedparameter);
        return returnvalue;
    }

    public static double getScalingFactor(DeviceData device){
        double value    =   (double)device.mobilespeedparameter/(double)device.cloudspeedparameter;
        return value;
    }

    public static void setPowerProfile(DeviceData deviceData){
        try {
            Class<?> powerProfileClazz = Class.forName("com.android.internal.os.PowerProfile");

//get constructor that takes a context object
            Class[] argTypes = { Context.class };
            Constructor constructor = powerProfileClazz
                    .getDeclaredConstructor(argTypes);
            Object[] arguments = { anApplicationContext };
//Instantiate
            Object powerProInstance = constructor.newInstance(arguments);

//define method
            Method batteryCap = powerProfileClazz.getMethod("getBatteryCapacity", null);

//call method
            Log.d("TEST", batteryCap.invoke(powerProInstance, null).toString());
            for (Field field : powerProfileClazz.getDeclaredFields()) {
                field.setAccessible(true);
                System.out.println("FIELD " + field.getName() + " " + field.get(batteryCap).toString());
                if(field.getName().equals("sPowerMap")){
                    HashMap<String,Object> values =   (HashMap<String,Object> )field.get(batteryCap);
                    System.out.println("VALUES "+values.toString());
                    Set<String> keys    =   values.keySet();
                    Iterator<String> iterator   =   keys.iterator();
                    while(iterator.hasNext()){
                        String key  =   iterator.next();
                        if(key.equals("wifi.active")||key.equals("radio.active")||key.equals("wifi.on")||key.equals("radio.on")||key.equals("cpu.active")||key.equals("cpu.speeds")){
                            Object test =   values.get(key);
                            System.out.println(key+" "+test.toString());
                            if(key.equals("wifi.on")){
                                deviceData.wifiidle   =   (double)test;
                            }
                            else if(key.equals("radio.on")){
                                if(test.getClass().equals(Double[].class)){
                                    Double[] res    = (Double[]) test;
                                    for(int i=0;i<res.length;i++){
                                        System.out.println("valore "+res[i]);
                                        if(res[i]>deviceData.cellularmaxidle){
                                            deviceData.cellularmaxidle =   res[i];
                                        }
                                    }
                                }
                                else if(test.getClass().equals(Double.class)){
                                    Double res  =   (Double)test;
                                    System.out.println("SINGOLO VALORE " + res);
                                    deviceData.cellularmaxidle =   res;
                                }
                                deviceData.cellularidle =   test;
                            }
                            else if(key.equals("wifi.active")){
                                deviceData.wifipower  =   (double)test;
                            }
                            else if(key.equals("radio.active")){
                                if(test.getClass().equals(Double[].class)){
                                    Double[] res    = (Double[]) test;
                                    for(int i=0;i<res.length;i++){
                                        System.out.println("valore "+res[i]);
                                        if(res[i]>deviceData.cellularmaxpower){
                                            deviceData.cellularmaxpower    =   res[i];
                                        }
                                    }
                                }
                                else if(test.getClass().equals(Double.class)){
                                    Double res  =   (Double)test;
                                    System.out.println("SINGOLO VALORE " + res);
                                    deviceData.cellularmaxpower    =   res;
                                }
                                deviceData.radiopower =   test;
                            }
                            else if(key.equals("cpu.active")){
                                if(test.getClass().equals(Double[].class)){
                                    Double[] res    = (Double[]) test;
                                    for(int i=0;i<res.length;i++){
                                        System.out.println("valore "+res[i]);
                                        if(res[i]>deviceData.maxCpuPower){
                                            deviceData.maxCpuPower    =   res[i];
                                        }
                                    }
                                }
                                else if(test.getClass().equals(Double.class)){
                                    Double res  =   (Double)test;
                                    System.out.println("SINGOLO VALORE " + res);
                                    deviceData.maxCpuPower    =   res;
                                }
                                deviceData.cpupower =   test;
                            }
                            else if(key.equals("cpu.speeds")){
                                if(test.getClass().equals(Double[].class)){
                                    Double[] res    = (Double[]) test;
                                    for(int i=0;i<res.length;i++){
                                        System.out.println("valore "+res[i]);
                                    }
                                }
                                else if(test.getClass().equals(Double.class)){
                                    Double res  =   (Double)test;
                                    System.out.println("SINGOLO VALORE " + res);
                                    deviceData.maxCpuPower    =   res;
                                }
                                deviceData.cpuspeeds =   test;
                            }
                            System.out.println("OGGETTO CLASSE: "+test.getClass());
                        }
                    }

                }
            }
            System.out.println("TEST2 " + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}