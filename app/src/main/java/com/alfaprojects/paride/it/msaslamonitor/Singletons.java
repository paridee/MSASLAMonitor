package com.alfaprojects.paride.it.msaslamonitor;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by paride on 01/07/15.
 * this class contains all the project's singletons
 */
public class Singletons {
    private static int processedTasks       =   0;
    private static int localTasks           =   0;
    private static int offloadedTasks       =   0;
    private static int savedEnergy          =   0;
    private static int postponedTask        =   0;
    private static ReentrantLock valueslock        =   new ReentrantLock();
    public static MainDashboard dashboard=null;
    public static Date    currentSimulatedTime            =   new Date();
    public static double  currentSimulatedBattery         =     1.0;
    public static int     currentSimulatedWifilevel       =     -127;
    public static int     scalingfactor                   =     900;
    public static int     simulatedTimeStep               =     900000;
    public static Context anApplicationContext;
    //public static String serverip   =   "192.168.43.227";
    public static String serverip   =   "51.254.137.204";
    public static String userEmailAddress;
    public static String GCMToken;
    private static AlarmSerializer sqLiteSerializer=null;
    private static GeneratoreCasuale paretogenerator    =   GeneratoreCasuale.Pareto;
    private static Random randomGen =   new Random(System.currentTimeMillis());


