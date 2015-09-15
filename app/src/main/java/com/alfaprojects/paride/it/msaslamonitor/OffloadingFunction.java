package com.alfaprojects.paride.it.msaslamonitor;

/**
 * Created by paride on 30/06/15.
 */
public class OffloadingFunction {
    double txPower;
    double idlePower;
    double RTT;
    double computationLocalPower;
    double scalingFactor;

    public OffloadingFunction(double computationLocalPower, double idlePower, double RTT, double scalingFactor, double txPower) {
        this.computationLocalPower = computationLocalPower;
        this.idlePower = idlePower;
        this.RTT = RTT;
        this.scalingFactor = scalingFactor;
        this.txPower = txPower;
    }

    public void setRTT(double RTT) {
        this.RTT = RTT;
    }

    public double getLocalComputationTimeForOffloadingTime(double txTime){
        double res  =   ((this.txPower*txTime)+(this.idlePower*this.RTT))/(this.computationLocalPower-(this.idlePower/this.scalingFactor));
        return res;
    }

    public double calculateOffloadingConsumption(DeviceData data, double txtime,double localComputationTime){
        if (data.wifilevel > -127) {
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
        double result   =   (txpower*txtime)+(idlepower*data.RTT)+(data.maxCpuPower*(idlepower/scalingFactor));
        return result;
    }
}
