package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.os.SystemClock;

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

    public KPIScheduler(DeviceData data,ArrayList<TaskInstance> mytasks,Decisor aDecisor,int delay){
        this.deviceData =   data;
        this.decisor    =   aDecisor;
        this.polling    =   delay;
        this.mytasks    =   mytasks;
    }
    @Override
    protected Object doInBackground(Object[] params) {
        System.out.println("KpiScheduler m'addormo pe' 2 minuti");
        SystemClock.sleep(10000);

        while(waitp){
            SystemClock.sleep(2000);
            if(!(deviceData.RTT==-1||deviceData.bandwidthUL==-1||deviceData.bandwidthDL==-1)){
                waitp   =   false;
            }
            System.out.println("KpiScheduler ASPETTO DEVICEDATA RTT "+deviceData.RTT+" UL "+deviceData.bandwidthUL+" DL "+deviceData.bandwidthDL);
        }
        System.out.println("KpiScheduler OTTENUTI DEVICEDATA");
        System.out.println("KpiScheduler s'e' svejatoooooooooooo lunghezza lista task "+mytasks.size());
        ArrayList<TaskInstance> launched    =   new ArrayList<TaskInstance>();
        for(int i=0;i<mytasks.size();i++){
            KPIElaborator elaborator    =   new KPIElaborator(this.decisor,this.deviceData,this.polling,mytasks.get(i));
            elaborator.execute();
            launched.add(mytasks.get(i));
        }
        for(int j=0;j<launched.size();j++){
            mytasks.remove(launched.get(j));
        }
        KPIScheduler newInstance    =   new KPIScheduler(this.deviceData,this.mytasks,this.decisor,this.polling);
        newInstance.execute();
        return null;
    }
}
