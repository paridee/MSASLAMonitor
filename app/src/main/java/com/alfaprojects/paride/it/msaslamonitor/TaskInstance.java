package com.alfaprojects.paride.it.msaslamonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;

public class TaskInstance extends Task {
	private Date 						startDate;
	private Date 						endDate;
	private Date						expirationDate;
	private double 						processeddata	=	-1;
	private HashMap<String,double[]> 	rawdata			=	new HashMap<String,double[]>();

	public String						debugTag		=	"";

	public TaskInstance(int id, String formula, int expiration,
						double threshold, Date startDate, Date endDate,HashMap<Integer,Double> heurstics,int[] keys) {
		super(id, formula, expiration, threshold);
		this.heurstics		=	heurstics;
		this.keys			=	keys;
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

	public void resetRawData(){
		this.rawdata	=	new HashMap<String,double[]>();
	}

	public int getDataSize(){
		int tempvalue	=	0;
		Set keyset	= this.rawdata.keySet();
		Iterator<String> iterator	=	keyset.iterator();
		while(iterator.hasNext()){
			String key	=	iterator.next();
			tempvalue	=	tempvalue	+	rawdata.get(key).length;
		}
		return tempvalue;
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

	public double calculateHeuristicTime() {
		int size	=	this.getDataSize();
		if(size<keys[0]){
			System.out.println("TaskInstance per il singolo elemento ci metto "+heurstics.get(keys[0]) +" indice "+0 +" ho numero di elementi: "+size);
			return (heurstics.get(keys[(keys.length-1)])*this.getDataSize());
		}
		if(heurstics.get(keys[0])==null){
			System.out.println("TaskInstance null heuristic type "+this.debugTag+" keys "+keys[0]+" "+keys[2]+" heuristics "+heurstics.size()+" valori chiavi "+keys.toString()+" verifica "+heurstics.keySet().toString());//TODO remove, debug istruction
		}
		else{
			System.out.println("TaskInstance NOT null heuristic type "+this.debugTag);
		}
		double returnvalue	=	heurstics.get(keys[0]);;
		for(int i=0;i<(keys.length-1);i++){
			if(size>=keys[i]){
				returnvalue	=	heurstics.get(keys[i]);
			}
			else{
				System.out.println("TaskInstance per il singolo elemento ci metto "+heurstics.get(keys[i-1]) +" indice "+(i-1) +" ho numero di elementi: "+size);
				return (returnvalue*this.getDataSize());
			}
		}
		System.out.println("TaskInstance per il singolo elemento ci metto "+heurstics.get(keys[keys.length-1]) +" indice "+(keys[keys.length-1]) +" ho numero di elementi: "+size);
		return (returnvalue*this.getDataSize());
	}
}
