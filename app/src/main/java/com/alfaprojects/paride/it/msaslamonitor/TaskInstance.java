package com.alfaprojects.paride.it.msaslamonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.Gson;

public class TaskInstance extends Task {
	public	HashMap<Integer,Double>		heurstics =	new HashMap<Integer,Double>();
	int[]								keys	=	new int[5];
	private Date 						startDate;
	private Date 						endDate;
	private Date						expirationDate;
	private double 						processeddata	=	-1;
	private HashMap<String,double[]> 	rawdata			=	new HashMap<String,double[]>();
	public TaskInstance(int id, String formula, int expiration,
						double threshold, Date startDate, Date endDate) {
		super(id, formula, expiration, threshold);
		this.startDate		=	startDate;
		long endseconds		=	startDate.getTime()+(expiration*1000);	//library uses milliseconds
		this.expirationDate	=	new Date(endseconds);
		this.endDate		=	endDate;
		//TODO setExpirationDate
	}

	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}

	public HashMap<String, double[]> getRawdata() {
		return this.rawdata;
	}

	public void addToRawData(String key, double[] values){
		this.rawdata.put(key, values);
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public double getProcesseddata() {
		return processeddata;
	}

	public void setProcesseddata(double processeddata) {
		this.processeddata 	= 	processeddata;
		this.rawdata		=	null;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	//TODO check if generates JSON also for superclass
	public String generateJson(){
		Gson gson	=	new Gson();
		String json 	=	gson.toJson(this);
		return json;
	}

	public static TaskInstance loadFromJson(String s){
		Gson gson				=	new Gson();
		TaskInstance	newtask	=	gson.fromJson(s, TaskInstance.class);
		return newtask;
	}
	public void setHeuristic(String key,int maxnumberofelements,double meanvalue){ //poniamo che ho 1 solo item nell hashmap dei dati
		for(int i=5;i>0;i--){
			int k		=	maxnumberofelements/i;
			double[] testdata2	=	new double[k];
			TaskInstance newTask	=	new TaskInstance(this.getId(),this.getFormula(),this.getExpiration(),this.getThreshold(),this.startDate,this.endDate);
			for(int j=0;j<k;j++) {
				testdata2[j] = meanvalue;
				newTask.addToRawData(key, testdata2);
			}

			long start 	= 	System.currentTimeMillis();
			//System.out.println("DATI "+newTask.getRawdata()+" val k "+k);
			double res2	=	ExpressionSolver.getResult(this.getFormula(), newTask.getRawdata());
			long end	= 	System.currentTimeMillis()-start;
			System.out.println("ci ho messo per "+k+" elementi questo tempo "+end);
			this.heurstics.put(k, (double) end / k);
			keys[i-1]	=	k;
			System.out.println("TaskInstance calcolata per numero element " + k + " valore " + (double)end/k);
		}

		long average	=	0;
		/*
		for(int i=0;i<20;i++){
			int k		=	maxnumberofelements;
			double[] testdata2	=	new double[k];
			TaskInstance newTask	=	new TaskInstance(this.getId(),this.getFormula(),this.getExpiration(),this.getThreshold(),this.startDate,this.endDate);
			for(int j=0;j<k;j++) {
				testdata2[j] = meanvalue;
				newTask.addToRawData(key, testdata2);
			}

			long start 	= 	System.currentTimeMillis();
			//System.out.println("DATI "+newTask.getRawdata()+" val k "+k);
			double res2	=	ExpressionSolver.getResult(this.getFormula(), newTask.getRawdata());
			long end	= 	System.currentTimeMillis()-start;
			System.out.println("ci ho messo per "+k+" elementi questo tempo "+end);
			average	=	average+end;
			System.out.println("TaskInstance calcolata per numero element " + k + " valore " + (double)end/k);
		}
		System.out.println("ci ho messo per "+maxnumberofelements+" elementi IN MEDIA questo tempo "+(average/20));
		*/

	}

	public double calculateHeuristicTime() {
		int size	=	this.rawdata.size();
		if(size>=keys[4]){
			return heurstics.get(keys[4]);
		}
		double returnvalue	=	heurstics.get(keys[0]);;
		for(int i=0;i<5;i++){
			if(size>keys[i]){
				return returnvalue;
			}
			returnvalue	=	heurstics.get(keys[i]);
		}
		return returnvalue;
	}
}
