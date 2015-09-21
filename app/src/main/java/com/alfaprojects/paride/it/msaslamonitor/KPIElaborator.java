package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.Date;

/**
 * Created by paride on 26/08/15.
 */
public class KPIElaborator extends AsyncTask{
    public DeviceData       data;
    public TaskInstance     subject;
    public Decisor          aDecisor;
    public int              delay;//milliseconds, poll interval for check

    public KPIElaborator(Decisor aDecisor, DeviceData data, int delay, TaskInstance subject) {
        this.aDecisor = aDecisor;
        this.data = data;
        this.delay = delay;
        this.subject = subject;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        //data.updateBatteryAndSignalState(); //before making elaboration please update cellular state
        int batterylevel=0;
        int wifilevel   =0;
        System.out.println("KPIELABORATOR inizio esecuzione");
        if(data.batterylevel>=data.highbatterythreshold){
            batterylevel    =   1;
        }
        if(data.wifilevel>=data.highwifithreshold){
            wifilevel       =   1;
        }
        //decide if compute now or later
        int result =    aDecisor.decide(batterylevel,wifilevel,Singletons.currentSimulatedTime,subject.getExpirationDate());
        System.out.println("KPIELABORATOR risultato dopo 1a decisione " + result);
        while(result==1){   //while i don't have to compute check again and sleep
            SystemClock.sleep(Singletons.getInSimulatedtime(delay));
            Date now    =   Singletons.currentSimulatedTime;
            result =    aDecisor.decide(batterylevel,wifilevel,now,subject.getExpirationDate());
            System.out.println("KPIELABORATOR risultato dopo 1a decisione succ "+result);
            Date    today   =  now;
            Date next   =   new Date(today.getTime()+delay);

            //TODO TEST
            //result  =   0;

            //check if the next interval is AFTER task expiration date!
            if (next.after(subject.getExpirationDate())){
                result  =   0;
            }
        }
        System.out.println("KPIELABORATOR elaboro ");
        if (wifilevel > -127) {
            data.wifi   =   true;
        } else {
            data.wifi   =   false;
        }
        double idlepower    =   0;
        double txpower      =   0;
        if(data.wifi==true){
            idlepower   =   data.wifiidle;
            txpower     =   data.wifipower;
        }
        else{
            idlepower   =   data.cellularmaxidle;
            txpower     =   data.cellularmaxpower;
        }
        double RTTms    =   data.RTT/1000;
        OffloadingFunction myOffloadingFunction =   new OffloadingFunction(data.maxCpuPower,idlepower,RTTms,data.scalingfactor,txpower);
        String json     =   subject.generateJson();
        //TODO
        //TODO
        //TODO misura header HTTP per il messaggio per calcolare il tempo di trasmissione
        int messagesize =   json.length();//TODO +httpheader
        System.out.println("KPIELABORATOR calcolo dati da trasmettere ");
        double localtime    =   subject.calculateHeuristicTime()/1000;   //divide 1000 seconds!!!
        double numerator    =   (double)(messagesize*8)/(1024*1024);
        System.out.println("KPIELABORATOR numeratore "+numerator);
        double txtime               =   (numerator)/data.bandwidthUL;//TODO speedtest uplink! TIME IN SECONDS ASSHOLE!
        double maxlocaltime         =   myOffloadingFunction.getLocalComputationTimeForOffloadingTime(txtime);
        double sizeforoffloading    =   (maxlocaltime*data.bandwidthUL)/8;
        System.out.println("KPIELABORATOR  valori trovati: localtime "+localtime+" txtime "+txtime+" per lunghezza messaggio "+messagesize+" maxlocaltime "+maxlocaltime+" maximum data size for offloading "+sizeforoffloading+" MB numero elementi "+subject.getDataSize());
        if(localtime>maxlocaltime){
            //TODO OFFLOAD
            System.out.println("KPIELABORATOR  vado in offload");
            double saving   =   localtime*data.maxCpuPower-myOffloadingFunction.calculateOffloadingConsumption(data,txtime,localtime);
            System.out.println("KPIELABORATOR risparmio calcolato " + saving);
            System.out.println("KPIELABORATOR consumo calcolato OFFLOAD " + myOffloadingFunction.calculateOffloadingConsumption(data,txtime,localtime));
        }
        else{
            System.out.println("KPIELABORATOR  vado in locale");
            //double solution =   ExpressionSolver.getResult(subject.getFormula(),subject.getRawdata());
            //subject.setProcesseddata(solution);
            //System.out.println("KPIELABORATOR  risultato: "+solution);
            //TODO SEND TO SERVER (only solution)
            double consumption   =   localtime*data.maxCpuPower;
            System.out.println("KPIELABORATOR consumo calcolato LOCALE " + consumption);
        }
        //TODO save values on a log
        return null;
    }
}