    /**
     * auxiliary method to get string from date object
     * @param aDate date to be converted
     * @return string with the date
     */
    public static String getStringFromDate(Date aDate){
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ITALY);
        return  df.format(aDate);
    }

    /**
     * returns the value of total task instances processed
     * @return
     */
    public static int getLocalTasks() {
        return localTasks;
    }

    /**
     * sets the total number of task instances processed
     */
    public static void setLocalTasks() {
        valueslock.lock();;
        Singletons.localTasks = localTasks+1;
        valueslock.unlock();
    }

    /**
     * gets the number of offloaded tasks
     * @return
     */
    public static int getOffloadedTasks() {
        return offloadedTasks;
    }

    /**
     * increase the number of offloaded tasks counter
     */
    public static void setOffloadedTasks() {
        valueslock.lock();
        Singletons.offloadedTasks = offloadedTasks+1;
        valueslock.unlock();
    }

    /**
     * gets the number of postponed tasks instances
     */
    public static int getPostponedTask() {
        return postponedTask;
    }

    /**
     * increases the counter for postponed tasks
     */
    public static void setPostponedTask() {
        valueslock.lock();
        Singletons.postponedTask = postponedTask+1;
        valueslock.unlock();
    }

    /**
     * returnes the value of total processed tasks counter
     * @return
     */
    public static int getProcessedTasks() {
        return processedTasks;
    }

    /**
     * increases the counter for total processed tasks
     */
    public static void setProcessedTasks() {
        valueslock.lock();
        Singletons.processedTasks = processedTasks+1;
        valueslock.unlock();
    }

    /**
     * gets the value of saved energy
     * @return
     */
    public static int getSavedEnergy() {
        return savedEnergy;
    }

    /**
     * increases the value for the counter of saved energy
     * @param savedEnergy delta
     */
    public static void setSavedEnergy(int savedEnergy) {
        valueslock.lock();
        Singletons.savedEnergy = savedEnergy+Singletons.savedEnergy;
        valueslock.unlock();
    }

    /**
     * updates the offloading graph
     * @param data
     */
    public static void updateGraph(DeviceData data) {
        if(data.RTT!=-1&&data.bandwidthUL!=-1)
            dashboard.updateGraph(data.wifi,data.bandwidthUL,data.RTT,data);
        else{
            System.out.println("missing some values");
        }
    }

    //in my simulated time 900 seconds (900000 milliseconds) are 100 millisencods of real time (if scalingfactor = 9000)
    public static int getInSimulatedtime(int milliseconds){
        return (milliseconds/scalingfactor);
    }

    /**
     * advances simulated time (hardcoded timestep)
     */
    public static void advanceSimulatedTime(){
        if(simulatedTimeStep==1){
            currentSimulatedTime    =   new Date();
            return;
        }
        Calendar aCalendar  =   Calendar.getInstance();
        aCalendar.setTime(currentSimulatedTime);
        long millis =   aCalendar.getTimeInMillis();
        millis  =   millis+simulatedTimeStep;
        currentSimulatedTime    =   new Date(millis);
        System.out.println("Singletons.java time advanced: "+currentSimulatedTime.toString());
    }


    //not used, in this case scaling factor is hardcoded after experimental tests
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

    /**
     * returns actual scaling factor
     * @param device
     * @return
     */
    public static double getScalingFactor(DeviceData device){
        double value    =   (double)device.mobilespeedparameter/(double)device.cloudspeedparameter;
        return value;
    }

    /**
     * read the power profile data
     * @param deviceData
     */
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

    /**
     * updates the text views in UI
     */
    public static void updateTasksTextView() {

        TextView tvTotalTasks       =   (TextView)  dashboard.findViewById(R.id.tvTotalTasks);
        TextView tvPostponedTasks   =   (TextView)  dashboard.findViewById(R.id.tvPostponedTasks);
        TextView tvLocalTasks       =   (TextView)  dashboard.findViewById(R.id.tvLocalTasks);
        TextView tvOffloadedTasks   =   (TextView)  dashboard.findViewById(R.id.tvOffloadedTasks);
        TextView tvSavings          =   (TextView)  dashboard.findViewById(R.id.tvSavedEnergy);
        if(tvTotalTasks!=null){
            tvTotalTasks.setText("Total tasks instances managed: "+Singletons.getProcessedTasks());
        }
        if(tvLocalTasks!=null){
            tvLocalTasks.setText("Total local tasks: "+Singletons.getLocalTasks());
        }
        if(tvOffloadedTasks!=null){
            tvOffloadedTasks.setText("Total offloaded tasks: "+Singletons.getOffloadedTasks());
        }
        if(tvPostponedTasks!=null){
            tvPostponedTasks.setText("Total postponed tasks: "+Singletons.getPostponedTask());
        }
        if(tvSavings!=null){
            tvSavings.setText("Total energy saving: " + Singletons.getSavedEnergy()+" mAs");
        }
    }

    /*
 * Singleton for SQLiteSerializer.
 * @return the SQLiteSerializer instance.
 */
    public static AlarmSerializer getDBSerializer(Context aContext) {

        if (sqLiteSerializer == null) {
            sqLiteSerializer = new AlarmSerializer(aContext, "alarm.db");
            sqLiteSerializer.open();
        }
        return sqLiteSerializer;
    }

    /**
     * simulates battery drain
     * @param data
     */
    public static void advanceBatterySimulation(DeviceData data){
        double actualLevel  =   data.batterylevel;
        if(actualLevel<=2){
            System.out.println("Low battery simulation level, recharge");
            int chargeplus  =   GeneratoreCasuale.randInt(1,98);
            data.batterylevel   =   actualLevel+chargeplus;
        }
        else{
            double batterydec   = 4* simulatedTimeStep/3600000;
            data.batterylevel   =   actualLevel-batterydec;
            System.out.println("Low battery simulation level discharging to "+actualLevel);
            }



        //Update battery pie chart

        PieGraph pg = (PieGraph)dashboard.findViewById(R.id.batterylevelgraph);
        pg.removeSlices();
        PieSlice slice = new PieSlice();
        if(data.batterylevel>data.highbatterythreshold){
            slice.setColor(Color.parseColor("#99CC00"));
        }
        else {
            slice.setColor(Color.parseColor("#CC0000"));
        }
        slice.setValue((int) data.batterylevel);
        pg.addSlice(slice);
        slice = new PieSlice();
        slice.setColor(Color.WHITE);
        slice.setValue(100 - (int) data.batterylevel);
        pg.addSlice(slice);
    }

    //simulates connectivity environment, wifi availability and ////
    public static void advanceConnectivitySimulation(DeviceData data){
        int dado        =    GeneratoreCasuale.randInt(0,10);
        if(dado<2){
            data.wifi   =   true;
            int dadoint        =    GeneratoreCasuale.randInt(50,60);
            data.wifilevel     =    -dadoint;
        }
        else{
            data.wifi       =   false;
            data.wifilevel  =   -127;
        }
        System.out.println("Singletons.java simulating connevtivity environment wifi availability " + data.wifi + " signal level " + data.wifilevel);
        dashboard.updateWifiLevel(data);
        int dlSimulatedBW   =       100000+(int)(25000*(randomGen.nextGaussian()));//gaussiana ha media 0 e varianza 1, +4 rende valori "sempre"positivi (vd grafico dist)"
        int ulSimulatedBW   =       40000+(int)(10000*(randomGen.nextGaussian()));
        if(dlSimulatedBW<0){
            dlSimulatedBW   =   1;
        }
        if(ulSimulatedBW<0){
            ulSimulatedBW   =   1;
        }
        data.bandwidthDL    =       (double)dlSimulatedBW/100000;
        data.bandwidthUL    =       (double)ulSimulatedBW/100000;
        updateBWTextViews(data);
        double dadoping        =    paretogenerator.getRandom(1, 100);
        data.RTT  =   dadoping;
        updateTVRTT(dadoping,data," test ");
    }

    public static void updateBWTextViews(DeviceData data){
        TextView dlTextView =   (TextView)dashboard.findViewById(R.id.connection3);
        dlTextView.setText("DL Bandwidth "+data.bandwidthDL+" Mb/s");
        dlTextView =   (TextView)dashboard.findViewById(R.id.connection4);
        dlTextView.setText("UL Bandwidth " + data.bandwidthUL + " Mb/s");
        TextView tvConn     =   (TextView)dashboard.findViewById(R.id.connection);
        if(data.wifilevel>-127){
            tvConn.setText("Simulation WiFi");
        }
        else{
            tvConn.setText("Simulation Cellular");
        }
    }

    /**
     * updates ping textview
     * @param value
     * @param data
     * @param endpoint
     */
    public static void updateTVRTT(double value,DeviceData data,String endpoint) {
        TextView pingView   =   ((TextView)dashboard.findViewById(R.id.connection2));
        if(value>200)
            pingView.setTextColor(Color.parseColor("#FF3D33"));
        else if (value>50){
            pingView.setTextColor(Color.parseColor("#FFBB33"));
        }
        else{
            pingView.setTextColor(Color.parseColor("#99CC00"));
        }
        pingView.setText("RTT "+endpoint+" "+value+"ms");
    }
}
