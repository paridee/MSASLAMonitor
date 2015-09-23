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
        System.out.println("TXTIME "+txTime+" sec RTT "+this.RTT+"sec");
        double res  =   ((this.txPower*txTime)+(2*this.idlePower*this.RTT))/(this.computationLocalPower-(this.idlePower/this.scalingFactor));
        return res;
    }

    public double calculateOffloadingConsumption(double txtime,double localComputationTime){
        double result   =   (this.txPower*txtime)+(2*this.idlePower*this.RTT)+(localComputationTime*(this.idlePower/scalingFactor));
        return result;
    }
}
