package com.alfaprojects.paride.it.msaslamonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/*
l'update del grafico viene fatto dopo lo speedtest! quindi dopo lo speedtest (che e' l'operazione piu'
lenta) ho tutti i dati per continuare il processamento
 */
public class MainDashboard extends ActionBarActivity {

    DeviceData currentDevice;   //status of the current device
    double upperbound = -1; //graph y upper bound
    ArrayList<AsyncTask> activeTasks    =   new ArrayList<AsyncTask>();
    static KPIScheduler aScheduler  =   null;
    UsePattern pattern  =   null;
    Task testTask;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if(Singletons.simulatedTimeStep==1) {   //if battery level is simulated skip
                currentDevice.batterylevel = level;
            }
            PieGraph pg = (PieGraph)findViewById(R.id.batterylevelgraph);
            pg.removeSlices();
            PieSlice slice = new PieSlice();
            if(currentDevice.batterylevel>currentDevice.highbatterythreshold){
                slice.setColor(Color.parseColor("#99CC00"));
            }
            else {
                slice.setColor(Color.parseColor("#CC0000"));
            }
            slice.setValue((int)currentDevice.batterylevel);
            pg.addSlice(slice);
            slice = new PieSlice();
            slice.setColor(Color.WHITE);
            slice.setValue(100-(int)currentDevice.batterylevel);
            pg.addSlice(slice);
        }
    };

    public class ShellService extends AsyncTask {
        //shell service is a sort of "container" for the task that needed to be done

        Context aContext;
        TextView tv;
        MainDashboard aDashBoard;
        DeviceData device;

        public ShellService(MainDashboard aDashBoard,DeviceData device) {
            super();
            this.aDashBoard =   aDashBoard;
            this.device     =   device;

        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            TextView    RTTTextView     =   (TextView)findViewById(R.id.connection2);
            TextView    uploadTextView  =   (TextView)findViewById(R.id.connection4);
            TextView    downloadTestView=   (TextView)findViewById(R.id.connection3);
            uploadTextView.setText("Upload "+this.device.bandwidthUL+" Mb/s");
            RTTTextView.setText("RTT " + this.device.RTT + " ms");
            downloadTestView.setText("Download " + this.device.bandwidthDL + " Mb/s");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            System.out.println("STARTING SHELLSERVICE");
            StartService aService = new StartService(device);
            aService.aTextView = tv;
            aService.aContext = aContext;
            AsyncTask task  =   aService.execute();
            while (true){
                //launch pingtest to google DNS
                PingTask aTask = new PingTask(device);
                aTask.aTextView = (TextView) findViewById(R.id.connection2);

                aTask.execute(""+Singletons.serverip);
                //TODO aTask.execute("8.8.8.8");
                activeTasks.add(this);
                activeTasks.add(aTask);
                //launch speedtest (demo image)
                SystemClock.sleep(10000);


                SpeedTestTask speedTestTask =   new SpeedTestTask(device);
                speedTestTask.aTv   =   (TextView) findViewById(R.id.connection3);
                activeTasks.add(speedTestTask);
                AsyncTask task2 =   speedTestTask.execute();
                SystemClock.sleep(10000);
                SpeedTestUploadTask uploadTask  =   new SpeedTestUploadTask(getApplicationContext(),device);
                uploadTask.aTv   =   (TextView)findViewById(R.id.connection4);
                uploadTask.execute();
                Object returnobj    =   uploadTask.result;
                System.out.println("ShellService: ho aspettato il risultato di uploadasynctask "+returnobj);
                activeTasks.add(uploadTask);

/*
                //TODO TEST DATI SIMULATI
                double randomRTT    =   GeneratoreCasuale.randInt(50,10000);
                randomRTT           =   randomRTT/10;
                double randomDL     =   GeneratoreCasuale.randInt(10,10000);
                randomDL            =   randomDL/100;
                double randomUL     =   GeneratoreCasuale.randInt(1,3);
                randomUL            =   randomDL/100;
                this.device.RTT     =   randomRTT;
                this.device.bandwidthDL     =   randomDL;
                this.device.bandwidthUL     =   randomUL;
                this.publishProgress();
                Singletons.updateGraph(this.device);*/

                SystemClock.sleep(10000);
                //start another service like this after 10 seconds than die
                if (isCancelled()) return null;
                //System.out.println("Dati elaborati wifi "+aService.isWifi+" speed "+speedTestTask.result+" RTT "+aTask.RTT );
                if(aScheduler ==null){

                    //TODO REMOVE TEST TASK
                    int random  = (int)GeneratoreCasuale.randInt(900,72000);
                    testTask   =   new Task(1,"sumA/cardA",random,0.5);
                    testTask.setHeuristic("A", 10000, 1);
                    ArrayList<TaskInstance> tasks   =   new ArrayList<>();
                    for(int i=0;i<2000;i++){
                        TaskInstance atask = new TaskInstance(testTask.getId(),testTask.getFormula(),testTask.getExpiration(),testTask.getThreshold(), Singletons.currentSimulatedTime,Singletons.currentSimulatedTime,testTask.getHeurstics(),testTask.getKeys());
                        int k=i+1;//(int)GeneratoreCasuale.randInt(1,50000);
                        double[] testdata	=	new double[k];
                        for(int j=0;j<k;j++){
                            testdata[j]			=	k%50;
                        }
                        atask.addToRawData("A", testdata);;
                        tasks.add(atask);
                    }
                    Log.i("shellservice","non esiste scheduler creo");
                    int[][] hBatteryMatrix	=	pattern.getHighBatteryMatrix(pattern.startDate,pattern.finishDate);
                    int[][] hWifiMatrix      =   pattern.getHighWifiMatrix(pattern.startDate, pattern.finishDate);
                    Decisor testd 	=	 new Decisor(hBatteryMatrix,hWifiMatrix);
                    aScheduler  =   new KPIScheduler(currentDevice,tasks,testd,120000,this.aContext);
                    ExecutorService anExecutor  =   java.util.concurrent.Executors.newFixedThreadPool(100);
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        aScheduler.executeOnExecutor(anExecutor);
                    else
                        aScheduler.execute();
                }

                else{
                    int random  = (int)GeneratoreCasuale.randInt(900,72000);
                    Log.i("shellservice","esiste scheduler, aggiungo a task esistenti");
                    for(int i=0;i<2000;i++){
                        TaskInstance atask = new TaskInstance(testTask.getId(),testTask.getFormula(),testTask.getExpiration(),testTask.getThreshold(), Singletons.currentSimulatedTime,Singletons.currentSimulatedTime,testTask.getHeurstics(),testTask.getKeys());
                        int k=1+i;//(int)GeneratoreCasuale.randInt(1,50000);
                        double[] testdata	=	new double[k];
                        for(int j=0;j<k;j++){
                            testdata[j]			=	j%50;
                        }
                        atask.addToRawData("A", testdata);;
                        aScheduler.mytasks.add(atask);
                    }
                    Log.i("shellservice","esiste scheduler, totale task esistenti "+aScheduler.mytasks.size());
                }
                SystemClock.sleep(Singletons.getInSimulatedtime(30000));
                ShellService newService =   new ShellService(this.aDashBoard,device);
                newService.tv   =   tv;
                newService.aContext =   aContext;
                AsyncTask task3 =   newService.execute();
                return null;
            }

        }

        public class StartService extends AsyncTask {
            boolean isWifi  =   false;
            boolean completed=false;
            Context aContext;
            WifiManager myManager = null;
            public TextView aTextView;
            public DeviceData device;

            public StartService(DeviceData deviceData){
                super();
                this.device =   deviceData;
            }

            public IBinder onBind(Intent intent) {
                return null;
            }

            //returns network generation (cellular)
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

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);
                String RSSI = (String) values[0];
                String type = (String) values[1];
                int intRSSI = Integer.parseInt(RSSI);
                if (intRSSI > -127) {
                    this.aTextView.setText("WiFi");
                    this.device.wifi=true;
                } else {
                    this.aTextView.setText(type);
                    this.device.wifi=false;
                }
            }

            protected Object doInBackground(Object[] params) {
                this.myManager = (WifiManager) this.aContext.getSystemService(Context.WIFI_SERVICE);
                WifiManager myManager = (WifiManager) this.aContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo myInfo = myManager.getConnectionInfo();
                int RSSI = -127; //no signal
                if (myInfo != null) {
                    try {
                        RSSI = myInfo.getRssi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("INFO NOT AVAILABLE");
                }
                String networkKind = getNetworkClass(this.aContext);
                System.out.println(":LIVELLO RSSI " + RSSI + " network type " + networkKind);
                this.device.wifilevel   =   RSSI;
                if(RSSI>-127){
                    this.isWifi =   true;
                }
                String[] values = new String[2];
                values[0] = RSSI + "";
                values[1] = networkKind;
                this.publishProgress(values);
                completed=true;
                return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       currentDevice   =   new DeviceData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);
        //Singletons.calculatespeedparameter();
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //TODO before using it ask the speed parameter to the server!!! (now a default value has been set)
        System.out.println("Scaling factor " + Singletons.getScalingFactor(currentDevice));
        Singletons.dashboard    =   this;
        Singletons.anApplicationContext =   this.getApplicationContext();
        Singletons.setPowerProfile(currentDevice);
        printPowerProfile(currentDevice);
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                int state = wifi.getWifiState();
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    List<ScanResult> results = wifi.getScanResults();

                    for (ScanResult result : results) {
                        if (result.BSSID.equals(wifi.getConnectionInfo().getBSSID())) {
                            int level = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(),
                                    result.level);
                            currentDevice.wifilevel = level;
                            TextView aview = (TextView) findViewById(R.id.wifilevel);
                            if (level == -127) {
                                aview.setText("Wifi not available");
                            } else if (level > currentDevice.highbatterythreshold) {
                                aview.setText("Wifi level: " + level);
                                aview.setTextColor(Color.parseColor("#CC0000"));
                            } else {
                                aview.setText("Wifi level: " + level);
                                aview.setTextColor(Color.parseColor("#99CC00"));
                            }
                        }

                    }
                }
            }
        }, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        pattern  =   new UsePattern("log.txt",30,-80,getApplicationContext());


        /*
        ArrayList<Bar> points = new ArrayList<Bar>();
        Bar d = new Bar();
        d.setColor(Color.parseColor("#99CC00"));
        d.setName(this.getString(R.string.wifiPower));
        d.setValue(10);
        if(this.radiopower.getClass().equals(Double[].class)){
            System.out.println("ARRAY");
            Double[] radio  =   (Double[])this.radiopower;
            for(int i=0;i<radio.length;i++){
                Bar d2 = new Bar();
                d2.setColor(Color.parseColor("#FFBB33"));
                d2.setName(this.getString(R.string.radioPower) + " " + i);
                d2.setValue((radio[i].floatValue()));
                points.add(d);
                points.add(d2);
            }
        }
        else if(this.radiopower.getClass().equals(Double.class)){
            System.out.println("VALORE SINGOLO");
        }
        BarGraph g = (BarGraph)findViewById(R.id.graph2);
        g.setBars(points);*/
    }

    private void printPowerProfile(DeviceData thisDevice) {
        //prints power profile graphs
        ArrayList<Bar> points = new ArrayList<Bar>();
        Bar d = new Bar();
        d.setColor(Color.parseColor("#99CC00"));
        d.setName(this.getString(R.string.wifiPower));
        d.setValue((float) thisDevice.wifipower);
        points.add(d);
        d = new Bar();
        d.setColor(Color.parseColor("#99CC00"));
        d.setName(this.getString(R.string.wifiIdle));
        d.setValue((float) thisDevice.wifiidle);
        points.add(d);
        System.out.println("RADIO POWER "+thisDevice.radiopower+" "+thisDevice.radiopower.getClass());
        if(thisDevice.radiopower.getClass().equals(Double[].class)){
            System.out.println("ARRAY");
            Double[] radio  =   (Double[])thisDevice.radiopower;
            for(int i=0;i<radio.length;i++){
                Bar d2 = new Bar();
                d2.setColor(Color.parseColor("#FFBB33"));
                d2.setName(this.getString(R.string.radioPower) + " " + i);
                d2.setValue((radio[i].floatValue()));
                points.add(d2);
            }
        }
        else if(thisDevice.radiopower.getClass().equals(Double.class)){
            System.out.println("VALORE SINGOLO");
            Bar d2 = new Bar();
            d2.setColor(Color.parseColor("#FFBB33"));
            d2.setName(this.getString(R.string.radioPower));
            d2.setValue(((Double) thisDevice.radiopower).floatValue());
            points.add(d2);
        }
        if(thisDevice.cellularidle.getClass().equals(Double[].class)){
            System.out.println("ARRAY");
            Double[] radio  =   (Double[])thisDevice.cellularidle;
            for(int i=0;i<radio.length;i++){
                Bar d2 = new Bar();
                d2.setColor(Color.parseColor("#FFBB33"));
                d2.setName(this.getString(R.string.radioIdle) + " " + i);
                d2.setValue((radio[i].floatValue()));
                points.add(d2);
            }
        }
        else if(thisDevice.cellularidle.getClass().equals(Double.class)){
            System.out.println("VALORE SINGOLO");
            Bar d2 = new Bar();
            d2.setColor(Color.parseColor("#FFBB33"));
            d2.setName(this.getString(R.string.radioIdle));
            d2.setValue(((Double) thisDevice.cellularidle).floatValue());
            points.add(d2);
        }
        BarGraph g = (BarGraph)findViewById(R.id.graph2);
        g.setBars(points);

        ArrayList<Bar> points2 = new ArrayList<Bar>();
        if(thisDevice.cpupower.getClass().equals(Double[].class)){
            System.out.println("ARRAY");
            Double[] radio  =   (Double[])thisDevice.cpupower;
            Double[] speeds =   (Double[])thisDevice.cpuspeeds;
            for(int i=0;i<radio.length;i++){
                Bar d2 = new Bar();
                d2.setColor(Color.parseColor("#FFBB33"));
                d2.setName(this.getString(R.string.cpuPower) + " @ " + (speeds[i]/1000000)+"Ghz");
                d2.setValue((radio[i].floatValue()));
                points2.add(d2);
            }
        }
        else if(thisDevice.cpupower.getClass().equals(Double.class)){
            System.out.println("VALORE SINGOLO");
            Bar d2 = new Bar();
            d2.setColor(Color.parseColor("#FFBB33"));
            System.out.println("CPU SPEEDS CLASS " + thisDevice.cpuspeeds);
            d2.setName(this.getString(R.string.cpuPower) + " @ " + thisDevice.cpuspeeds.toString());
            d2.setValue(((Double) thisDevice.cpupower).floatValue());
            points2.add(d2);
        }
        BarGraph g2 = (BarGraph)findViewById(R.id.graph3);
        g2.setBars(points2);
    }

    public void updateGraph(boolean isWifi, double result, double RTT,DeviceData deviceData){
        double idlepower    =   0;
        double txpower      =   0;
        if(isWifi==true){
            idlepower   =   deviceData.wifiidle;
            txpower     =   deviceData.wifipower;
        }
        else{
            //TODO
            idlepower   =   deviceData.cellularmaxidle;
            txpower     =   deviceData.cellularmaxpower;
        }
        Line l = new Line();
        l.setColor(Color.parseColor("#FFBB33"));

        //TODO REPLACE WITH REAL DATA
        OffloadingFunction  afunction =   new OffloadingFunction(deviceData.maxCpuPower,idlepower,RTT/1000,3,txpower);//TODO TEST
        for(int i=0;i<3;i++){
            double test = afunction.getLocalComputationTimeForOffloadingTime((i*8)/result);
            System.out.println("Valore per "+i+" "+test);
            LinePoint p = new LinePoint();
            p.setX(i);
            p.setY(test);
            l.addPoint(p);
            if(i==2&&upperbound==-1){
                upperbound  =   test;
            }
        }
        LineGraph li = (LineGraph)findViewById(R.id.graph);
        li.removeAllLines();
        li.addLine(l);
        if((int)upperbound==0){
            upperbound  =   1;
        }
        li.setRangeY(0, (int)upperbound);
        li.setLineToFill(0);
        System.out.println("parametri passati: max cpu power "+deviceData.maxCpuPower+" wifi idle "+deviceData.wifiidle+" wifi power "+deviceData.wifipower);
        //System.out.println("VALORE PROCESSATO " + test);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_dashboard, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ShellService aService   =   new ShellService(this,currentDevice);
        aService.aContext   =   this.getApplicationContext();
        aService.tv         =   (TextView)this.findViewById(R.id.connection);
        final FloatingActionsMenu     choicemenu      =   (FloatingActionsMenu)this.findViewById(R.id.famChoice);
        FloatingActionButton    standardButton  =   (FloatingActionButton)this.findViewById(R.id.fabStandard);
        FloatingActionButton    emulationButton =   (FloatingActionButton)this.findViewById(R.id.fabEmulation);
        standardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainDashboard: esecuzione standard");
                choicemenu.collapseImmediately();
                Singletons.simulatedTimeStep    =   1;
                choicemenu.setEnabled(false);
            }
        });
        emulationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainDashboard: esecuzione simulata");
                choicemenu.collapseImmediately();
                choicemenu.setEnabled(false);
                aService.execute();
            }
        });

        //TestJson testJson=  new TestJson();
        //testJson.execute();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(int i=0;i<activeTasks.size();i++){
            AsyncTask item  =   activeTasks.get(i);
            if(!item.isCancelled())
                item.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
