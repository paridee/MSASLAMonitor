package com.alfaprojects.paride.it.msaslamonitor;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by paride on 10/09/15.
 */
public class KPIScheduler extends AsyncTask {

    DeviceData  deviceData;
    Decisor     decisor;
    int         polling;
    private ArrayList<TaskInstance> mytasks;
    static boolean waitp   =   true;
    Context appContext;
    ReentrantLock taskListLock   =   new ReentrantLock();

    public KPIScheduler(DeviceData data,ArrayList<TaskInstance> mytasks,Decisor aDecisor,int delay,Context appContext){
        this.deviceData =   data;
        this.decisor    =   aDecisor;
        this.polling    =   delay;
        this.mytasks    =   mytasks;
        this.appContext =   appContext;
    }

    /**
     * adds a task to the tasklist
     * @param aTask
     */
    public void addToTaskList(TaskInstance aTask){
        taskListLock.lock();
        mytasks.add(aTask);
        taskListLock.unlock();
    }

    public int getTaskListSize(){
        taskListLock.lock();
        int size    =   mytasks.size();
        taskListLock.unlock();
        return size;
    }

    /**
     * updates the UI
     * @param values
     */
    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        Singletons.updateTasksTextView();
        if(values.length>0){
            if((boolean) values[0]){
                Singletons.advanceSimulatedTime();
                Singletons.advanceBatterySimulation(deviceData);
                Singletons.advanceConnectivitySimulation(deviceData);
                Singletons.updateGraph(deviceData);
            }
        }
    }



    @Override
    protected Object doInBackground(Object[] params) {
        while(true){
            SystemClock.sleep(Singletons.getInSimulatedtime(this.polling));

            while(waitp){
                SystemClock.sleep(Singletons.getInSimulatedtime(2000));
                if(!(deviceData.RTT==-1||deviceData.bandwidthUL==-1||deviceData.bandwidthDL==-1)){
                    waitp   =   false;
                }
                System.out.println("KpiScheduler ASPETTO DEVICEDATA RTT "+deviceData.RTT+" UL "+deviceData.bandwidthUL+" DL "+deviceData.bandwidthDL);
            }
            System.out.println("KpiScheduler OTTENUTI DEVICEDATA");
            System.out.println("KpiScheduler s'e' svegliato lunghezza lista task " + mytasks.size());
            if(Singletons.simulatedTimeStep!=1){
                Object[]    args    =   new Object[1];  //stub argument,signals that a simulation advance is needed
                args[0]             =   true;
                this.publishProgress(args);
            }

            ArrayList<TaskInstance> launched    =   new ArrayList<TaskInstance>();
            taskListLock.lock();
            for(int i=0;i<mytasks.size();i++){
                System.out.println("KPIScheduler instanzio elaborator posizione "+i+" size array "+mytasks.size());
                KPIElaborator elaborator    =   new KPIElaborator(this.decisor,this.deviceData,this.polling,mytasks.get(i));
                System.out.println("KPIScheduler vado per eseguire");
                int completed   =   elaborator.compute();
                System.out.println("KPIScheduler completato round task "+mytasks.get(i).toString()+" con risultato "+completed);
                if(completed==1) {
                    System.out.println("KPIScheduler inserisco in lista eliminazione ");
                    Object[]    args    =   new Object[1];  //stub argument,signals that a simulation advance is needed
                    args[0]             =   false;
                    this.publishProgress();
                    launched.add(mytasks.get(i));
                }
                System.out.println("KPIScheduler esco dall'array launched "+launched.size()+" tasks");
            }
            for(int j=0;j<launched.size();j++){
                System.out.println("KPIScheduler rimuovo ");
                mytasks.remove(launched.get(j));
            }
            taskListLock.unlock();
            System.out.println("KPIScheduler completato totale round task ");

        }
    }
}
