package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.Date;

/**
 * Created by paride on 04/10/15.
 * this class creates fake tasks to be scheduled... 2 types, testing purpose
 */
public class FakeTaskLauncher extends AsyncTask {
    KPIScheduler kpiScheduler;
    MainDashboard mainDashboard;
    Task    firstTestTask;
    Task    secondTestTask;
    double  th1 =   2.5;
    double  th2 =   4;
    public FakeTaskLauncher(KPIScheduler aScheduler,MainDashboard mainDashboard){
        kpiScheduler        =   aScheduler;
        this.mainDashboard  =   mainDashboard;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //task is average, i will reuse heuristics also for the second one
        firstTestTask   =   new Task(1,"sumA/cardA",1,0.5); //fake expiration, will be replaced in instances
        firstTestTask.setHeuristic("A", 10000, GeneratoreCasuale.Pareto);
        mainDashboard.publishCPUTimes(firstTestTask.heurstics, firstTestTask.keys);
        secondTestTask  =   new Task(2,"sumA/cardA",1,0.5);
        secondTestTask.setHeuristic("A", 10000, GeneratoreCasuale.Exponential);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        while (true){
            Date    t1startDate =   new Date();
            Date    t1endDate;
            Date    t2startDate =   new Date();
            Date    t2endDate;
            if(Singletons.simulatedTimeStep!=1) {
                t1startDate = Singletons.currentSimulatedTime;
                t2startDate = Singletons.currentSimulatedTime;
            }
            long rand1      =   GeneratoreCasuale.randInt(900,60*60*24*30);
            rand1           =   rand1*1000;
            long rand2      =   GeneratoreCasuale.randInt(900,60*60*24*30);
            rand2           =   rand2*1000;
            t1endDate       =   new Date(t1startDate.getTime()+rand1);
            t2endDate       =   new Date(t2startDate.getTime()+rand2);
            int exrand1     =   GeneratoreCasuale.randInt(900,60*60*24*7);//simulated expiration
            int exrand2     =   GeneratoreCasuale.randInt(900,60*60*24*7);//simulated expiration
            TaskInstance t1 =   new TaskInstance(firstTestTask.getId(),firstTestTask.getFormula(),exrand1,th1, t1startDate,t1endDate,firstTestTask.getHeurstics(),firstTestTask.getKeys());
            t1.heurstics    =   firstTestTask.heurstics;
            t1.keys         =   firstTestTask.keys;
            TaskInstance t2 =   new TaskInstance(secondTestTask.getId(),secondTestTask.getFormula(),exrand2,th2, t2startDate,t2endDate,secondTestTask.getHeurstics(),secondTestTask.getKeys());
            t2.heurstics    =   secondTestTask.heurstics;
            t2.keys         =   secondTestTask.keys;
            int k=1+GeneratoreCasuale.randInt(1,5000);//(int)GeneratoreCasuale.randInt(1,50000);
            double[] testdata	=	new double[k];
            GeneratoreCasuale testStat = GeneratoreCasuale.Pareto;
            for(int j=0;j<k;j++){
                double value    =   testStat.getRandom(3,2);
                if(value<0.01){
                    value   =   0;
                }
                testdata[j]			=   value;
            }
            t1.addToRawData("A", testdata);
            kpiScheduler.addToTaskList(t1);
            k=1+GeneratoreCasuale.randInt(1,5000);//(int)GeneratoreCasuale.randInt(1,50000);
            testdata	=	new double[k];
            testStat = GeneratoreCasuale.Exponential;
            for(int j=0;j<k;j++){
                double  value   =   testStat.getRandom(0.25);
                if(value<0.01){
                    value   =   0;
                }
                testdata[j]			=   value;
            }
            t2.addToRawData("A", testdata);
            kpiScheduler.addToTaskList(t2);
            System.out.println("FakeTaskLauncher launched fake tasks");
            SystemClock.sleep(2000);
            }
        }
    }

