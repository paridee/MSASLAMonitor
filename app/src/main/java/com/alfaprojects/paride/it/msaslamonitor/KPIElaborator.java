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
        int result =    aDecisor.decide(batterylevel,wifilevel,new Date(),subject.getExpirationDate());
        System.out.println("KPIELABORATOR risultato dopo 1a decisione " + result);
        while(result==1){   //while i don't have to compute check again and sleep
            SystemClock.sleep(delay);
            result =    aDecisor.decide(batterylevel,wifilevel,new Date(),subject.getExpirationDate());
            System.out.println("KPIELABORATOR risultato dopo 1a decisione succ "+result);
            Date    today   =   new Date();
            Date next   =   new Date(today.getTime()+delay);
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
        OffloadingFunction myOffloadingFunction =   new OffloadingFunction(data.maxCpuPower,idlepower,data.RTT/1000,data.scalingfactor,txpower);
        String json     =   subject.generateJson();
        //TODO
        //TODO
        //TODO misura header HTTP per il messaggio per calcolare il tempo di trasmissione
        int messagesize =   json.length();//TODO +httpheader
        System.out.println("KPIELABORATOR calcolo dati da trasmettere ");
        double localtime    =   subject.calculateHeuristicTime();   //todo calculate local time (heuristic)
        double txtime   =   ((messagesize*8)/(1024*1024))/data.bandwidthUL;//TODO speedtest uplink!
        double maxlocaltime=   myOffloadingFunction.getLocalComputationTimeForOffloadingTime(txtime);
        System.out.println("KPIELABORATOR  valori trovati: localtime "+localtime+" txtime "+txtime+" maxlocaltime "+maxlocaltime);
        if(localtime>maxlocaltime){
            //TODO OFFLOAD
            System.out.println("KPIELABORATOR  vado in offload");
            double saving   =   localtime*data.maxCpuPower-myOffloadingFunction.calculateOffloadingConsumption(data,txtime,localtime);
        }
        else{
            System.out.println("KPIELABORATOR  vado in locale");
            double solution =   ExpressionSolver.getResult(subject.getFormula(),subject.getRawdata());
            subject.setProcesseddata(solution);
            System.out.println("KPIELABORATOR  risultato: "+solution);
            //TODO SEND TO SERVER (only solution)
        }
        //TODO save values on a log
        return null;
    }
}
