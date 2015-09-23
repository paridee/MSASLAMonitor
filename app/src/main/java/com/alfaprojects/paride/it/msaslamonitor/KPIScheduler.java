package com.alfaprojects.paride.it.msaslamonitor;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by paride on 10/09/15.
 */
public class KPIScheduler extends AsyncTask {

    DeviceData  deviceData;
    Decisor     decisor;
    int         polling;
    public ArrayList<TaskInstance> mytasks;
    static boolean waitp   =   true;
    Context appContext;

    public KPIScheduler(DeviceData data,ArrayList<TaskInstance> mytasks,Decisor aDecisor,int delay,Context appContext){
        this.deviceData =   data;
        this.decisor    =   aDecisor;
        this.polling    =   delay;
        this.mytasks    =   mytasks;
        this.appContext =   appContext;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        Singletons.updateTasksTextView();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        while(true){
            System.out.println("KpiScheduler m'addormo pe' 2 minuti cioe "+Singletons.getInSimulatedtime(this.polling)+" millisecondi");
            SystemClock.sleep(Singletons.getInSimulatedtime(this.polling));

            while(waitp){
                SystemClock.sleep(Singletons.getInSimulatedtime(2000));
                if(!(deviceData.RTT==-1||deviceData.bandwidthUL==-1||deviceData.bandwidthDL==-1)){
                    waitp   =   false;
                }
                System.out.println("KpiScheduler ASPETTO DEVICEDATA RTT "+deviceData.RTT+" UL "+deviceData.bandwidthUL+" DL "+deviceData.bandwidthDL);
            }
            System.out.println("KpiScheduler OTTENUTI DEVICEDATA");
            System.out.println("KpiScheduler s'e' svejatoooooooooooo lunghezza lista task "+mytasks.size());
            Singletons.advanceSimulatedTime();
            ArrayList<TaskInstance> launched    =   new ArrayList<TaskInstance>();
            for(int i=0;i<mytasks.size();i++){
                System.out.println("KPIScheduler instanzio elaborator posizione "+i+" size array "+mytasks.size());
                KPIElaborator elaborator    =   new KPIElaborator(this.decisor,this.deviceData,this.polling,mytasks.get(i));
                System.out.println("KPIScheduler vado per eseguire");
                int completed   =   elaborator.compute();
                System.out.println("KPIScheduler completato round task "+mytasks.get(i).toString()+" con risultato "+completed);
                if(completed==1) {
                    System.out.println("KPIScheduler inserisco in lista eliminazione ");
                    this.publishProgress();
                    launched.add(mytasks.get(i));
                }
                System.out.println("KPIScheduler esco dall'array ");
            }
            for(int j=0;j<launched.size();j++){
                System.out.println("KPIScheduler rimuovo ");
                mytasks.remove(launched.get(j));
            }
            System.out.println("KPIScheduler completato totale round task ");

        }
    }
}
