package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.Date;

/**
 * Created by paride on 26/08/15.
 */
public class KPIElaborator{
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

    public int compute() {
        //data.updateBatteryAndSignalState(); //before making elaboration please update cellular state
        int batterylevel=0;
        int wifilevel   =0;
        Singletons.setProcessedTasks();
        System.out.println("KPIELABORATOR inizio esecuzione");
        if(data.batterylevel>=data.highbatterythreshold){
            batterylevel    =   1;
        }
        if(data.wifilevel>=data.highwifithreshold){
            wifilevel       =   1;
        }
        //decide if compute now or later
        int result =    aDecisor.decide(batterylevel,wifilevel,Singletons.currentSimulatedTime,subject.getExpirationDate());
        Date next   =   new Date(Singletons.currentSimulatedTime.getTime()+delay);
        //check if the next interval is AFTER task expiration date!
        if (next.after(subject.getExpirationDate())){
            result  =   0;
        }
        System.out.println("KPIELABORATOR risultato dopo 1a decisione " + result+" now "+Singletons.currentSimulatedTime.toString()+" expiration "+subject.getExpirationDate().toString());
        if(result==1){   //while i don't have to compute check again and sleep
            Singletons.setPostponedTask();
            return 0;
        }
        System.out.println("KPIELABORATOR elaboro ");
        //if (wifilevel > -127) {
        //    data.wifi   =   true;
        //} else {
        //    data.wifi   =   false;
        //}
        double idlepower    =   0;
        double txpower      =   0;
        System.out.println("KPIElaborator livello wifi "+data.wifilevel);
        if(data.wifi==true){
            idlepower   =   data.wifiidle;
            txpower     =   data.wifipower;
            System.out.println("KPIElaborator sto in wifi potenze: idle: " + idlepower + " TX: " + txpower);
        }
        else{
            idlepower   =   data.cellularmaxidle;
            txpower     =   data.cellularmaxpower;
            System.out.println("KPIElaborator sto in cellular potenze: idle: "+idlepower+" TX: "+txpower);
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
        double saving   =   (localtime*data.maxCpuPower)-(myOffloadingFunction.calculateOffloadingConsumption(txtime,localtime));
        if(localtime>maxlocaltime){
            //TODO OFFLOAD
            Singletons.setOffloadedTasks();
            Singletons.setSavedEnergy((int) saving);
            System.out.println("KPIELABORATOR  vado in offload");
            System.out.println("KPIELABORATOR risparmio calcolato " + saving);
            System.out.println("KPIELABORATOR consumo calcolato OFFLOAD " + myOffloadingFunction.calculateOffloadingConsumption(txtime,localtime));
            SendTask sendTask   =   new SendTask(subject.generateJson());
            sendTask.execute();
        }
        else{
            Singletons.setLocalTasks();
            System.out.println("KPIELABORATOR  vado in locale");
            double solution =   0;
            if(subject.getDataSize()>0){
                long start  =   System.currentTimeMillis();
                solution    =   ExpressionSolver.getResult(subject.getFormula(),subject.getRawdata());
                long end    =   System.currentTimeMillis();
                end =   end-start;
                double timer =   (double)end/1000;
                System.out.println("KPIElaborator task elaborato "+solution+" tempo previsto "+localtime+" tempo effettivo "+timer+" numero elementi "+subject.getDataSize());
            }
            else{
                solution    =   -1;
                System.out.println("KPIElaborator task vuoto!!!");
            }
            subject.setProcesseddata(solution);
            System.out.println("KPIELABORATOR  risultato: " + solution);
            subject.resetRawData();
            SendTask sendTask   =   new SendTask(subject.generateJson());
            sendTask.execute();
            double consumption   =   localtime*data.maxCpuPower;
            System.out.println("KPIELABORATOR consumo calcolato LOCALE " + consumption);
        }
        //TODO save values on a log
        return 1;
    }
}
